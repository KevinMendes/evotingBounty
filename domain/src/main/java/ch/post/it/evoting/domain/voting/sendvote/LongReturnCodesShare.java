/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.sendvote;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.CorrelatedSupport;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;

@JsonDeserialize(using = LongReturnCodesShareDeserializer.class)
public abstract class LongReturnCodesShare extends CorrelatedSupport implements HashableList {

	@JsonProperty
	private final String electionEventId;

	@JsonProperty
	private final String verificationCardSetId;

	@JsonProperty
	private final String verificationCardId;

	@JsonProperty
	private final String requestId;

	@JsonProperty
	private final boolean isCastCode;

	@JsonProperty
	private final int nodeId;

	LongReturnCodesShare(final UUID correlationId, final String electionEventId, final String verificationCardSetId,
			final String verificationCardId, final String requestId, final boolean isCastCode, final int nodeId) {

		super(checkNotNull(correlationId));
		this.electionEventId = checkNotNull(electionEventId);
		this.verificationCardSetId = checkNotNull(verificationCardSetId);
		this.verificationCardId = checkNotNull(verificationCardId);
		this.requestId = checkNotNull(requestId);
		this.isCastCode = isCastCode;
		this.nodeId = nodeId;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public String getVerificationCardId() {
		return verificationCardId;
	}

	public String getRequestId() {
		return requestId;
	}

	@JsonIgnore
	public boolean isCastCode() {
		return isCastCode;
	}

	public int getNodeId() {
		return nodeId;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		final LongReturnCodesShare that = (LongReturnCodesShare) o;
		return electionEventId.equals(that.electionEventId) &&
				verificationCardSetId.equals(that.verificationCardSetId) &&
				verificationCardId.equals(that.verificationCardId) &&
				requestId.equals(that.requestId) &&
				isCastCode == that.isCastCode &&
				nodeId == that.nodeId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), electionEventId, verificationCardSetId, verificationCardId, requestId, isCastCode, nodeId);
	}
}
