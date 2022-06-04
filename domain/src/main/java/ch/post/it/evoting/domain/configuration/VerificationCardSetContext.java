/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.configuration;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableBigInteger;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;

@JsonPropertyOrder({ "verificationCardSetId", "ballotBoxId", "testBallotBox", "numberOfWriteInFields" })
public class VerificationCardSetContext implements HashableList {

	@JsonProperty
	private final String verificationCardSetId;

	@JsonProperty
	private final String ballotBoxId;

	@JsonProperty
	private final boolean testBallotBox;

	@JsonProperty
	private final int numberOfWriteInFields;

	@JsonCreator
	public VerificationCardSetContext(

			@JsonProperty("verificationCardSetId")
					String verificationCardSetId,

			@JsonProperty("ballotBoxId")
					String ballotBoxId,

			@JsonProperty("testBallotBox")
					boolean testBallotBox,

			@JsonProperty("numberOfWriteInFields")
					int numberOfWriteInFields) {

		this.verificationCardSetId = checkNotNull(verificationCardSetId);
		this.ballotBoxId = checkNotNull(ballotBoxId);
		this.testBallotBox = testBallotBox;
		this.numberOfWriteInFields = numberOfWriteInFields;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public boolean getTestBallotBox() {
		return testBallotBox;
	}

	public Integer getNumberOfWriteInFields() {
		return numberOfWriteInFields;
	}

	@Override
	public ImmutableList<? extends Hashable> toHashableForm() {
		return ImmutableList.of(HashableString.from(verificationCardSetId), HashableString.from(ballotBoxId),
				HashableString.from(String.valueOf(testBallotBox)), HashableBigInteger.from(BigInteger.valueOf(numberOfWriteInFields)));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		VerificationCardSetContext that = (VerificationCardSetContext) o;
		return testBallotBox == that.testBallotBox && numberOfWriteInFields == that.numberOfWriteInFields && verificationCardSetId.equals(
				that.verificationCardSetId) && ballotBoxId.equals(that.ballotBoxId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(verificationCardSetId, ballotBoxId, testBallotBox, numberOfWriteInFields);
	}
}
