/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.readers;

import java.io.IOException;
import java.util.List;

import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ChoiceCodeGenerationDTO;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationRequestPayload;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationResponsePayload;
import ch.post.it.evoting.securedatamanager.batch.batch.NodeContributions;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.NodeContributionsPath;

public class NodeContributionsReader implements ItemReader<NodeContributions> {

	private final List<NodeContributionsPath> nodeContributionsPaths;

	@Autowired
	private ObjectMapper objectMapper;

	private Integer index = 0;

	public NodeContributionsReader(final List<NodeContributionsPath> nodeContributionsPaths) {
		this.nodeContributionsPaths = nodeContributionsPaths;
	}

	@Override
	public NodeContributions read() throws IOException {
		if (index < nodeContributionsPaths.size()) {
			final NodeContributionsPath nodeContributionsPath = nodeContributionsPaths.get(index);

			final List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> nodeContributionResponse = objectMapper
					.readValue(nodeContributionsPath.getOutput().toFile(),
							new TypeReference<List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>>>() {
							});

			final ReturnCodeGenerationRequestPayload nodeContributionRequest = objectMapper
					.readValue(nodeContributionsPath.getInput().toFile(), new TypeReference<ReturnCodeGenerationRequestPayload>() {
					});

			index++;
			return new NodeContributions(nodeContributionResponse, nodeContributionRequest);
		} else {
			return null;
		}
	}

}
