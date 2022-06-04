/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans;

import java.math.BigInteger;
import java.util.Map;

/**
 *
 */
public final class VerificationCardCodesDataPack {

	private final Map<String, String> codesMappingTable;

	private final String ballotCastingKey;

	private final String voteCastingCode;

	private final Map<String, BigInteger> choiceCodes2BallotVotingOption;

	public VerificationCardCodesDataPack(final Map<String, String> listMappings, final String ballotCastingKey, final String voteCastingCode,
			final Map<String, BigInteger> choiceCodes2BallotVotingOption) {

		this.codesMappingTable = listMappings;
		this.ballotCastingKey = ballotCastingKey;
		this.voteCastingCode = voteCastingCode;
		this.choiceCodes2BallotVotingOption = choiceCodes2BallotVotingOption;
	}

	public Map<String, String> getCodesMappingTable() {
		return codesMappingTable;
	}

	public String getBallotCastingKey() {
		return ballotCastingKey;
	}

	public String getVoteCastingCode() {
		return voteCastingCode;
	}

	public Map<String, BigInteger> getMapChoiceCodesToVotingOption() {
		return choiceCodes2BallotVotingOption;
	}
}
