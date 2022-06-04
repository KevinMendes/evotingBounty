/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch;

import java.util.List;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ChoiceCodeGenerationDTO;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationRequestPayload;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationResponsePayload;

public class NodeContributions {

	private List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> nodeContributionResponse;

	private ReturnCodeGenerationRequestPayload nodeContributionRequest;

	public NodeContributions(final List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> nodeContributionResponse,
			final ReturnCodeGenerationRequestPayload nodeContributionRequest) {
		this.nodeContributionResponse = nodeContributionResponse;
		this.nodeContributionRequest = nodeContributionRequest;
	}

	public List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> getNodeContributionResponse() {
		return nodeContributionResponse;
	}

	public void setNodeContributionResponse(final List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> nodeContributionResponse) {
		this.nodeContributionResponse = nodeContributionResponse;
	}

	public ReturnCodeGenerationRequestPayload getNodeContributionRequest() {
		return nodeContributionRequest;
	}

	public void setNodeContributionRequest(final ReturnCodeGenerationRequestPayload nodeContributionRequest) {
		this.nodeContributionRequest = nodeContributionRequest;
	}

}
