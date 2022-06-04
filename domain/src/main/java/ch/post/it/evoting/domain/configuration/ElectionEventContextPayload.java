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
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@JsonDeserialize(using = ElectionEventContextPayloadDeserializer.class)
@JsonPropertyOrder({ "encryptionGroup", "electionEventContext", "signature" })
public class ElectionEventContextPayload implements SignedPayload {

	@JsonProperty
	private final GqGroup encryptionGroup;

	@JsonProperty
	private final ElectionEventContext electionEventContext;

	@JsonProperty
	private CryptoPrimitivesPayloadSignature signature;

	@JsonCreator
	public ElectionEventContextPayload(
			@JsonProperty("encryptionGroup")
					GqGroup encryptionGroup,

			@JsonProperty("electionEventContext")
					ElectionEventContext electionEventContext,

			@JsonProperty("signature")
					CryptoPrimitivesPayloadSignature signature
	) {

		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.electionEventContext = checkNotNull(electionEventContext);
		this.signature = checkNotNull(signature);
	}

	public ElectionEventContextPayload(GqGroup encryptionGroup, ElectionEventContext electionEventContext) {
		this.encryptionGroup = checkNotNull(encryptionGroup);
		this.electionEventContext = checkNotNull(electionEventContext);
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}

	public ElectionEventContext getElectionEventContext() {
		return electionEventContext;
	}

	public CryptoPrimitivesPayloadSignature getSignature() {
		return signature;
	}

	public void setSignature(CryptoPrimitivesPayloadSignature signature) {
		this.signature = checkNotNull(signature);
	}

	@Override
	public ImmutableList<? extends Hashable> toHashableForm() {
		return ImmutableList.of(encryptionGroup, electionEventContext);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ElectionEventContextPayload that = (ElectionEventContextPayload) o;
		return encryptionGroup.equals(that.encryptionGroup) && electionEventContext.equals(that.electionEventContext)
				&& Objects.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(encryptionGroup, electionEventContext, signature);
	}
}
