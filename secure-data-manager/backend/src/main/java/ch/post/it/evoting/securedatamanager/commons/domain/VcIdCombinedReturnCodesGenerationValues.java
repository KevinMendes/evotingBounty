/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.commons.domain;

import java.util.List;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;

/**
 * Represents the combined contributions of the control components when generating the long return codes (both long choice return codes and long vote
 * cast return code) in the configuration phase.
 */
public class VcIdCombinedReturnCodesGenerationValues {

	private String verificationCardId;

	private ElGamalMultiRecipientCiphertext encryptedPreChoiceReturnCodes;

	private ElGamalMultiRecipientCiphertext encryptedPreVoteCastReturnCode;

	private List<String> voterChoiceReturnCodeGenerationPublicKey;

	private List<String> voterVoteCastReturnCodeGenerationPublicKey;

	private boolean poisonPill;

	private VcIdCombinedReturnCodesGenerationValues() {
		poisonPill = true;
	}

	public VcIdCombinedReturnCodesGenerationValues(final String verificationCardId,
			final ElGamalMultiRecipientCiphertext encryptedPreChoiceReturnCodes,
			final ElGamalMultiRecipientCiphertext encryptedPreVoteCastReturnCode) {
		this.verificationCardId = verificationCardId;
		this.encryptedPreChoiceReturnCodes = encryptedPreChoiceReturnCodes;
		this.encryptedPreVoteCastReturnCode = encryptedPreVoteCastReturnCode;
	}

	public VcIdCombinedReturnCodesGenerationValues(final String verificationCardId,
			final ElGamalMultiRecipientCiphertext encryptedPreChoiceReturnCodes, final ElGamalMultiRecipientCiphertext encryptedPreVoteCastReturnCode,
			final List<String> voterChoiceReturnCodeGenerationPublicKey, final List<String> voterVoteCastReturnCodeGenerationPublicKey) {
		this.verificationCardId = verificationCardId;
		this.encryptedPreChoiceReturnCodes = encryptedPreChoiceReturnCodes;
		this.encryptedPreVoteCastReturnCode = encryptedPreVoteCastReturnCode;
		this.voterChoiceReturnCodeGenerationPublicKey = voterChoiceReturnCodeGenerationPublicKey;
		this.voterVoteCastReturnCodeGenerationPublicKey = voterVoteCastReturnCodeGenerationPublicKey;
	}

	public static VcIdCombinedReturnCodesGenerationValues poisonPill() {
		return new VcIdCombinedReturnCodesGenerationValues();
	}

	public String getVerificationCardId() {
		return verificationCardId;
	}

	public void setVerificationCardId(final String verificationCardId) {
		this.verificationCardId = verificationCardId;
	}

	public ElGamalMultiRecipientCiphertext getEncryptedPreChoiceReturnCodes() {
		return encryptedPreChoiceReturnCodes;
	}

	public void setEncryptedPreChoiceReturnCodes(final ElGamalMultiRecipientCiphertext encryptedPreChoiceReturnCodes) {
		this.encryptedPreChoiceReturnCodes = encryptedPreChoiceReturnCodes;
	}

	public ElGamalMultiRecipientCiphertext getEncryptedPreVoteCastReturnCode() {
		return encryptedPreVoteCastReturnCode;
	}

	public void setEncryptedPreVoteCastReturnCode(final ElGamalMultiRecipientCiphertext encryptedPreVoteCastReturnCode) {
		this.encryptedPreVoteCastReturnCode = encryptedPreVoteCastReturnCode;
	}

	public List<String> getVoterChoiceReturnCodeGenerationPublicKey() {
		return voterChoiceReturnCodeGenerationPublicKey;
	}

	public void setVoterChoiceReturnCodeGenerationPublicKey(final List<String> voterChoiceReturnCodeGenerationPublicKey) {
		this.voterChoiceReturnCodeGenerationPublicKey = voterChoiceReturnCodeGenerationPublicKey;
	}

	public List<String> getVoterVoteCastReturnCodeGenerationPublicKey() {
		return voterVoteCastReturnCodeGenerationPublicKey;
	}

	public void setVoterVoteCastReturnCodeGenerationPublicKey(final List<String> voterVoteCastReturnCodeGenerationPublicKey) {
		this.voterVoteCastReturnCodeGenerationPublicKey = voterVoteCastReturnCodeGenerationPublicKey;
	}

	public boolean isPoisonPill() {
		return poisonPill;
	}

}
