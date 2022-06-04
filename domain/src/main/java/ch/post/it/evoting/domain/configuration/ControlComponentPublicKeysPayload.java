/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.configuration;

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

@JsonDeserialize(using = ControlComponentPublicKeysPayloadDeserializer.class)
@JsonPropertyOrder({ "encryptionGroup", "electionEventId", "controlComponentPublicKeys", "signature" })
public class ControlComponentPublicKeysPayload implements SignedPayload {

	@JsonProperty
	private final GqGroup encryptionGroup;

	@JsonProperty
	private final String electionEventId;

	@JsonProperty
	private final ControlComponentPublicKeys controlComponentPublicKeys;

	@JsonProperty
	private CryptoPrimitivesPayloadSignature signature;

	@JsonCreator
	public ControlComponentPublicKeysPayload(
			@JsonProperty("encryptionGroup")
			final GqGroup encryptionGroup,
			@JsonProperty("electionEventId")
			final String electionEventId,
			@JsonProperty("controlComponentPublicKeys")
			final ControlComponentPublicKeys controlComponentPublicKeys,
			@JsonProperty("signature")
			final CryptoPrimitivesPayloadSignature signature) {

		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.electionEventId = checkNotNull(electionEventId);
		this.controlComponentPublicKeys = checkNotNull(controlComponentPublicKeys);
		this.signature = checkNotNull(signature);
	}

	public ControlComponentPublicKeysPayload(final GqGroup encryptionGroup, final String electionEventId,
			final ControlComponentPublicKeys controlComponentPublicKeys) {
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.electionEventId = checkNotNull(electionEventId);
		this.controlComponentPublicKeys = checkNotNull(controlComponentPublicKeys);
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public ControlComponentPublicKeys getControlComponentPublicKeys() {
		return controlComponentPublicKeys;
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
	public ImmutableList<? extends Hashable> toHashableForm() {
		return ImmutableList.of(encryptionGroup, HashableString.from(electionEventId), controlComponentPublicKeys);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ControlComponentPublicKeysPayload that = (ControlComponentPublicKeysPayload) o;
		return encryptionGroup.equals(that.encryptionGroup) && electionEventId.equals(that.electionEventId) && controlComponentPublicKeys.equals(
				that.controlComponentPublicKeys) && Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(encryptionGroup, electionEventId, controlComponentPublicKeys, signature);
	}

}
