/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.sendvote;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.PlaintextEqualityProof;
import ch.post.it.evoting.domain.ContextIds;

@JsonPropertyOrder({ "contextIds", "encryptedVote", "exponentiatedEncryptedVote", "encryptedPartialChoiceReturnCodes", "exponentiationProof",
		"plaintextEqualityProof" })
public class EncryptedVerifiableVote implements HashableList {

	@JsonProperty
	private final ContextIds contextIds;

	@JsonProperty
	private final ElGamalMultiRecipientCiphertext encryptedVote;

	@JsonProperty
	private final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote;

	@JsonProperty
	private final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes;

	@JsonProperty
	private final ExponentiationProof exponentiationProof;

	@JsonProperty
	private final PlaintextEqualityProof plaintextEqualityProof;

	@JsonCreator
	public EncryptedVerifiableVote(
			@JsonProperty("contextIds")
			final ContextIds contextIds,
			@JsonProperty("encryptedVote")
			final ElGamalMultiRecipientCiphertext encryptedVote,
			@JsonProperty("exponentiatedEncryptedVote")
			final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote,
			@JsonProperty("encryptedPartialChoiceReturnCodes")
			final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes,
			@JsonProperty("exponentiationProof")
			final ExponentiationProof exponentiationProof,
			@JsonProperty("plaintextEqualityProof")
			final PlaintextEqualityProof plaintextEqualityProof) {
		this.contextIds = checkNotNull(contextIds);
		this.encryptedVote = checkNotNull(encryptedVote);
		this.exponentiatedEncryptedVote = checkNotNull(exponentiatedEncryptedVote);
		this.encryptedPartialChoiceReturnCodes = checkNotNull(encryptedPartialChoiceReturnCodes);
		this.exponentiationProof = checkNotNull(exponentiationProof);
		this.plaintextEqualityProof = checkNotNull(plaintextEqualityProof);
	}

	public ContextIds getContextIds() {
		return contextIds;
	}

	public ElGamalMultiRecipientCiphertext getEncryptedVote() {
		return encryptedVote;
	}

	public ElGamalMultiRecipientCiphertext getExponentiatedEncryptedVote() {
		return exponentiatedEncryptedVote;
	}

	public ElGamalMultiRecipientCiphertext getEncryptedPartialChoiceReturnCodes() {
		return encryptedPartialChoiceReturnCodes;
	}

	public ExponentiationProof getExponentiationProof() {
		return exponentiationProof;
	}

	public PlaintextEqualityProof getPlaintextEqualityProof() {
		return plaintextEqualityProof;
	}

	@Override
	public ImmutableList<Hashable> toHashableForm() {
		return Streams.concat(
				contextIds.toHashableForm().stream(),
				Stream.of(encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes, exponentiationProof, plaintextEqualityProof)
		).collect(ImmutableList.toImmutableList());
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final EncryptedVerifiableVote that = (EncryptedVerifiableVote) o;
		return contextIds.equals(that.contextIds) && encryptedVote.equals(that.encryptedVote) && exponentiatedEncryptedVote.equals(
				that.exponentiatedEncryptedVote) && encryptedPartialChoiceReturnCodes.equals(that.encryptedPartialChoiceReturnCodes)
				&& exponentiationProof.equals(that.exponentiationProof) && plaintextEqualityProof.equals(that.plaintextEqualityProof);
	}

	@Override
	public int hashCode() {
		return Objects.hash(contextIds, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes, exponentiationProof,
				plaintextEqualityProof);
	}
}
