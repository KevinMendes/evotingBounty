/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.csv;

import java.io.OutputStream;

import ch.post.it.evoting.votingserver.commons.infrastructure.csv.AbstractCSVWriter;

/**
 * The Class ExportedPartialVotingCardStateItemWriter.
 */
public class FailedAuthenticationVotingCardsItemWriter extends AbstractCSVWriter<FailedAuthenticationVotingCardItem> {

	private static final String MAX_ALLOWED_NUMBER_OF_ATTEMPTS = "MAX_ALLOWED_LOGIN_ATTEMPTS_REACHED";

	/**
	 * Instantiates a new exported partial voting card state item writer.
	 *
	 * @param outputStream the output stream
	 */
	public FailedAuthenticationVotingCardsItemWriter(OutputStream outputStream) {
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
	protected String[] extractValues(final FailedAuthenticationVotingCardItem failedAuthenticationVotingCardItem) {

		return new String[] { failedAuthenticationVotingCardItem.getVotingCardId(), MAX_ALLOWED_NUMBER_OF_ATTEMPTS };
	}

}
