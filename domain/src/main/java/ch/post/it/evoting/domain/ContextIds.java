/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;

@JsonPropertyOrder({ "electionEventId", "verificationCardSetId", "verificationCardId" })
public class ContextIds implements HashableList {

	@JsonProperty
	private final String electionEventId;

	@JsonProperty
	private final String verificationCardSetId;

	@JsonProperty
	private final String verificationCardId;

	@JsonCreator
	public ContextIds(
			@JsonProperty("electionEventId")
			final String electionEventId,
			@JsonProperty("verificationCardSetId")
			final String verificationCardSetId,
			@JsonProperty("verificationCardId")
			final String verificationCardId) {

		this.electionEventId = checkNotNull(electionEventId);
		this.verificationCardSetId = checkNotNull(verificationCardSetId);
		this.verificationCardId = checkNotNull(verificationCardId);
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

	@Override
	public ImmutableList<Hashable> toHashableForm() {
		return ImmutableList.of(HashableString.from(electionEventId), HashableString.from(verificationCardSetId),
				HashableString.from(verificationCardId));
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ContextIds that = (ContextIds) o;
		return electionEventId.equals(that.electionEventId) && verificationCardSetId.equals(that.verificationCardSetId) && verificationCardId.equals(
				that.verificationCardId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(electionEventId, verificationCardSetId, verificationCardId);
	}

	@Override
	public String toString() {
		return "ContextIds{" +
				"electionEventId='" + electionEventId + '\'' +
				", verificationCardSetId='" + verificationCardSetId + '\'' +
				", verificationCardId='" + verificationCardId + '\'' +
				'}';
	}
}
