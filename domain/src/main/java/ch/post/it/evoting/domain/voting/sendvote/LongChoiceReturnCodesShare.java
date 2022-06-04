/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.sendvote;

import static ch.post.it.evoting.cryptoprimitives.utils.ConversionService.integerToString;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.GqGroupVectorDeserializer;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;

@JsonDeserialize(as = LongChoiceReturnCodesShare.class)
@JsonPropertyOrder({ "correlationId", "electionEventId", "verificationCardSetId", "verificationCardId", "requestId", "isCastCode", "nodeId",
		"longChoiceReturnCodeShare", "voterChoiceReturnCodeGenerationPublicKey", "exponentiationProof" })
public class LongChoiceReturnCodesShare extends LongReturnCodesShare {

	@JsonProperty
	@JsonDeserialize(using = GqGroupVectorDeserializer.class)
	private final GroupVector<GqElement, GqGroup> longChoiceReturnCodeShare;

	@JsonProperty
	private final GqElement voterChoiceReturnCodeGenerationPublicKey;

	@JsonProperty
	private final ExponentiationProof exponentiationProof;

	@JsonCreator
	public LongChoiceReturnCodesShare(
			@JsonProperty("correlationId")
			final UUID correlationId,
			@JsonProperty("electionEventId")
			final String electionEventId,
			@JsonProperty("verificationCardSetId")
			final String verificationCardSetId,
			@JsonProperty("verificationCardId")
			final String verificationCardId,
			@JsonProperty("requestId")
			final String requestId,
			@JsonProperty(value = "nodeId")
			final int nodeId,
			@JsonProperty("longChoiceReturnCodeShare")
			final GroupVector<GqElement, GqGroup> longChoiceReturnCodeShare,
			@JsonProperty("voterChoiceReturnCodeGenerationPublicKey")
			final GqElement voterChoiceReturnCodeGenerationPublicKey,
			@JsonProperty("exponentiationProof")
			final ExponentiationProof exponentiationProof) {

		super(checkNotNull(correlationId), checkNotNull(electionEventId), checkNotNull(verificationCardSetId), checkNotNull(verificationCardId),
				checkNotNull(requestId), false, nodeId);
		this.longChoiceReturnCodeShare = checkNotNull(longChoiceReturnCodeShare);
		this.voterChoiceReturnCodeGenerationPublicKey = checkNotNull(voterChoiceReturnCodeGenerationPublicKey);
		this.exponentiationProof = checkNotNull(exponentiationProof);
	}

	public GroupVector<GqElement, GqGroup> getLongChoiceReturnCodeShare() {
		return longChoiceReturnCodeShare;
	}

	public GqElement getVoterChoiceReturnCodeGenerationPublicKey() {
		return voterChoiceReturnCodeGenerationPublicKey;
	}

	public ExponentiationProof getExponentiationProof() {
		return exponentiationProof;
	}

	@Override
	public ImmutableList<Hashable> toHashableForm() {
		return ImmutableList.of(HashableString.from(getElectionEventId()), HashableString.from(getVerificationCardSetId()),
				HashableString.from(getVerificationCardId()), HashableString.from(getRequestId()), HashableString.from(integerToString(getNodeId())),
				longChoiceReturnCodeShare, voterChoiceReturnCodeGenerationPublicKey, exponentiationProof);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		final LongChoiceReturnCodesShare payload = (LongChoiceReturnCodesShare) o;
		return longChoiceReturnCodeShare.equals(payload.longChoiceReturnCodeShare) && voterChoiceReturnCodeGenerationPublicKey
				.equals(payload.voterChoiceReturnCodeGenerationPublicKey) && exponentiationProof.equals(payload.exponentiationProof);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), longChoiceReturnCodeShare, voterChoiceReturnCodeGenerationPublicKey, exponentiationProof);
	}
}
