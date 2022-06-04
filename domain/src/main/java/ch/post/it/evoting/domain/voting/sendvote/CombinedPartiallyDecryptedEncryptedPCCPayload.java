/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.sendvote;

import static ch.post.it.evoting.cryptoprimitives.utils.Validations.allEqual;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.SignedPayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;

@JsonPropertyOrder({ "partiallyDecryptedEncryptedPCCPayloads", "signature" })
public class CombinedPartiallyDecryptedEncryptedPCCPayload implements SignedPayload {

	@JsonProperty
	private List<PartiallyDecryptedEncryptedPCCPayload> partiallyDecryptedEncryptedPCCPayloads;

	@JsonProperty
	private CryptoPrimitivesPayloadSignature signature;

	@JsonCreator
	public CombinedPartiallyDecryptedEncryptedPCCPayload(
			@JsonProperty("partiallyDecryptedEncryptedPCCPayloads")
			final List<PartiallyDecryptedEncryptedPCCPayload> partiallyDecryptedEncryptedPCCPayloads,
			@JsonProperty("signature")
			final CryptoPrimitivesPayloadSignature signature) {

		checkNotNull(partiallyDecryptedEncryptedPCCPayloads);
		checkNotNull(signature);
		checkConsistency(partiallyDecryptedEncryptedPCCPayloads);

		this.partiallyDecryptedEncryptedPCCPayloads = partiallyDecryptedEncryptedPCCPayloads;
		this.signature = signature;
	}

	/**
	 * Creates an unsigned payload.
	 */
	public CombinedPartiallyDecryptedEncryptedPCCPayload(final List<PartiallyDecryptedEncryptedPCCPayload> partiallyDecryptedEncryptedPCCPayloads) {
		checkNotNull(partiallyDecryptedEncryptedPCCPayloads);
		checkConsistency(partiallyDecryptedEncryptedPCCPayloads);

		this.partiallyDecryptedEncryptedPCCPayloads = partiallyDecryptedEncryptedPCCPayloads;
	}

	public List<PartiallyDecryptedEncryptedPCCPayload> getPartiallyDecryptedEncryptedPCCPayloads() {
		return partiallyDecryptedEncryptedPCCPayloads;
	}

	public CryptoPrimitivesPayloadSignature getSignature() {
		return signature;
	}

	public void setSignature(final CryptoPrimitivesPayloadSignature signature) {
		this.signature = checkNotNull(signature);
	}

	@Override
	public ImmutableList<Hashable> toHashableForm() {
		return partiallyDecryptedEncryptedPCCPayloads.stream()
				.map(payload -> HashableList.from(payload.toHashableForm()))
				.collect(ImmutableList.toImmutableList());
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CombinedPartiallyDecryptedEncryptedPCCPayload that = (CombinedPartiallyDecryptedEncryptedPCCPayload) o;
		return partiallyDecryptedEncryptedPCCPayloads.equals(that.partiallyDecryptedEncryptedPCCPayloads) && Objects.equals(signature,
				that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(partiallyDecryptedEncryptedPCCPayloads, signature);
	}

	private void checkConsistency(final List<PartiallyDecryptedEncryptedPCCPayload> partiallyDecryptedEncryptedPCCPayloads) {
		checkArgument(partiallyDecryptedEncryptedPCCPayloads.size() == 4, "There must be contributions from 4 nodes.");
		checkArgument(allEqual(partiallyDecryptedEncryptedPCCPayloads.stream(), PartiallyDecryptedEncryptedPCCPayload::getEncryptionGroup),
				"The contribution payloads do not have all the same group.");
		checkArgument(
				allEqual(partiallyDecryptedEncryptedPCCPayloads.stream(), payload -> payload.getPartiallyDecryptedEncryptedPCC().getContextIds()),
				"The contribution payloads do not have all the same contextIds.");

		final List<Integer> nodeIds = partiallyDecryptedEncryptedPCCPayloads.stream()
				.map(payload -> payload.getPartiallyDecryptedEncryptedPCC().getNodeId())
				.distinct()
				.collect(Collectors.toList());
		checkArgument(nodeIds.size() == 4, "There must be 4 different node ids.");
	}

}
