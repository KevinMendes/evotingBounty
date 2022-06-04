/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.rule;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonString;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.rule.AbstractRule;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.Ballot;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotRepository;

/**
 * Rule that validates the vote correctness
 */
public class VoteCorrectnessRule implements AbstractRule<Vote> {

	private static final Logger LOGGER = LoggerFactory.getLogger(VoteCorrectnessRule.class);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Inject
	BallotRepository ballotRepository;

	@Override
	public ValidationError execute(final Vote vote) {
		checkNotNull(vote);

		final ValidationError result = new ValidationError(ValidationErrorType.INVALID_VOTE_CORRECTNESS);

		final String ballotId = vote.getBallotId();
		final String votingCardId = vote.getVotingCardId();

		final Ballot ballot;
		try {
			ballot = ballotRepository.findByTenantIdElectionEventIdBallotId(vote.getTenantId(), vote.getElectionEventId(), ballotId);
		} catch (ResourceNotFoundException e) {
			final String errorDescription = String.format("Cannot retrieve the ballot for a voting card. [ballotId=%s, votingCardId=%s]", ballotId,
					votingCardId);
			LOGGER.error(errorDescription, e);
			result.setErrorArgs(new String[] { errorDescription });
			return result;
		}

		final CombinedCorrectnessInformation combinedCorrectnessInformation;
		try {
			combinedCorrectnessInformation = new CombinedCorrectnessInformation(getBallot(ballot.getJson()));
		} catch (IOException e) {
			final String errorDescription = String.format(
					"Cannot deserialize the ballot json string to a valid Ballot object for a voting card. [votingCardId=%s]",
					votingCardId);
			LOGGER.error(errorDescription, e);
			result.setErrorArgs(new String[] { errorDescription });

			return result;
		}

		final String validationErrors = validate(vote, combinedCorrectnessInformation);
		if (!StringUtils.isEmpty(validationErrors)) {
			final String errorDescription = String.format(
					"Vote correctness validation failed for a voting card due to the following error: [votingCardId=%s]%s%s.",
					votingCardId, System.lineSeparator(), validationErrors);
			LOGGER.error(errorDescription);
			result.setErrorArgs(new String[] { errorDescription });

			return result;
		}

		return new ValidationError(ValidationErrorType.SUCCESS);
	}

	/**
	 * Validates the vote correctness using the provided combinedCorrectnessInformation.
	 * <p>
	 * The validation checks that the vote contains the correct number and a valid combination of selections.
	 *
	 * @return A string with all the errors encountered during the validation process.
	 */
	private String validate(final Vote vote, final CombinedCorrectnessInformation combinedCorrectnessInformation) {
		final StringBuilder errors = new StringBuilder();

		final List<String> correctnessIds = getCorrectnessIds(vote);

		if (correctnessIds.size() != combinedCorrectnessInformation.getTotalNumberOfSelections()) {

			errors.append(String.format(
					"The size of the vote's correctnessIds does not match the expected total number of selections. [correctnessIdsSize=%s, totalNumberOfSelections=%s]%s",
					correctnessIds.size(), combinedCorrectnessInformation.getTotalNumberOfSelections(), System.lineSeparator()));

		} else {

			for (int index = 0; index < combinedCorrectnessInformation.getTotalNumberOfSelections(); index++) {
				final String correctnessId = combinedCorrectnessInformation.getCorrectnessIdForSelectionIndex(index);
				if (!correctnessId.equals(correctnessIds.get(index))) {
					errors.append(
							String.format(
									"For index %s, the correctnessId does not match the expected element of the vote's correctnessIds. [correctnessId=%s, expected element=%s]%s",
									index, correctnessId, correctnessIds.get(index), System.lineSeparator()));
				}
			}

		}

		return errors.toString();
	}

	private List<String> getCorrectnessIds(final Vote vote) {
		return JsonUtils.getJsonArray(vote.getCorrectnessIds()).stream().flatMap(x -> ((JsonArray) x).stream())
				.map(value -> ((JsonString) value).getString()).collect(Collectors.toList());
	}

	public ch.post.it.evoting.cryptoprimitives.domain.election.Ballot getBallot(final String ballotAsJson) throws JsonProcessingException {
		return OBJECT_MAPPER.readValue(ballotAsJson, ch.post.it.evoting.cryptoprimitives.domain.election.Ballot.class);
	}

	@Override
	public String getName() {
		return RuleNames.VOTE_CORRECTNESS.getText();
	}

}
