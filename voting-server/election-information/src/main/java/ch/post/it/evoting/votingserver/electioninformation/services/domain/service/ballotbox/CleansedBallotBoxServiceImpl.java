/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.ballotbox;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.domain.election.model.ballotbox.BallotBoxId;
import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.CleansedBallotBoxRepositoryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.CleansedBallotBoxServiceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformationRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.CleansedBallotBox;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.CleansedBallotBoxRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.tenant.EiTenantSystemKeys;
import ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence.CleansedBallotBoxAccess;

public class CleansedBallotBoxServiceImpl implements CleansedBallotBoxService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CleansedBallotBoxServiceImpl.class);

	private static final String TENANT_ID = "100";
	private static final String ENCRYPTION_PARAMETERS_JSON_FIELD = "encryptionParameters";
	private static final String P_JSON_FIELD = "p";
	private static final String Q_JSON_FIELD = "q";
	private static final String G_JSON_FIELD = "g";

	@Inject
	private CleansedBallotBoxRepository cleansedBallotBoxRepository;

	@Inject
	private BallotBoxInformationRepository ballotBoxInformationRepository;

	@Inject
	private CleansedBallotBoxAccess cleansedBallotBoxAccess;

	@EJB
	private EiTenantSystemKeys eiTenantSystemKeys;

	@Inject
	private AsymmetricServiceAPI asymmetricService;

	@Inject
	private HashService hashService;

	@Inject
	private ObjectMapper objectMapper;

	@Override
	public boolean isBallotBoxEmpty(final String electionEventId, final String ballotBoxId) {
		checkNotNull(electionEventId);
		checkNotNull(ballotBoxId);

		return !cleansedBallotBoxRepository.exists(electionEventId, ballotBoxId);
	}

	@Override
	public MixnetInitialPayload getMixnetInitialPayload(final BallotBoxId ballotBoxId)
			throws ResourceNotFoundException, CleansedBallotBoxServiceException {

		checkNotNull(ballotBoxId);

		// Find out how many vote sets fit the ballot box.
		final int voteCount;
		try {
			voteCount = cleansedBallotBoxRepository.count(ballotBoxId);
		} catch (final CleansedBallotBoxRepositoryException e) {
			throw new CleansedBallotBoxServiceException(String.format("Failed to count votes for ballot box %s.", ballotBoxId), e);
		}

		// Get the encryption parameters from the ballot box information.
		final JsonObject ballotBoxInformation = getBallotBoxInformationJson(ballotBoxId);
		final JsonObject encryptionParametersJson = ballotBoxInformation.getJsonObject(ENCRYPTION_PARAMETERS_JSON_FIELD);

		final BigInteger p = new BigInteger(encryptionParametersJson.getString(P_JSON_FIELD));
		final BigInteger q = new BigInteger(encryptionParametersJson.getString(Q_JSON_FIELD));
		final BigInteger g = new BigInteger(encryptionParametersJson.getString(G_JSON_FIELD));
		final GqGroup encryptionParameters = new GqGroup(p, q, g);

		// Retrieve the encrypted votes.
		final List<ElGamalMultiRecipientCiphertext> encryptedVotes = cleansedBallotBoxRepository.getVoteSet(ballotBoxId, 0, voteCount)
				.map(encryptedVoteJson -> {
					try {
						return objectMapper.reader().withAttribute("group", encryptionParameters)
								.readValue(encryptedVoteJson, ElGamalMultiRecipientCiphertext.class);
					} catch (final IOException e) {
						throw new UncheckedIOException("Failed to deserialize encrypted vote.", e);
					}
				})
				.collect(Collectors.toList());

		final GqElement identityGqElement = GqElement.GqElementFactory.fromValue(BigInteger.ONE, encryptionParameters);
		final ElGamalMultiRecipientPublicKey electionPublicKey = new ElGamalMultiRecipientPublicKey(Collections.singletonList(identityGqElement));

		// Get the certificate chain for the election information public key.
		LOGGER.info("Finding the validation key certificate chain for ballot box {}...", ballotBoxId);
		final X509Certificate[] fullCertificateChain = eiTenantSystemKeys.getSigningCertificateChain(TENANT_ID);
		if (null == fullCertificateChain) {
			throw new CleansedBallotBoxServiceException("No certificate chain was found for tenant " + TENANT_ID);
		}
		final X509Certificate[] certificateChain = new X509Certificate[fullCertificateChain.length - 1];
		System.arraycopy(fullCertificateChain, 0, certificateChain, 0, fullCertificateChain.length - 1);
		LOGGER.info("Obtained the validation key certificate for tenant {} with {} elements", TENANT_ID, certificateChain.length);

		// Create the initial payload to send.
		final MixnetInitialPayload mixnetInitialPayload = new MixnetInitialPayload(ballotBoxId.getElectionEventId(), ballotBoxId.getId(),
				encryptionParameters, encryptedVotes, electionPublicKey);

		// Hash the payload.
		final byte[] payloadHash = hashService.recursiveHash(mixnetInitialPayload);

		// Get the election information system key to sign the payload.
		final PrivateKey signingKey = eiTenantSystemKeys.getSigningPrivateKey(TENANT_ID);
		LOGGER.info("Obtained the signing key for tenant {}, signing the initial payload...", TENANT_ID);

		// Sign the payload hash.
		final byte[] signature;
		try {
			signature = asymmetricService.sign(signingKey, payloadHash);
		} catch (final GeneralCryptoLibException e) {
			throw new CleansedBallotBoxServiceException("Failed to sign the initial payload.", e);
		}
		final CryptoPrimitivesPayloadSignature payloadSignature = new CryptoPrimitivesPayloadSignature(signature, certificateChain);
		mixnetInitialPayload.setSignature(payloadSignature);
		LOGGER.info("Initial payload signed successfully.");

		return mixnetInitialPayload;
	}

	/**
	 * Retrieves a ballot box's information
	 *
	 * @param ballotBoxId the ballot box identifier
	 * @return a ballot box's information as a JSON object
	 * @throws ResourceNotFoundException if the ballot box is not found
	 */
	private JsonObject getBallotBoxInformationJson(final BallotBoxId ballotBoxId) throws ResourceNotFoundException {
		return JsonUtils.getJsonObject(ballotBoxInformationRepository
				.findByTenantIdElectionEventIdBallotBoxId(ballotBoxId.getTenantId(), ballotBoxId.getElectionEventId(), ballotBoxId.getId())
				.getJson());
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void storeCleansedVote(final Vote vote) throws DuplicateEntryException {
		final CleansedBallotBox cleansedBallotBox = new CleansedBallotBox();
		cleansedBallotBox.setTenantId(vote.getTenantId());
		cleansedBallotBox.setElectionEventId(vote.getElectionEventId());
		cleansedBallotBox.setVotingCardId(vote.getVotingCardId());
		cleansedBallotBox.setBallotId(vote.getBallotId());
		cleansedBallotBox.setBallotBoxId(vote.getBallotBoxId());
		final String encryptedVote = vote.getEncryptedOptions();

		cleansedBallotBox.setEncryptedVote(encryptedVote);
		cleansedBallotBoxAccess.save(cleansedBallotBox);
	}

}
