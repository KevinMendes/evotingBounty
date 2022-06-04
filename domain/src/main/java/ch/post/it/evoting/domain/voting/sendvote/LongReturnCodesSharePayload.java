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
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@JsonDeserialize(using = LongReturnCodesSharePayloadDeserializer.class)
@JsonPropertyOrder({ "encryptionGroup", "longReturnCodesShare", "signature" })
public class LongReturnCodesSharePayload implements SignedPayload {

	@JsonProperty
	private final GqGroup encryptionGroup;

	@JsonProperty
	private final LongReturnCodesShare longReturnCodesShare;

	@JsonProperty
	private CryptoPrimitivesPayloadSignature signature;

	@JsonCreator
	public LongReturnCodesSharePayload(
			@JsonProperty("encryptionGroup")
			final GqGroup encryptionGroup,
			@JsonProperty("longReturnCodesShare")
			final LongReturnCodesShare longReturnCodesShare,
			@JsonProperty("signature")
			final CryptoPrimitivesPayloadSignature signature) {

		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.longReturnCodesShare = checkNotNull(longReturnCodesShare);
		this.signature = checkNotNull(signature);
	}

	/**
	 * Creates an unsigned payload.
	 */
	public LongReturnCodesSharePayload(final GqGroup encryptionGroup, final LongReturnCodesShare longReturnCodesShare) {
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.longReturnCodesShare = checkNotNull(longReturnCodesShare);
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	public LongReturnCodesShare getLongReturnCodesShare() {
		return longReturnCodesShare;
	}

	@Override
	public CryptoPrimitivesPayloadSignature getSignature() {
		return signature;
	}

	@Override
	public void setSignature(final CryptoPrimitivesPayloadSignature signature) {
		this.signature = checkNotNull(signature);
	}

	@Override
	public ImmutableList<Hashable> toHashableForm() {
		return ImmutableList.of(encryptionGroup, longReturnCodesShare);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final LongReturnCodesSharePayload that = (LongReturnCodesSharePayload) o;
		return encryptionGroup.equals(that.encryptionGroup) && longReturnCodesShare.equals(that.longReturnCodesShare) && Objects
				.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(encryptionGroup, longReturnCodesShare, signature);
	}
}
