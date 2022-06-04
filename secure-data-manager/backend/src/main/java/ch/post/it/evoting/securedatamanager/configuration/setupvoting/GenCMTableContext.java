/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

/**
 * Regroups the context values needed by the GenCMTable algorithm.
 */
public class GenCMTableContext {
	private final GqGroup encryptionGroup;
	private final String electionEventId;
	private final String ballotId;
	private final String verificationCardSetId;
	private final ElGamalMultiRecipientPrivateKey setupSecretKey;

	private GenCMTableContext(final GqGroup encryptionGroup, final String electionEventId, final String ballotId, final String verificationCardSetId,
			final ElGamalMultiRecipientPrivateKey setupSecretKey) {
		this.encryptionGroup = encryptionGroup;
		this.electionEventId = electionEventId;
		this.ballotId = ballotId;
		this.verificationCardSetId = verificationCardSetId;
		this.setupSecretKey = setupSecretKey;
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getBallotId() {
		return ballotId;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public ElGamalMultiRecipientPrivateKey getSetupSecretKey() {
		return setupSecretKey;
	}

	public static class Builder {
		private GqGroup encryptionGroup;
		private String electionEventId;
		private String ballotId;
		private String verificationCardSetId;
		private ElGamalMultiRecipientPrivateKey setupSecretKey;

		public Builder setEncryptionGroup(final GqGroup encryptionGroup) {
			this.encryptionGroup = encryptionGroup;
			return this;
		}

		public Builder setElectionEventId(final String electionEventId) {
			this.electionEventId = electionEventId;
			return this;
		}

		public Builder setBallotId(final String ballotId) {
			this.ballotId = ballotId;
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
		 * Creates the GenCMTableContext. All fields must have been set and be non-null.
		 *
		 * @throws NullPointerException      if any of the fields is null.
		 * @throws IllegalArgumentException  if
		 *                                   <ul>
		 *                                       <li>The {@code encryptionGroup} and {@code setupSecretKey} do not have the same group order.</li>
		 *                                       <li>The {@code setupSecretKey} does not have omega elements.</li>
		 *                                   </ul>
		 * @throws FailedValidationException if the {@code electionEventId}, {@code ballotId} and {@code votingCardSetId} do not comply with the UUID
		 *                                   format.
		 */
		public GenCMTableContext build() {
			checkNotNull(encryptionGroup);
			validateUUID(electionEventId);
			validateUUID(ballotId);
			validateUUID(verificationCardSetId);
			checkNotNull(setupSecretKey);

			checkArgument(encryptionGroup.hasSameOrderAs(setupSecretKey.getGroup()),
					"The setup secret key must have the same group order than the encryption group.");

			checkArgument(setupSecretKey.size() == GenCMTableService.OMEGA, "The setup secret key must have omega elements.");
			return new GenCMTableContext(encryptionGroup, electionEventId, ballotId, verificationCardSetId, setupSecretKey);
		}
	}
}
