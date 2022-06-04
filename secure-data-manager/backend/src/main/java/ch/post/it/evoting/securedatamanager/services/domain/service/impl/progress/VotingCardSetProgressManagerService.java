/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service.impl.progress;

import java.util.List;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.securedatamanager.config.engine.commands.api.ConfigurationService;
import ch.post.it.evoting.securedatamanager.services.domain.model.config.VotingCardGenerationJobStatus;

@Service
public class VotingCardSetProgressManagerService extends GenericProgressManagerService<VotingCardGenerationJobStatus> {

	private final ConfigurationService configurationService;

	public VotingCardSetProgressManagerService(final ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	@Override
	protected VotingCardGenerationJobStatus defaultData(final String jobId) {
		return new VotingCardGenerationJobStatus(jobId);
	}

	@Override
	protected List<VotingCardGenerationJobStatus> getJobs() {
		return configurationService.getJobs();
	}
}

