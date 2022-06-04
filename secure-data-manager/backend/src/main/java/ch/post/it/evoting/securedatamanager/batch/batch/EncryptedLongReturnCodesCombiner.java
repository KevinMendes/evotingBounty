/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.NUMBER_CCG;
import static org.msgpack.core.Preconditions.checkArgument;
import static org.msgpack.core.Preconditions.checkNotNull;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ChoiceCodeGenerationDTO;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationOutput;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationResponsePayload;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.domain.cryptoadapters.CryptoAdapters;
import ch.post.it.evoting.domain.election.model.messaging.InvalidSignatureException;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.CryptolibPayloadSignatureService;
import ch.post.it.evoting.securedatamanager.commons.domain.VcIdCombinedReturnCodesGenerationValues;

/**
 * Implements the algorithm CombineEncLongCodeShares in the configuration phase.
 * <p>
 * The nodeContributions refers to the contributions of the control components generated in GenEncLongCodeShares.
 */
@Service
@JobScope
public class EncryptedLongReturnCodesCombiner implements ItemProcessor<NodeContributions, List<VcIdCombinedReturnCodesGenerationValues>> {

	private final X509Certificate trustedCertificate;

	private final CryptolibPayloadSignatureService payloadSignatureService;

	public EncryptedLongReturnCodesCombiner(final CryptolibPayloadSignatureService payloadSignatureService,
			@Value("#{jobExecutionContext['" + Constants.PLATFORM_ROOT_CA_CERTIFICATE + "']}")
			final String platformRootCACertificate) {

		try {
			this.trustedCertificate = (X509Certificate) PemUtils.certificateFromPem(platformRootCACertificate);
		} catch (final GeneralCryptoLibException e) {
			throw new IllegalArgumentException("Error obtaining platform root certificate", e);
		}

		this.payloadSignatureService = payloadSignatureService;

	}

	@Override
	public List<VcIdCombinedReturnCodesGenerationValues> process(final NodeContributions nodeContributions) throws Exception {
		final List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> nodeContributionResponse = nodeContributions
				.getNodeContributionResponse();

		verifySignatures(nodeContributionResponse);
		verifyIdsConsistency(nodeContributionResponse);

		final CombineEncLongCodeSharesOutput combineEncLongCodeSharesOutput = combineEncLongCodeShares(nodeContributionResponse);

		return buildResponse(nodeContributionResponse, combineEncLongCodeSharesOutput);
	}

	private void verifyIdsConsistency(final List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> nodeContributions) {
		checkArgument(!nodeContributions.isEmpty(), "The node contributions list can not be empty.");

		final String electionEventId = nodeContributions.get(0).getPayload().getElectionEventId();
		final String verificationCardSetId = nodeContributions.get(0).getPayload().getVerificationCardSetId();
		final int chunkId = nodeContributions.get(0).getPayload().getChunkId();

		validateUUID(electionEventId);
		validateUUID(verificationCardSetId);

		checkArgument(nodeContributions.stream().map(ChoiceCodeGenerationDTO::getPayload).map(ReturnCodeGenerationResponsePayload::getElectionEventId)
				.allMatch(eeid -> eeid.equals(electionEventId)), "All node contributions must be have the same election event id.");
		checkArgument(
				nodeContributions.stream().map(ChoiceCodeGenerationDTO::getPayload).map(ReturnCodeGenerationResponsePayload::getVerificationCardSetId)
						.allMatch(vcsid -> vcsid.equals(verificationCardSetId)),
				"All node contributions must be have the same verification card set id.");
		checkArgument(nodeContributions.stream().map(ChoiceCodeGenerationDTO::getPayload).map(ReturnCodeGenerationResponsePayload::getChunkId)
				.allMatch(cid -> cid.equals(chunkId)), "All node contributions must be have the same chunk id.");
	}

	private void verifySignatures(final List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> nodeContributions)
			throws PayloadVerificationException, InvalidSignatureException {

		for (final ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload> returnCodeNoteContribution : nodeContributions) {
			final ReturnCodeGenerationResponsePayload responsePayload = returnCodeNoteContribution.getPayload();

			final boolean isSignatureValid = payloadSignatureService.verify(responsePayload, trustedCertificate);

			if (!isSignatureValid) {
				throw new InvalidSignatureException("verification card set", returnCodeNoteContribution.getPayload().getVerificationCardSetId());
			}
		}
	}

