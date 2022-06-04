/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.votingcard;

import java.io.OutputStream;

import ch.post.it.evoting.votingserver.commons.infrastructure.csv.AbstractCSVWriter;

public class VotingCardWriter extends AbstractCSVWriter<VotingCard> {

	public VotingCardWriter(OutputStream outputStream) {
		super(outputStream);
	}

	@Override
	protected String[] extractValues(final VotingCard votingCardItem) {
		return new String[] { votingCardItem.getVotingCardId() };
	}

}
