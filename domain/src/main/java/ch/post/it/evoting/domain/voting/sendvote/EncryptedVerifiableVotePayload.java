/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.sendvote;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.SignedPayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@JsonDeserialize(using = EncryptedVerifiableVotePayloadDeserializer.class)
@JsonPropertyOrder({ "encryptionGroup", "encryptedVerifiableVote", "requestId", "signature" })
public class EncryptedVerifiableVotePayload implements SignedPayload {

	@JsonProperty
	private final GqGroup encryptionGroup;

	@JsonProperty
	private final EncryptedVerifiableVote encryptedVerifiableVote;

	@JsonProperty
	private final String requestId;

	@JsonProperty
	private CryptoPrimitivesPayloadSignature signature;

	@JsonCreator
	public EncryptedVerifiableVotePayload(
			@JsonProperty("encryptionGroup")
			final GqGroup encryptionGroup,
			@JsonProperty("encryptedVerifiableVote")
			final EncryptedVerifiableVote encryptedVerifiableVote,
			@JsonProperty("requestId")
			final String requestId,
			@JsonProperty("signature")
			final CryptoPrimitivesPayloadSignature signature) {
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.encryptedVerifiableVote = checkNotNull(encryptedVerifiableVote);
		this.requestId = checkNotNull(requestId);
		this.signature = checkNotNull(signature);
	}

	/**
	 * Creates an unsigned payload.
	 */
	public EncryptedVerifiableVotePayload(final GqGroup encryptionGroup, final EncryptedVerifiableVote encryptedVerifiableVote,
			final String requestId) {
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.encryptedVerifiableVote = checkNotNull(encryptedVerifiableVote);
		this.requestId = checkNotNull(requestId);
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	public EncryptedVerifiableVote getEncryptedVerifiableVote() {
		return encryptedVerifiableVote;
	}

	public String getRequestId() {
		return requestId;
	}

	public CryptoPrimitivesPayloadSignature getSignature() {
		return signature;
	}

	public void setSignature(final CryptoPrimitivesPayloadSignature signature) {
		this.signature = checkNotNull(signature);
	}

	@Override
	public ImmutableList<Hashable> toHashableForm() {
		return ImmutableList.of(encryptionGroup, encryptedVerifiableVote, HashableString.from(requestId));
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final EncryptedVerifiableVotePayload that = (EncryptedVerifiableVotePayload) o;
		return encryptionGroup.equals(that.encryptionGroup) && encryptedVerifiableVote.equals(that.encryptedVerifiableVote) && requestId.equals(
				that.requestId) && Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(encryptionGroup, encryptedVerifiableVote, requestId, signature);
	}
}
