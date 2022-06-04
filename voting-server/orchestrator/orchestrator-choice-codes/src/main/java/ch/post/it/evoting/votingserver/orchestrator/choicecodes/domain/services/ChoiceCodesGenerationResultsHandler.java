/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.choicecodes.domain.services;

import javax.inject.Inject;

import ch.post.it.evoting.votingserver.orchestrator.commons.config.QueuesConfig;
import ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence.Jdbc;
import ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence.PartialResultsRepository;
import ch.post.it.evoting.votingserver.orchestrator.commons.polling.AbstractPartialResultsHandler;

public final class ChoiceCodesGenerationResultsHandler extends AbstractPartialResultsHandler<byte[]> {

	/**
	 * Constructor.
	 *
	 * @param repository
	 */
	@Inject
	public ChoiceCodesGenerationResultsHandler(
			@Jdbc
					PartialResultsRepository<byte[]> repository) {
		super(repository);
	}

	@Override
	protected int getPartialResultsCount() {
		return QueuesConfig.GENERATION_CONTRIBUTIONS_RES_QUEUES.length;
	}
}
