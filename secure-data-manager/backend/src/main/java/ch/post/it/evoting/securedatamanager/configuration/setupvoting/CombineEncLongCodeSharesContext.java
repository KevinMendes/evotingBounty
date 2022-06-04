/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;

/**
 * Regroups the context values needed by the CombineEncLongCodeShares algorithm.
 *
 * <ul>
 * <li>vcs, the verification card set ID. Not null.</li>
 * <li>sk<sub>setup</sub>, the setup secret key. Not null.</li>
 * </ul>
 */
public class CombineEncLongCodeSharesContext {

	private final String electionEventId;
	private final String verificationCardSetId;
	private final ElGamalMultiRecipientPrivateKey setupSecretKey;

	private CombineEncLongCodeSharesContext(final String electionEventId, final String verificationCardSetId,
			final ElGamalMultiRecipientPrivateKey setupSecretKey) {
		this.electionEventId = electionEventId;
		this.verificationCardSetId = verificationCardSetId;
		this.setupSecretKey = setupSecretKey;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public ElGamalMultiRecipientPrivateKey getSetupSecretKey() {
		return setupSecretKey;
	}

	/**
	 * Builder performing input validations before constructing a {@link CombineEncLongCodeSharesContext}.
	 */
	public static class Builder {
		private String electionEventId;
		private String verificationCardSetId;
		private ElGamalMultiRecipientPrivateKey setupSecretKey;

		public Builder setElectionEventId(final String electionEventId) {
			this.electionEventId = electionEventId;
			return this;
		}

		public Builder setVerificationCardSetId(final String verificationCardSetId) {
			this.verificationCardSetId = verificationCardSetId;
			return this;
		}

		public Builder setSetupSecretKey(final ElGamalMultiRecipientPrivateKey setupSecretKey) {
			this.setupSecretKey = setupSecretKey;
			return this;
		}

		/**
		 * Creates the CombineEncLongCodeSharesContext. All fields must have been set and be non-null.
		 *
		 * @return a new CombineEncLongCodeSharesContext.
		 * @throws NullPointerException      if any of the fields is null.
		 * @throws FailedValidationException if {@code electionEventId} or {@code verificationCardSetId} have invalid UUID.
		 */
		public CombineEncLongCodeSharesContext build() {

			validateUUID(electionEventId);
			validateUUID(verificationCardSetId);
			checkNotNull(setupSecretKey, "The Setup Secret Key must not be null.");

			return new CombineEncLongCodeSharesContext(electionEventId, verificationCardSetId, setupSecretKey);
		}
	}
}
