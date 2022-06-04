/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.common.csv;

import java.io.OutputStream;

import ch.post.it.evoting.votingserver.commons.infrastructure.csv.AbstractCSVWriter;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.ExportedBallotBoxItem;

/**
 * Writer in csv for every item to be downloaded of a ballot box
 */
public class ExportedBallotBoxItemWriter extends AbstractCSVWriter<ExportedBallotBoxItem> {

	public ExportedBallotBoxItemWriter(final OutputStream outputStream) {
		super(outputStream);
	}

	/**
	 * Extracts the values of a ballot box item into an array of Strings.
	 *
	 * @param exportedBallotBoxItem - ballot box item from which the info is extracted
	 */
	@Override
	public String[] extractValues(final ExportedBallotBoxItem exportedBallotBoxItem) {
		return new String[] {
				exportedBallotBoxItem.getVote(), exportedBallotBoxItem.getVoteCastCode(), exportedBallotBoxItem.getVoteComputationResults(),
				exportedBallotBoxItem.getCastCodeComputationResults(), exportedBallotBoxItem.getTenantId(),
				exportedBallotBoxItem.getElectionEventId(), exportedBallotBoxItem.getVotingCardId(), exportedBallotBoxItem.getBallotId(),
				exportedBallotBoxItem.getBallotBoxId()
		};
	}

}
