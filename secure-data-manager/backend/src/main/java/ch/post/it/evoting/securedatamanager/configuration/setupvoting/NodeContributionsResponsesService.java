/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ChoiceCodeGenerationDTO;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationResponsePayload;

/**
 * Service loading the chunk-wise contributions of all control component nodes. For performance reasons, the GenVerDat algorithm splits the entire
 * verification card set into smaller pieces (a process called chunking). In this service, we combine the chunks into a single data structure for the
 * entire verification card set.
 */
@Service
public class NodeContributionsResponsesService {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeContributionsResponsesService.class);

	private final NodeContributionsResponsesFileRepository nodeContributionsResponsesFileRepository;

	public NodeContributionsResponsesService(final NodeContributionsResponsesFileRepository nodeContributionsResponsesFileRepository) {
		this.nodeContributionsResponsesFileRepository = nodeContributionsResponsesFileRepository;
	}

	/**
	 * Loads all node contributions responses for the given {@code electionEventId} and {@code verificationCardSetId}. These node contributions
	 * responses are chunked in a list of node contributions.
	 *
	 * @param electionEventId       the node contributions responses' election event id.
	 * @param verificationCardSetId the node contributions responses' verification card set id.
	 * @return all node contributions responses {@code electionEventId} and {@code verificationCardSetId}.
	 * @throws IllegalArgumentException  if
	 *                                   <ul>
	 *                                       <li>Election event is not consistent.</li>
	 *                                       <li>Verification card set is not consistent.</li>
	 *                                   </ul>
	 * @throws FailedValidationException if {@code electionEventId} or {@code verificationCardSetId} is invalid.
	 */
	public List<List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>>> load(final String electionEventId,
			final String verificationCardSetId) {
		validateUUID(electionEventId);
		validateUUID(verificationCardSetId);

		LOGGER.info("Loading all the node contributions. [electionEventId: {}, verificationCardSetId: {}]", electionEventId, verificationCardSetId);

		final List<List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>>> nodeContributionsResponses = nodeContributionsResponsesFileRepository.findAll(
				electionEventId, verificationCardSetId);

		for (final List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> nodeContributionsResponse : nodeContributionsResponses) {
			for (final ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload> choiceCodeGenerationDTO : nodeContributionsResponse) {
				final ReturnCodeGenerationResponsePayload payload = choiceCodeGenerationDTO.getPayload();
				checkArgument(electionEventId.equals(choiceCodeGenerationDTO.getPayload().getElectionEventId()),
						String.format("Election event is not consistent. [expected: %s, actual: %s]", electionEventId, payload.getElectionEventId()));
				checkArgument(verificationCardSetId.equals(payload.getVerificationCardSetId()),
						String.format("Verification card set is not consistent. [expected: %s, actual: %s]", verificationCardSetId,
								payload.getVerificationCardSetId()));
			}
		}

		return nodeContributionsResponses;
	}

}
