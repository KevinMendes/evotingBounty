/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.configuration;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@JsonPropertyOrder({ "electionEventId", "encryptionParameters" })
public class ControlComponentKeyGenerationRequestPayload {

	@JsonProperty
	private final String electionEventId;

	@JsonProperty
	private final GqGroup encryptionParameters;

	@JsonCreator
	public ControlComponentKeyGenerationRequestPayload(
			@JsonProperty("electionEventId")
			final String electionEventId,
			@JsonProperty("encryptionParameters")
			final GqGroup encryptionParameters) {

		this.electionEventId = checkNotNull(electionEventId);
		this.encryptionParameters = checkNotNull(encryptionParameters);
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public GqGroup getEncryptionParameters() {
		return encryptionParameters;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ControlComponentKeyGenerationRequestPayload that = (ControlComponentKeyGenerationRequestPayload) o;
		return electionEventId.equals(that.electionEventId) && encryptionParameters.equals(that.encryptionParameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(electionEventId, encryptionParameters);
	}
}
