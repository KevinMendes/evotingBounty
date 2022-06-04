/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.controlcomponents.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

/**
 * Regroups the context values needed by the GenEncLongCodeShares algorithm.
 *
 * <ul>
 * <li>ee, the election event ID. Not null.</li>
 * <li>vcs, the voting card set ID. Not null.</li>
 * <li>g, a group generator. Not null.</li>
 * <li>j, the CCR<sub>’</sub>s index. Not null.</li>
 * </ul>
 */
public class GenEncLongCodeSharesContext {
	private final String electionEventId;
	private final String verificationCardSetId;
	private final GqGroup gqGroup;
	private final int nodeID;

	private GenEncLongCodeSharesContext(final String electionEventId, final String verificationCardSetId, final GqGroup gqGroup, final int nodeID) {
		this.electionEventId = electionEventId;
		this.verificationCardSetId = verificationCardSetId;
		this.gqGroup = gqGroup;
		this.nodeID = nodeID;
	}

	public int getNodeID() {
		return nodeID;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public GqGroup getGqGroup() {
		return gqGroup;
	}

	/**
	 * Builder performing input validations before constructing a {@link GenEncLongCodeSharesContext}.
	 */
	public static class Builder {
		private String electionEventId;
		private String verificationCardSetId;
		private GqGroup gqGroup;
		private int nodeID;

		public Builder electionEventId(final String electionEventId) {
			this.electionEventId = electionEventId;
			return this;
		}

		public Builder verificationCardSetId(final String verificationCardSetId) {
			this.verificationCardSetId = verificationCardSetId;
			return this;
		}

		public Builder gqGroup(final GqGroup gqGroup) {
			this.gqGroup = gqGroup;
			return this;
		}

		public Builder nodeID(final int nodeID) {
			this.nodeID = nodeID;
			return this;
		}

		/**
		 * Creates the GenEncLongCodeSharesContext. All fields must have been set and be non-null.
		 *
		 * @return a new GenEncLongCodeSharesContext.
		 * @throws NullPointerException      if any of the fields is null.
		 * @throws FailedValidationException if any of the election event Id and verification card IDs do not comply with the required UUID format
		 */
		public GenEncLongCodeSharesContext build() {
			checkNotNull(electionEventId, "The election event id is null.");
			checkNotNull(verificationCardSetId, "The verification card set id is null.");
			checkNotNull(gqGroup, "The GqGroup is null.");

			validateUUID(electionEventId);
			validateUUID(verificationCardSetId);

			return new GenEncLongCodeSharesContext(electionEventId, verificationCardSetId, gqGroup, nodeID);
		}
	}
}

