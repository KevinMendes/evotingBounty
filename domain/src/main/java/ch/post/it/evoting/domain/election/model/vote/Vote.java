/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.vote;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ch.post.it.evoting.domain.election.errors.SemanticErrorGroup;
import ch.post.it.evoting.domain.election.errors.SyntaxErrorGroup;
import ch.post.it.evoting.domain.election.model.constants.Constants;
import ch.post.it.evoting.domain.election.model.constants.Patterns;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * This class represents the vote in this context.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Vote {

	@NotNull(groups = SyntaxErrorGroup.class)
	@Pattern(regexp = Patterns.ID, groups = SemanticErrorGroup.class)
	@Size(min = 1, max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String tenantId;

	@NotNull(groups = SyntaxErrorGroup.class)
	@Pattern(regexp = Patterns.ID, groups = SemanticErrorGroup.class)
	@Size(min = 1, max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String electionEventId;

	@NotNull(groups = SyntaxErrorGroup.class)
	@Pattern(regexp = Patterns.ID, groups = SemanticErrorGroup.class)
	@Size(min = 1, max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String ballotId;

	// The identifier of the ballot box to which the vote belongs.
	@NotNull(groups = SyntaxErrorGroup.class)
	@Pattern(regexp = Patterns.ID, groups = SemanticErrorGroup.class)
	@Size(min = 1, max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String ballotBoxId;

	@NotNull(groups = SyntaxErrorGroup.class)
	@Pattern(regexp = Patterns.ID, groups = SemanticErrorGroup.class)
	@Size(min = 1, max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String votingCardId;

	// The encrypted options as input.
	@NotNull(groups = SyntaxErrorGroup.class)
	private String encryptedOptions;

	// The partial choice codes.
	private String encryptedPartialChoiceCodes;

	@NotNull(groups = SyntaxErrorGroup.class)
	private String correctnessIds;

	@NotNull(groups = SyntaxErrorGroup.class)
	private String credentialId;

	private String authenticationTokenSignature;

	private String authenticationToken;

	/**
	 * The vote ciphertext that contains the exponentiated elements (C'0, C'1).
	 */
	private String cipherTextExponentiations;

	private String exponentiationProof;

	private String plaintextEqualityProof;

	private String verificationCardId;

	private String verificationCardSetId;

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(final String tenantId) {
		this.tenantId = tenantId;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public void setElectionEventId(final String electionEventId) {
		this.electionEventId = electionEventId;
	}

	public String getVotingCardId() {
		return votingCardId;
	}

	public void setVotingCardId(final String votingCardId) {
		this.votingCardId = votingCardId;
	}

	public String getEncryptedOptions() {
		return encryptedOptions;
	}

	public void setEncryptedOptions(final String encryptedOptions) {
		this.encryptedOptions = encryptedOptions;
	}

	public String getBallotId() {
		return ballotId;
	}

	public void setBallotId(final String ballotId) {
		this.ballotId = ballotId;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public void setBallotBoxId(final String ballotBoxId) {
		this.ballotBoxId = ballotBoxId;
	}

	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(final String credentialId) {
		this.credentialId = credentialId;
	}

	public String getEncryptedPartialChoiceCodes() {
		return encryptedPartialChoiceCodes;
	}

	public void setEncryptedPartialChoiceCodes(final String encryptedPartialChoiceCodes) {
		this.encryptedPartialChoiceCodes = encryptedPartialChoiceCodes;
	}

	public String getAuthenticationTokenSignature() {
		return authenticationTokenSignature;
	}

	public void setAuthenticationTokenSignature(final String authenticationTokenSignature) {
		this.authenticationTokenSignature = authenticationTokenSignature;
	}

	public String getCorrectnessIds() {
		return correctnessIds;
	}

	public void setCorrectnessIds(String correctnessIds) {
		this.correctnessIds = correctnessIds;
	}

	public String getCipherTextExponentiations() {
		return cipherTextExponentiations;
	}

	public void setCipherTextExponentiations(final String cipherTextExponentiations) {
		this.cipherTextExponentiations = cipherTextExponentiations;
	}

	public String getExponentiationProof() {
		return exponentiationProof;
	}

	public void setExponentiationProof(final String exponentiationProof) {
		this.exponentiationProof = exponentiationProof;
	}

	public String getPlaintextEqualityProof() {
		return plaintextEqualityProof;
	}

	public void setPlaintextEqualityProof(final String plaintextEqualityProof) {
		this.plaintextEqualityProof = plaintextEqualityProof;
	}

	public String getAuthenticationToken() {
		return authenticationToken;
	}

	public void setAuthenticationToken(final String authenticationToken) {
		this.authenticationToken = authenticationToken;
	}

	public String getVerificationCardId() {
		return verificationCardId;
	}

	public void setVerificationCardId(String verificationCardId) {
		this.verificationCardId = verificationCardId;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public void setVerificationCardSetId(String verificationCardSetId) {
		this.verificationCardSetId = verificationCardSetId;
	}

	@JsonIgnore
	public String[] getFieldsAsStringArray() {
		return new String[] { encryptedOptions, correctnessIds, authenticationTokenSignature, votingCardId, electionEventId };
	}

}
