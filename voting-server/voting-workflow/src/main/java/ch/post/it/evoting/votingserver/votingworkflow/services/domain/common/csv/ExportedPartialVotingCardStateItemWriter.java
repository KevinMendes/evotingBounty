/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.common.csv;

import java.io.OutputStream;

import ch.post.it.evoting.votingserver.commons.infrastructure.csv.AbstractCSVWriter;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.ExportedPartialVotingCardStateItem;

/**
 * The Class ExportedPartialVotingCardStateItemWriter.
 */
public class ExportedPartialVotingCardStateItemWriter extends AbstractCSVWriter<ExportedPartialVotingCardStateItem> {

	/**
	 * Instantiates a new exported partial voting card state item writer.
	 *
	 * @param outputStream the output stream
	 */
	public ExportedPartialVotingCardStateItemWriter(OutputStream outputStream) {
		super(outputStream);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * ch.post.it.evoting.vw.services.domain.common.csv.AbstractCSVWriter#extractValues(java.lang.
	 * Object)
	 */
	@Override
	protected String[] extractValues(final ExportedPartialVotingCardStateItem exportedPartialVotingCardStateItem) {

		return new String[] { exportedPartialVotingCardStateItem.getVotingCardId(), exportedPartialVotingCardStateItem.getState() };
	}

}
