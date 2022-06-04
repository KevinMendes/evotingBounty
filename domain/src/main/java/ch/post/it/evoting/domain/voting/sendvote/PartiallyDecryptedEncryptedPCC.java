/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.sendvote;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.ExponentiationProofGroupVectorDeserializer;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.GqGroupVectorDeserializer;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.domain.ContextIds;

@JsonPropertyOrder({ "contextIds", "nodeId", "exponentiatedGammas", "exponentiationProofs" })
public class PartiallyDecryptedEncryptedPCC implements HashableList {

	@JsonProperty
	private final ContextIds contextIds;

	@JsonProperty
	private final int nodeId;

	@JsonProperty
	@JsonDeserialize(using = GqGroupVectorDeserializer.class)
	private final GroupVector<GqElement, GqGroup> exponentiatedGammas;

	@JsonProperty
	@JsonDeserialize(using = ExponentiationProofGroupVectorDeserializer.class)
	private final GroupVector<ExponentiationProof, ZqGroup> exponentiationProofs;

	@JsonCreator
	public PartiallyDecryptedEncryptedPCC(
			@JsonProperty("contextIds")
			final ContextIds contextIds,
			@JsonProperty("nodeId")
			final int nodeId,
			@JsonProperty("exponentiatedGammas")
			final GroupVector<GqElement, GqGroup> exponentiatedGamma,
			@JsonProperty("exponentiationProofs")
			final GroupVector<ExponentiationProof, ZqGroup> exponentiationProofs) {
		this.contextIds = checkNotNull(contextIds);
		this.nodeId = nodeId;
		this.exponentiatedGammas = checkNotNull(exponentiatedGamma);
		this.exponentiationProofs = checkNotNull(exponentiationProofs);
	}

	public ContextIds getContextIds() {
		return contextIds;
	}

	public int getNodeId() {
		return nodeId;
	}

	public GroupVector<GqElement, GqGroup> getExponentiatedGammas() {
		return exponentiatedGammas;
	}

	public GroupVector<ExponentiationProof, ZqGroup> getExponentiationProofs() {
		return exponentiationProofs;
	}

	@Override
	public ImmutableList<Hashable> toHashableForm() {
		return ImmutableList.of(contextIds, HashableBigInteger.from(BigInteger.valueOf(nodeId)), exponentiatedGammas, exponentiationProofs);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PartiallyDecryptedEncryptedPCC that = (PartiallyDecryptedEncryptedPCC) o;
		return nodeId == that.nodeId && contextIds.equals(that.contextIds) && exponentiatedGammas.equals(that.exponentiatedGammas)
				&& exponentiationProofs.equals(that.exponentiationProofs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(contextIds, nodeId, exponentiatedGammas, exponentiationProofs);
	}
}
