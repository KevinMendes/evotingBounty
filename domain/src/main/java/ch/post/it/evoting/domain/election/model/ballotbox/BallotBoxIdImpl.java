/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.ballotbox;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Default implementation of a ballot box identifier.
 */
public class BallotBoxIdImpl implements BallotBoxId {

	private static final long serialVersionUID = 6368889285991914900L;

	private final String tenantId;

	private final String electionEventId;

	private final String id;

	@JsonCreator
	public BallotBoxIdImpl(
			@JsonProperty("tenantId")
			final
			String tenantId,
			@JsonProperty("electionEventId")
			final
			String electionEventId,
			@JsonProperty("ballotBoxId")
			final
			String id) {
		this.tenantId = Objects.requireNonNull(tenantId, "The tenant identifier cannot be null");
		this.electionEventId = Objects.requireNonNull(electionEventId, "The election event identifier cannot be null");
		this.id = Objects.requireNonNull(id, "The ballot box identifier cannot be null");
	}

	@Override
	public String toString() {
		return String.format("%s-%s-%s", tenantId, electionEventId, id);
	}

	@Override
	public String getTenantId() {
		return tenantId;
	}

	@Override
	public String getElectionEventId() {
		return electionEventId;
	}

	@Override
	@JsonProperty("ballotBoxId")
	public String getId() {
		return id;
	}

	@Override
	public boolean equals(final Object other) {
		if (null == other) {
			return false;
		}

		if (!(other instanceof BallotBoxId)) {
			return false;
		}

		return hashCode() == other.hashCode();
	}

	@Override
	public int hashCode() {
		return Objects.hash(tenantId, electionEventId, id);
	}
}
