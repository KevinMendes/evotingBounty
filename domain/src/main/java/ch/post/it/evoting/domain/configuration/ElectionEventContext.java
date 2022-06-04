/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.configuration;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;

@JsonPropertyOrder({ "electionEventId", "verificationCardSetContexts", "combinedControlComponentPublicKeys", "electoralBoardPublicKey",
		"electionPublicKey", "choiceReturnCodesEncryptionPublicKey", "startTime", "finishTime" })
public class ElectionEventContext implements HashableList {

	@JsonProperty
	private final String electionEventId;

	@JsonProperty
	private final List<VerificationCardSetContext> verificationCardSetContexts;

	@JsonProperty
	private final List<ControlComponentPublicKeys> combinedControlComponentPublicKeys;

	@JsonProperty
	private final ElGamalMultiRecipientPublicKey electoralBoardPublicKey;

	@JsonProperty
	private final ElGamalMultiRecipientPublicKey electionPublicKey;

	@JsonProperty
	private final ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey;

	@JsonProperty
	private final LocalDateTime startTime;

	@JsonProperty
	private final LocalDateTime finishTime;

	private final int maxNumberOfWriteInFields;

	@JsonCreator
	public ElectionEventContext(
			@JsonProperty("electionEventId")
			final String electionEventId,
			@JsonProperty("verificationCardSetContexts")
			final List<VerificationCardSetContext> verificationCardSetContexts,
			@JsonProperty("combinedControlComponentPublicKeys")
			final List<ControlComponentPublicKeys> combinedControlComponentPublicKeys,
			@JsonProperty("electoralBoardPublicKey")
			final ElGamalMultiRecipientPublicKey electoralBoardPublicKey,
			@JsonProperty("electionPublicKey")
			final ElGamalMultiRecipientPublicKey electionPublicKey,
			@JsonProperty("choiceReturnCodesEncryptionPublicKey")
			final ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey,
			@JsonProperty("startTime")
			final LocalDateTime startTime,
			@JsonProperty("finishTime")
			final LocalDateTime finishTime

	) {
		checkNotNull(electoralBoardPublicKey);
		checkNotNull(electionPublicKey);
		checkNotNull(choiceReturnCodesEncryptionPublicKey);
		checkNotNull(verificationCardSetContexts);
		checkNotNull(combinedControlComponentPublicKeys);
		checkNotNull(startTime);
		checkNotNull(finishTime);

		validateUUID(electionEventId);

		final int verificationCardSetContextsSize = verificationCardSetContexts.size();
		checkArgument(verificationCardSetContextsSize > 0, "VerificationCardSetContexts cannot be empty.");
		checkArgument(verificationCardSetContexts.stream().map(VerificationCardSetContext::getBallotBoxId).distinct().count()
				== verificationCardSetContextsSize, "VerificationCardSetContexts cannot contain duplicate BallotBoxIds.");
		checkArgument(verificationCardSetContexts.stream().map(VerificationCardSetContext::getVerificationCardSetId).distinct().count()
				== verificationCardSetContextsSize, "VerificationCardSetContexts cannot contain duplicate VerificationCardSetIds.");
		checkArgument(verificationCardSetContexts.stream().map(VerificationCardSetContext::getNumberOfWriteInFields).allMatch(n -> n >= 0),
				"VerificationCardSetContexts cannot contain negative numberOfWriteInFields.");

		final int combinedControlComponentPublicKeysSize = combinedControlComponentPublicKeys.size();
		checkArgument(combinedControlComponentPublicKeysSize > 0, "CombinedControlComponentPublicKeys cannot be empty.");
		checkArgument(combinedControlComponentPublicKeysSize == 4,
				"CombinedControlComponentPublicKeys must contain four ControlComponentPublicKeys.");
		checkArgument(combinedControlComponentPublicKeys.stream().map(ControlComponentPublicKeys::getNodeId).distinct().count() == 4,
				"CombinedControlComponentPublicKeys must contain a key for each node [1..4].");

		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccrChoiceReturnCodePublicKeys = combinedControlComponentPublicKeys.stream()
				.map(ControlComponentPublicKeys::getCcrChoiceReturnCodesEncryptionPublicKey).collect(GroupVector.toGroupVector());

		final ElGamalService elGamalService = new ElGamalService();
		final ElGamalMultiRecipientPublicKey combinedCCrChoiceReturnCodesPublicKeys = elGamalService.combinePublicKeys(ccrChoiceReturnCodePublicKeys);

		checkArgument(choiceReturnCodesEncryptionPublicKey.equals(combinedCCrChoiceReturnCodesPublicKeys),
				"Multiplication of the ccrChoiceReturnCodesPublicKeys must equal the choiceReturnCodesPublicKey");

		this.maxNumberOfWriteInFields = verificationCardSetContexts.stream()
				.max(Comparator.comparingInt(VerificationCardSetContext::getNumberOfWriteInFields)).orElseThrow(IllegalArgumentException::new)
				.getNumberOfWriteInFields();

		checkArgument(electoralBoardPublicKey.size() == (maxNumberOfWriteInFields + 1),
				"The size of the electoralBoardPublicKey must equal the maximum number of write-ins fields in all verification card sets + 1");

		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> publicKeys = Streams.concat(
				combinedControlComponentPublicKeys.stream()
						.map(ControlComponentPublicKeys::getCcmElectionPublicKey)
						.filter(ccmElectionPublicKey -> ccmElectionPublicKey.size() >= maxNumberOfWriteInFields + 1)
						.map(ccmElectionPublicKey ->
								new ElGamalMultiRecipientPublicKey(ccmElectionPublicKey.getKeyElements().subList(0, maxNumberOfWriteInFields + 1))),
				Stream.of(electoralBoardPublicKey)).collect(GroupVector.toGroupVector());

		checkArgument(electionPublicKey.equals(elGamalService.combinePublicKeys(publicKeys)),
				"Multiplication of the ccmElectionPublicKeys times the electoralBoardPublicKey must equal the electionPublicKey");

		this.electionEventId = electionEventId;
		this.verificationCardSetContexts = verificationCardSetContexts;
		this.combinedControlComponentPublicKeys = combinedControlComponentPublicKeys;
		this.electoralBoardPublicKey = electoralBoardPublicKey;
		this.electionPublicKey = electionPublicKey;
		this.choiceReturnCodesEncryptionPublicKey = choiceReturnCodesEncryptionPublicKey;
		this.startTime = startTime;
		this.finishTime = finishTime;
	}

