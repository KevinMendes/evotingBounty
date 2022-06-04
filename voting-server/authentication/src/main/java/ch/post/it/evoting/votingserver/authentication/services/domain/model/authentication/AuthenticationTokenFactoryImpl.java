/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import java.util.Base64;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitives;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitivesService;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.information.VoterInformationRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.exception.AuthenticationTokenGenerationException;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.exception.AuthenticationTokenSigningException;
import ch.post.it.evoting.votingserver.authentication.services.infrastructure.remote.ValidationRepositoryImpl;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.crypto.SignatureForObjectService;
import ch.post.it.evoting.votingserver.commons.util.DateUtils;

/**
 * Factory class for building authentication token.
 */
@Stateless
public class AuthenticationTokenFactoryImpl implements AuthenticationTokenFactory {

	private static final String KEYSTORE_ALIAS = "privatekey";

	// the length of the authentication token id
	private static final int LENGTH_TOKEN_ID = 24;
	private static final String EMPTY_ID = "";

	private final CryptoPrimitives cryptoPrimitives = CryptoPrimitivesService.get();

	@Inject
	private VoterInformationRepository voterInformationRepository;

	@Inject
	private SignatureForObjectService signatureService;

	@Inject
	private ValidationRepositoryImpl validationRepository;

	/**
	 * @see AuthenticationTokenFactory#buildAuthenticationToken(String, String, String)
	 */
	@Override
	public AuthenticationTokenMessage buildAuthenticationToken(final String tenantId, final String electionEventId, final String credentialId)
			throws AuthenticationTokenGenerationException, AuthenticationTokenSigningException {
		try {
			// get information for auth token generation
			VoterInformation voterInformation = voterInformationRepository
					.findByTenantIdElectionEventIdCredentialId(tenantId, electionEventId, credentialId);

			// validates that election is in dates
			AuthenticationTokenMessage authenticationTokenMessage = new AuthenticationTokenMessage();
			ValidationResult validateIfElectionIsOpen = validationRepository
					.validateElectionInDates(tenantId, electionEventId, voterInformation.getBallotBoxId());

			// always include the auth-message and validation-error in the auth-token
			authenticationTokenMessage.setValidationError(validateIfElectionIsOpen.getValidationError());

			String base64AuthenticationTokenId = cryptoPrimitives.genRandomBase64String(LENGTH_TOKEN_ID);
			String currentTimestamp = DateUtils.getTimestamp();
			byte[] tokenSignature = signAuthenticationToken(tenantId, electionEventId, voterInformation, base64AuthenticationTokenId,
					currentTimestamp);

			// create token
			AuthenticationToken authenticationToken = new AuthenticationToken(voterInformation, base64AuthenticationTokenId, currentTimestamp,
					Base64.getEncoder().encodeToString(tokenSignature));
			authenticationTokenMessage.setAuthenticationToken(authenticationToken);
			return authenticationTokenMessage;
		} catch (ResourceNotFoundException e) {
			throw new AuthenticationTokenGenerationException("Error generating the authentication token", e);
		}
	}

	private byte[] signAuthenticationToken(final String tenantId, final String electionEventId, final VoterInformation voterInformation,
			final String base64AuthenticationTokenId, final String currentTimestamp) throws AuthenticationTokenSigningException {
		// sign the auth token
		try {
			return signatureService.sign(tenantId, electionEventId, EMPTY_ID, KEYSTORE_ALIAS, base64AuthenticationTokenId, currentTimestamp,
					voterInformation.getTenantId(), voterInformation.getElectionEventId(), voterInformation.getVotingCardId(),
					voterInformation.getBallotId(), voterInformation.getCredentialId(), voterInformation.getVerificationCardId(),
					voterInformation.getBallotBoxId(), voterInformation.getVerificationCardSetId(), voterInformation.getVotingCardSetId());
		} catch (CryptographicOperationException | ResourceNotFoundException e) {
			throw new AuthenticationTokenSigningException("Error signing authentication token", e);
		}
	}

}