	/**
	 * Combines the control components’encrypted long return code shares.
	 *
	 * @param controlComponentsContributions the contributions of the control components generated in GenEncLongCodeShares.
	 * @return the combined contributions as a {@link CombineEncLongCodeSharesOutput}.
	 */
	@SuppressWarnings("java:S117")
	private CombineEncLongCodeSharesOutput combineEncLongCodeShares(
			final List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> controlComponentsContributions) {

		checkNotNull(controlComponentsContributions);

		final ImmutableList<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> contributions = ImmutableList
				.copyOf(controlComponentsContributions);

		checkArgument(contributions.size() == NUMBER_CCG, String.format("There must be contributions from %s control components.", NUMBER_CCG));

		final List<ReturnCodeGenerationOutput> outputs = contributions.get(0).getPayload().getReturnCodeGenerationOutputs();

		// Size checking. Since we chunk the payloads, we are not directly working with N_E.
		final int chunkSize = outputs.size();
		final int n = outputs.get(0).getExponentiatedEncryptedPartialChoiceReturnCodes().size();
		checkArgument(contributions.stream().allMatch(contribution -> contribution.getPayload().getReturnCodeGenerationOutputs().size() == chunkSize),
				"The list of Return Code Generation Outputs of each control component must be of the same size.");
		checkArgument(contributions.stream().flatMap(contribution -> contribution.getPayload().getReturnCodeGenerationOutputs().stream())
						.allMatch(output -> output.getExponentiatedEncryptedPartialChoiceReturnCodes().size() == n),
				"All exponentiated, encrypted partial Choice Return Codes must be of the same size.");
		checkArgument(contributions.stream().flatMap(contribution -> contribution.getPayload().getReturnCodeGenerationOutputs().stream())
						.allMatch(output -> output.getExponentiatedEncryptedConfirmationKey().size() == 1),
				"All exponentiated, encrypted confirmation keys must be of size 1.");

		// Group checking.
		final GqGroup gqGroup = outputs.get(0).getExponentiatedEncryptedPartialChoiceReturnCodes().getGroup();
		checkArgument(contributions.stream().flatMap(contribution -> contribution.getPayload().getReturnCodeGenerationOutputs().stream())
						.allMatch(output -> output.getExponentiatedEncryptedPartialChoiceReturnCodes().getGroup().equals(gqGroup)),
				"All exponentiated, encrypted partial Choice Return Codes must be part of the same group.");
		checkArgument(contributions.stream().flatMap(contribution -> contribution.getPayload().getReturnCodeGenerationOutputs().stream())
						.allMatch(output -> output.getExponentiatedEncryptedConfirmationKey().getGroup().equals(gqGroup)),
				"All exponentiated, encrypted confirmation keys must be part of the same group.");
		checkArgument(outputs.get(0).getExponentiatedEncryptedConfirmationKey().getGroup().equals(gqGroup),
				"The exponentiated, encrypted partial Choice Return Codes must be part of the same group than the exponentiated, encrypted confirmation keys.");

		// Check verification card ids consistency.
		for (int i = 0; i < chunkSize; i++) {
			for (int j = 0; j < NUMBER_CCG - 1; j++) {
				checkArgument(contributions.get(j).getPayload().getReturnCodeGenerationOutputs().get(i).getVerificationCardId()
								.equals(contributions.get(j + 1).getPayload().getReturnCodeGenerationOutputs().get(i).getVerificationCardId()),
						String.format("The verification card ids are not consistent between contributions %s and %s at index %s.", j + 1, j + 2, i));
			}
		}

		final List<ElGamalMultiRecipientCiphertext> c_pC = new ArrayList<>();
		final List<ElGamalMultiRecipientCiphertext> c_pVCC = new ArrayList<>();

		// Algorithm.
		final ElGamalMultiRecipientCiphertext c_pC_identity = ElGamalMultiRecipientCiphertext.neutralElement(n, gqGroup);
		final ElGamalMultiRecipientCiphertext c_pVCC_identity = ElGamalMultiRecipientCiphertext.neutralElement(1, gqGroup);
		for (int id = 0; id < chunkSize; id++) {
			ElGamalMultiRecipientCiphertext c_pC_id = c_pC_identity;
			ElGamalMultiRecipientCiphertext c_pVCC_id = c_pVCC_identity;

			for (int j = 0; j < NUMBER_CCG; j++) {
				final ReturnCodeGenerationOutput returnCodeGenerationOutput = contributions.get(j).getPayload().getReturnCodeGenerationOutputs()
						.get(id);

				final ElGamalMultiRecipientCiphertext c_expPCC_j_id = returnCodeGenerationOutput.getExponentiatedEncryptedPartialChoiceReturnCodes();
				c_pC_id = c_pC_id.multiply(c_expPCC_j_id);

				final ElGamalMultiRecipientCiphertext c_expCK_j_id = returnCodeGenerationOutput.getExponentiatedEncryptedConfirmationKey();
				c_pVCC_id = c_pVCC_id.multiply(c_expCK_j_id);
			}

			c_pC.add(c_pC_id);
			c_pVCC.add(c_pVCC_id);
		}

		return new CombineEncLongCodeSharesOutput(c_pC, c_pVCC);
	}

