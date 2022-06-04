/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.confirmvote;

import static com.google.common.base.Preconditions.checkArgument;
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

@JsonPropertyOrder({ "encryptionGroup", "confirmationKey", "requestId", "signature" })
@JsonDeserialize(using = ConfirmationKeyPayloadDeserializer.class)
public class ConfirmationKeyPayload implements SignedPayload {

	@JsonProperty
	private final GqGroup encryptionGroup;
	@JsonProperty
	private final ConfirmationKey confirmationKey;
	@JsonProperty
	private final String requestId;
	@JsonProperty
	private CryptoPrimitivesPayloadSignature signature;

	@JsonCreator
	public ConfirmationKeyPayload(
			@JsonProperty("encryptionGroup")
			final GqGroup encryptionGroup,
			@JsonProperty("confirmationKey")
			final ConfirmationKey confirmationKey,
			@JsonProperty("requestId")
			final String requestId,
			@JsonProperty("signature")
			final CryptoPrimitivesPayloadSignature signature) {
		checkNotNull(encryptionGroup);
		checkNotNull(confirmationKey);
		checkNotNull(requestId);
		checkNotNull(signature);

		checkArgument(encryptionGroup.equals(confirmationKey.getElement().getGroup()), "The confirmation key must be in the encryption group");

		this.encryptionGroup = encryptionGroup;
		this.confirmationKey = confirmationKey;
		this.requestId = requestId;
		this.signature = signature;
	}

	public ConfirmationKeyPayload(final GqGroup encryptionGroup, final ConfirmationKey confirmationKey, final String requestId) {
		checkNotNull(encryptionGroup);
		checkNotNull(confirmationKey);
		checkNotNull(requestId);

		checkArgument(encryptionGroup.equals(confirmationKey.getElement().getGroup()), "The confirmation key must be in the encryption group");

		this.encryptionGroup = encryptionGroup;
		this.confirmationKey = confirmationKey;
		this.requestId = requestId;
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	public ConfirmationKey getConfirmationKey() {
		return confirmationKey;
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
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ConfirmationKeyPayload that = (ConfirmationKeyPayload) o;
		return encryptionGroup.equals(that.encryptionGroup) && confirmationKey.equals(that.confirmationKey) && requestId.equals(that.requestId)
				&& Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(encryptionGroup, confirmationKey, requestId, signature);
	}

	@Override
	public ImmutableList<? extends Hashable> toHashableForm() {
		return ImmutableList.of(encryptionGroup, confirmationKey, HashableString.from(requestId));
	}
}