	/**
	 * @return the maximum number of write-ins fields in all verification card sets.
	 */
	@JsonIgnore
	public int getMaxNumberOfWriteInFields() {
		return maxNumberOfWriteInFields;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public List<VerificationCardSetContext> getVerificationCardSetContexts() {
		return verificationCardSetContexts;
	}

	public List<ControlComponentPublicKeys> getCombinedControlComponentPublicKeys() {
		return combinedControlComponentPublicKeys;
	}

	public ElGamalMultiRecipientPublicKey getElectoralBoardPublicKey() {
		return electoralBoardPublicKey;
	}

	public ElGamalMultiRecipientPublicKey getElectionPublicKey() {
		return electionPublicKey;
	}

	public ElGamalMultiRecipientPublicKey getChoiceReturnCodesEncryptionPublicKey() {
		return choiceReturnCodesEncryptionPublicKey;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getFinishTime() {
		return finishTime;
	}

	@Override
	public ImmutableList<? extends Hashable> toHashableForm() {
		return ImmutableList.of(HashableString.from(electionEventId),
				HashableList.from(verificationCardSetContexts),
				HashableList.from(combinedControlComponentPublicKeys),
				electoralBoardPublicKey,
				electionPublicKey,
				choiceReturnCodesEncryptionPublicKey,
				HashableString.from(startTime.toString()),
				HashableString.from(finishTime.toString()));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ElectionEventContext that = (ElectionEventContext) o;
		return electionEventId.equals(that.electionEventId) && verificationCardSetContexts.equals(that.verificationCardSetContexts)
				&& combinedControlComponentPublicKeys.equals(that.combinedControlComponentPublicKeys) && electoralBoardPublicKey.equals(
				that.electoralBoardPublicKey) && electionPublicKey.equals(that.electionPublicKey) && choiceReturnCodesEncryptionPublicKey.equals(
				that.choiceReturnCodesEncryptionPublicKey) && startTime.equals(that.startTime) && finishTime.equals(that.finishTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(electionEventId, verificationCardSetContexts, combinedControlComponentPublicKeys, electoralBoardPublicKey,
				electionPublicKey, choiceReturnCodesEncryptionPublicKey, startTime, finishTime);
	}
}