	private List<VcIdCombinedReturnCodesGenerationValues> buildResponse(
			final List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> nodeContributionResponse,
			final CombineEncLongCodeSharesOutput combineEncLongCodeSharesOutput) {

		final List<VcIdCombinedReturnCodesGenerationValues> response = new ArrayList<>();

		final int chunkSize = nodeContributionResponse.get(0).getPayload().getReturnCodeGenerationOutputs().size();
		for (int i = 0; i < chunkSize; i++) {
			// Collect each control components' Voter Return Code Generation public keys for each verification card.
			final List<String> voterChoiceReturnCodesGenerationPublicKeys = new ArrayList<>();
			final List<String> voterVoteCastReturnCodeGenerationPublicKeys = new ArrayList<>();
			for (int j = 0; j < NUMBER_CCG; j++) {
				final ReturnCodeGenerationOutput returnCodeGenerationOutput = nodeContributionResponse.get(j).getPayload()
						.getReturnCodeGenerationOutputs().get(i);

				final String voterChoiceReturnCodesGenerationPublicKeyJson = convertToJson(
						returnCodeGenerationOutput.getVoterChoiceReturnCodeGenerationPublicKey());
				voterChoiceReturnCodesGenerationPublicKeys.add(voterChoiceReturnCodesGenerationPublicKeyJson);

				final String voterVoteCastReturnCodeGenerationPublicKeyJson = convertToJson(
						returnCodeGenerationOutput.getVoterVoteCastReturnCodeGenerationPublicKey());
				voterVoteCastReturnCodeGenerationPublicKeys.add(voterVoteCastReturnCodeGenerationPublicKeyJson);
			}

			// The verification card id is the same for all nodes.
			final String verificationCardId = nodeContributionResponse.get(0).getPayload().getReturnCodeGenerationOutputs().get(i)
					.getVerificationCardId();

			response.add(new VcIdCombinedReturnCodesGenerationValues(verificationCardId,
					combineEncLongCodeSharesOutput.getEncryptedPreChoiceReturnCodes().get(i),
					combineEncLongCodeSharesOutput.getEncryptedPreVoteCastReturnCodes().get(i), voterChoiceReturnCodesGenerationPublicKeys,
					voterVoteCastReturnCodeGenerationPublicKeys));
		}

		return response;
	}

	/**
	 * Converts an ElGamalMultiRecipientPublicKey to the json representation of its ElGamalPublicKey equivalent.
	 */
	private String convertToJson(final ElGamalMultiRecipientPublicKey elGamalMultiRecipientPublicKey) {
		try {
			return CryptoAdapters.convert(elGamalMultiRecipientPublicKey).toJson();
		} catch (final GeneralCryptoLibException e) {
			throw new IllegalArgumentException("Failed to convert multi recipient public key to single recipient public key json.");
		}
	}

	/**
	 * DTO to encapsulate the result of the CombineEncLongCodeShares algorithm.
	 */
	private static class CombineEncLongCodeSharesOutput {

		private final List<ElGamalMultiRecipientCiphertext> encryptedPreChoiceReturnCodes;
		private final List<ElGamalMultiRecipientCiphertext> encryptedPreVoteCastReturnCodes;

		private CombineEncLongCodeSharesOutput(final List<ElGamalMultiRecipientCiphertext> encryptedPreChoiceReturnCodes,
				final List<ElGamalMultiRecipientCiphertext> encryptedPreVoteCastReturnCodes) {

			this.encryptedPreChoiceReturnCodes = encryptedPreChoiceReturnCodes;
			this.encryptedPreVoteCastReturnCodes = encryptedPreVoteCastReturnCodes;
		}

		private List<ElGamalMultiRecipientCiphertext> getEncryptedPreChoiceReturnCodes() {
			return encryptedPreChoiceReturnCodes;
		}

		private List<ElGamalMultiRecipientCiphertext> getEncryptedPreVoteCastReturnCodes() {
			return encryptedPreVoteCastReturnCodes;
		}
	}
}
