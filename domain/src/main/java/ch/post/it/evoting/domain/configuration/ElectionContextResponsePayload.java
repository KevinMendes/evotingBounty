/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.configuration;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.SignedPayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;

@JsonDeserialize(using = ElectionContextResponsePayloadDeserializer.class)
@JsonPropertyOrder({ "nodeId", "electionEventId", "signature" })
public class ElectionContextResponsePayload implements SignedPayload {

	@JsonProperty
	private final int nodeId;

	@JsonProperty
	private final String electionEventId;

	@JsonProperty
	private CryptoPrimitivesPayloadSignature signature;

	@JsonCreator
	public ElectionContextResponsePayload(
			@JsonProperty("nodeId")
					int nodeId,
			@JsonProperty("electionEventId")
					String electionEventId,
			@JsonProperty("signature")
					CryptoPrimitivesPayloadSignature signature
	) {
		validateUUID(electionEventId);

		this.nodeId = nodeId;
		this.electionEventId = checkNotNull(electionEventId);
		this.signature = checkNotNull(signature);
	}

	public ElectionContextResponsePayload(int nodeId, String electionEventId) {
		validateUUID(electionEventId);

		this.nodeId = nodeId;
		this.electionEventId = checkNotNull(electionEventId);
	}

	public int getNodeId() {
		return nodeId;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	@Override
	public CryptoPrimitivesPayloadSignature getSignature() {
		return signature;
	}

	@Override
	public void setSignature(CryptoPrimitivesPayloadSignature signature) {
		this.signature = checkNotNull(signature);
	}

	@Override
	public ImmutableList<? extends Hashable> toHashableForm() {
		return ImmutableList.of(HashableBigInteger.from(BigInteger.valueOf(nodeId)), HashableString.from(electionEventId));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ElectionContextResponsePayload that = (ElectionContextResponsePayload) o;
		return nodeId == that.nodeId && electionEventId.equals(that.electionEventId) && Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nodeId, electionEventId, signature);
	}
}