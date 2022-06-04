/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.domain.configuration;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = ReturnCodesMappingTablePayload.Builder.class)
@JsonPropertyOrder({ "electionEventId", "verificationCardSetId", "returnCodesMappingTable" })
public class ReturnCodesMappingTablePayload {

	private final String electionEventId;
	private final String verificationCardSetId;
	private final Map<String, String> returnCodesMappingTable;

	private ReturnCodesMappingTablePayload(final String electionEventId, final String verificationCardSetId,
			final Map<String, String> returnCodesMappingTable) {
		this.electionEventId = validateUUID(electionEventId);
		this.verificationCardSetId = validateUUID(verificationCardSetId);
		this.returnCodesMappingTable = checkNotNull(returnCodesMappingTable);
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public Map<String, String> getReturnCodesMappingTable() {
		return returnCodesMappingTable;
	}

	@JsonPOJOBuilder(withPrefix = "set")
	public static class Builder {

		private String electionEventId;
		private String verificationCardSetId;
		private Map<String, String> returnCodesMappingTable;

		@JsonProperty("electionEventId")
		public Builder setElectionEventId(final String electionEventId) {
			this.electionEventId = electionEventId;
			return this;
		}

		@JsonProperty("verificationCardSetId")
		public Builder setVerificationCardSetId(final String verificationCardSetId) {
			this.verificationCardSetId = verificationCardSetId;
			return this;
		}

		@JsonProperty("returnCodesMappingTable")
		public Builder setReturnCodesMappingTable(final Map<String, String> returnCodesMappingTable) {
			this.returnCodesMappingTable = returnCodesMappingTable;
			return this;
		}

		public ReturnCodesMappingTablePayload build() {
			return new ReturnCodesMappingTablePayload(electionEventId, verificationCardSetId, returnCodesMappingTable);
		}
	}
}
