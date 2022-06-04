/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.configuration;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;

@JsonPropertyOrder({ "nodeId", "ccrChoiceReturnCodesEncryptionPublicKey", "ccmElectionPublicKey" })
public class ControlComponentPublicKeys implements HashableList {

	@JsonProperty
	private final int nodeId;

	@JsonProperty
	private final ElGamalMultiRecipientPublicKey ccrChoiceReturnCodesEncryptionPublicKey;

	@JsonProperty
	private final ElGamalMultiRecipientPublicKey ccmElectionPublicKey;

	@JsonCreator
	public ControlComponentPublicKeys(
			@JsonProperty("nodeId")
			final int nodeId,
			@JsonProperty("ccrChoiceReturnCodesEncryptionPublicKey")
			final ElGamalMultiRecipientPublicKey ccrChoiceReturnCodesEncryptionPublicKey,
			@JsonProperty("ccmElectionPublicKey")
			final ElGamalMultiRecipientPublicKey ccmElectionPublicKey) {

		checkArgument(nodeId >= 1 && nodeId <= 4, "Node Id's should have a value in the range 1 to 4");
		this.nodeId = nodeId;
		this.ccrChoiceReturnCodesEncryptionPublicKey = checkNotNull(ccrChoiceReturnCodesEncryptionPublicKey);
		this.ccmElectionPublicKey = checkNotNull(ccmElectionPublicKey);
	}

	public int getNodeId() {
		return nodeId;
	}

	public ElGamalMultiRecipientPublicKey getCcrChoiceReturnCodesEncryptionPublicKey() {
		return ccrChoiceReturnCodesEncryptionPublicKey;
	}

	public ElGamalMultiRecipientPublicKey getCcmElectionPublicKey() {
		return ccmElectionPublicKey;
	}

	@Override
	public ImmutableList<? extends Hashable> toHashableForm() {
		return ImmutableList.of(HashableBigInteger.from(BigInteger.valueOf(nodeId)), ccrChoiceReturnCodesEncryptionPublicKey, ccmElectionPublicKey);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ControlComponentPublicKeys that = (ControlComponentPublicKeys) o;
		return nodeId == that.nodeId && ccrChoiceReturnCodesEncryptionPublicKey.equals(that.ccrChoiceReturnCodesEncryptionPublicKey)
				&& ccmElectionPublicKey.equals(that.ccmElectionPublicKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nodeId, ccrChoiceReturnCodesEncryptionPublicKey, ccmElectionPublicKey);
	}
}
