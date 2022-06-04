/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import ch.post.it.evoting.controlcomponents.VerificationCardEntity;

@Entity
@Table(name = "ENCRYPTED_VERIFIABLE_VOTE")
public class EncryptedVerifiableVoteEntity {

	@Id
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "VERIFICATION_CARD_FK_ID")
	private VerificationCardEntity verificationCardEntity;

	private byte[] contextIds;

	private byte[] encryptedVote;

	private byte[] exponentiatedEncryptedVote;

	private byte[] encryptedPartialChoiceReturnCodes;

	private byte[] exponentiationProof;

	private byte[] plaintextEqualityProof;

	@Version
	private Integer changeControlId;

	public EncryptedVerifiableVoteEntity() {
	}

	private EncryptedVerifiableVoteEntity(final byte[] contextIds, final byte[] encryptedVote, final byte[] exponentiatedEncryptedVote,
			final byte[] encryptedPartialChoiceReturnCodes, final byte[] exponentiationProof, final byte[] plaintextEqualityProof,
			final VerificationCardEntity verificationCardEntity) {
		this.contextIds = contextIds;
		this.encryptedVote = encryptedVote;
		this.exponentiatedEncryptedVote = exponentiatedEncryptedVote;
		this.encryptedPartialChoiceReturnCodes = encryptedPartialChoiceReturnCodes;
		this.exponentiationProof = exponentiationProof;
		this.plaintextEqualityProof = plaintextEqualityProof;
		this.verificationCardEntity = verificationCardEntity;
	}

	public byte[] getEncryptedVote() {
		return encryptedVote;
	}

	public byte[] getExponentiatedEncryptedVote() {
		return exponentiatedEncryptedVote;
	}

	public byte[] getEncryptedPartialChoiceReturnCodes() {
		return encryptedPartialChoiceReturnCodes;
	}

	public byte[] getExponentiationProof() {
		return exponentiationProof;
	}

	public byte[] getPlaintextEqualityProof() {
		return plaintextEqualityProof;
	}

	public byte[] getContextIds() {
		return contextIds;
	}

	public Integer getChangeControlId() {
		return changeControlId;
	}

	public VerificationCardEntity getVerificationCardEntity() {
		return verificationCardEntity;
	}

	public static class Builder {

		private byte[] contextIds;
		private byte[] encryptedVote;
		private byte[] exponentiatedEncryptedVote;
		private byte[] encryptedPartialChoiceReturnCodes;
		private byte[] exponentiationProof;
		private byte[] plaintextEqualityProof;
		private VerificationCardEntity verificationCardEntity;

		public Builder setContextIds(final byte[] contextIds) {
			this.contextIds = checkNotNull(contextIds);
			return this;
		}

		public Builder setEncryptedVote(final byte[] encryptedVote) {
			this.encryptedVote = checkNotNull(encryptedVote);
			return this;
		}

		public Builder setExponentiatedEncryptedVote(final byte[] exponentiatedEncryptedVote) {
			this.exponentiatedEncryptedVote = checkNotNull(exponentiatedEncryptedVote);
			return this;
		}

		public Builder setEncryptedPartialChoiceReturnCodes(final byte[] encryptedPartialChoiceReturnCodes) {
			this.encryptedPartialChoiceReturnCodes = checkNotNull(encryptedPartialChoiceReturnCodes);
			return this;
		}

		public Builder setExponentiationProof(final byte[] exponentiationProof) {
			this.exponentiationProof = checkNotNull(exponentiationProof);
			return this;
		}

		public Builder setPlaintextEqualityProof(final byte[] plaintextEqualityProof) {
			this.plaintextEqualityProof = checkNotNull(plaintextEqualityProof);
			return this;
		}

		public Builder setVerificationCardEntity(final VerificationCardEntity verificationCardEntity) {
			this.verificationCardEntity = verificationCardEntity;
			return this;
		}

		public EncryptedVerifiableVoteEntity build() {
			checkNotNull(contextIds);
			checkNotNull(encryptedVote);
			checkNotNull(exponentiatedEncryptedVote);
			checkNotNull(encryptedPartialChoiceReturnCodes);
			checkNotNull(exponentiationProof);
			checkNotNull(plaintextEqualityProof);
			checkNotNull(verificationCardEntity);

			return new EncryptedVerifiableVoteEntity(contextIds, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes,
					exponentiationProof, plaintextEqualityProof, verificationCardEntity);
		}
	}
}
