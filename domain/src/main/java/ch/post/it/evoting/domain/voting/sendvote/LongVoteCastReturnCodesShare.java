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

import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;

@JsonDeserialize(as = LongVoteCastReturnCodesShare.class)
@JsonPropertyOrder({ "correlationId", "electionEventId", "verificationCardSetId", "verificationCardId", "requestId", "isCastCode", "nodeId",
		"longVoteCastReturnCodeShare", "voterVoteCastReturnCodeGenerationPublicKey", "exponentiationProof" })
public class LongVoteCastReturnCodesShare extends LongReturnCodesShare {

	@JsonProperty
	private final GqElement longVoteCastReturnCodeShare;

	@JsonProperty
	private final GqElement voterVoteCastReturnCodeGenerationPublicKey;

	@JsonProperty
	private final ExponentiationProof exponentiationProof;

	@JsonCreator
	public LongVoteCastReturnCodesShare(
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
			@JsonProperty("longVoteCastReturnCodeShare")
			final GqElement longVoteCastReturnCodeShare,
			@JsonProperty("voterVoteCastReturnCodeGenerationPublicKey")
			final GqElement voterVoteCastReturnCodeGenerationPublicKey,
			@JsonProperty("exponentiationProof")
			final ExponentiationProof exponentiationProof) {

		super(checkNotNull(correlationId), checkNotNull(electionEventId), checkNotNull(verificationCardSetId), checkNotNull(verificationCardId),
				checkNotNull(requestId), true, nodeId);

		this.longVoteCastReturnCodeShare = checkNotNull(longVoteCastReturnCodeShare);
		this.voterVoteCastReturnCodeGenerationPublicKey = checkNotNull(voterVoteCastReturnCodeGenerationPublicKey);
		this.exponentiationProof = checkNotNull(exponentiationProof);
	}

	public GqElement getLongVoteCastReturnCodeShare() {
		return longVoteCastReturnCodeShare;
	}

	public GqElement getVoterVoteCastReturnCodeGenerationPublicKey() {
		return voterVoteCastReturnCodeGenerationPublicKey;
	}

	public ExponentiationProof getExponentiationProof() {
		return exponentiationProof;
	}

	@Override
	public ImmutableList<? extends Hashable> toHashableForm() {
		return ImmutableList.of(HashableString.from(getElectionEventId()), HashableString.from(getVerificationCardSetId()),
				HashableString.from(getVerificationCardId()), HashableString.from(getRequestId()), HashableString.from(integerToString(getNodeId())),
				longVoteCastReturnCodeShare, voterVoteCastReturnCodeGenerationPublicKey, exponentiationProof);
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
		final LongVoteCastReturnCodesShare that = (LongVoteCastReturnCodesShare) o;
		return longVoteCastReturnCodeShare.equals(that.longVoteCastReturnCodeShare) && voterVoteCastReturnCodeGenerationPublicKey
				.equals(that.voterVoteCastReturnCodeGenerationPublicKey) && exponentiationProof.equals(that.exponentiationProof);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), longVoteCastReturnCodeShare, voterVoteCastReturnCodeGenerationPublicKey, exponentiationProof);
	}
}
