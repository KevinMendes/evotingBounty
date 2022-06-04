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

@JsonDeserialize(using = PartiallyDecryptedEncryptedPCCPayloadDeserializer.class)
@JsonPropertyOrder({ "encryptionGroup", "partiallyDecryptedEncryptedPCC", "requestId", "signature" })
public class PartiallyDecryptedEncryptedPCCPayload implements SignedPayload {

	@JsonProperty
	private final GqGroup encryptionGroup;

	@JsonProperty
	private final PartiallyDecryptedEncryptedPCC partiallyDecryptedEncryptedPCC;

	@JsonProperty
	private final String requestId;

	@JsonProperty
	private CryptoPrimitivesPayloadSignature signature;

	@JsonCreator
	public PartiallyDecryptedEncryptedPCCPayload(
			@JsonProperty("encryptionGroup")
			final GqGroup encryptionGroup,
			@JsonProperty("partiallyDecryptedEncryptedPCC")
			final PartiallyDecryptedEncryptedPCC partiallyDecryptedEncryptedPCC,
			@JsonProperty("requestId")
			final String requestId,
			@JsonProperty("signature")
			final CryptoPrimitivesPayloadSignature signature) {
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.partiallyDecryptedEncryptedPCC = checkNotNull(partiallyDecryptedEncryptedPCC);
		this.requestId = checkNotNull(requestId);
		this.signature = checkNotNull(signature);
	}

	/**
	 * Creates an unsigned payload.
	 */
	public PartiallyDecryptedEncryptedPCCPayload(final GqGroup encryptionGroup, final PartiallyDecryptedEncryptedPCC partiallyDecryptedEncryptedPCC,
			final String requestId) {
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.partiallyDecryptedEncryptedPCC = checkNotNull(partiallyDecryptedEncryptedPCC);
		this.requestId = checkNotNull(requestId);
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	public PartiallyDecryptedEncryptedPCC getPartiallyDecryptedEncryptedPCC() {
		return partiallyDecryptedEncryptedPCC;
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
		return ImmutableList.of(encryptionGroup, partiallyDecryptedEncryptedPCC, HashableString.from(requestId));
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PartiallyDecryptedEncryptedPCCPayload that = (PartiallyDecryptedEncryptedPCCPayload) o;
		return encryptionGroup.equals(that.encryptionGroup) && partiallyDecryptedEncryptedPCC.equals(that.partiallyDecryptedEncryptedPCC)
				&& requestId.equals(that.requestId) && Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(encryptionGroup, partiallyDecryptedEncryptedPCC, requestId, signature);
	}
}
