/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

public class ReturnCodesNodeContext {

	private final int nodeId;
	private final String electionEventId;
	private final String verificationCardSetId;
	private final GqGroup encryptionGroup;

	public ReturnCodesNodeContext(final int nodeId, final String electionEventId, final String verificationCardSetId,
			final GqGroup encryptionGroup) {

		this.nodeId = nodeId;
		this.electionEventId = checkNotNull(electionEventId);
		this.verificationCardSetId = checkNotNull(verificationCardSetId);
		this.encryptionGroup = checkNotNull(encryptionGroup);
	}

	public int getNodeId() {
		return nodeId;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ReturnCodesNodeContext that = (ReturnCodesNodeContext) o;
		return nodeId == that.nodeId && electionEventId.equals(that.electionEventId) && verificationCardSetId.equals(that.verificationCardSetId)
				&& encryptionGroup.equals(that.encryptionGroup);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nodeId, electionEventId, verificationCardSetId, encryptionGroup);
	}
}
