/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SemanticErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SyntaxErrorException;
import ch.post.it.evoting.votingserver.commons.domain.model.rule.AbstractRule;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;

/**
 * Rule to check the vote ids vs. the auth token ids.
 */
public class VoteIdsRule implements AbstractRule<Vote> {

	public static final String VOTER_INFORMATION_IS_NULL = "authToken.voterInformation is null";

	public static final String ERROR_CONVERTING_AUTH_TOKEN_JSON_TO_OBJECT = "Error converting auth token json to object";

	public static final String CREDENTIAL_ID_IN_VOTE_AND_AUTH_TOKEN_ARE_DIFFERENT = "Credential id in vote and auth token are different";

	public static final String BALLOT_ID_IN_VOTE_AND_AUTH_TOKEN_ARE_DIFFERENT = "Ballot id in vote and auth token are different";

	public static final String BALLOT_BOX_ID_IN_VOTE_AND_AUTH_TOKEN_ARE_DIFFERENT = "Ballot box id in vote and auth token are different";

	public static final String VOTING_CARD_ID_IN_VOTE_AND_AUTH_TOKEN_ARE_DIFFERENT = "Voting card id in vote and auth token are different";

	public static final String ELECTION_EVENT_ID_IN_VOTE_AND_AUTH_TOKEN_ARE_DIFFERENT = "Election event id in vote and auth token are different";

	public static final String TENANT_ID_IN_VOTE_AND_AUTH_TOKEN_ARE_DIFFERENT = "Tenant id in vote and auth token are different";

	public static final String VOTE_IS_NULL = "Vote is null";

	public static final String AUTH_TOKEN_IS_NULL = "Auth token is null";

	private static final Logger LOGGER = LoggerFactory.getLogger(VoteIdsRule.class);

	@Override
	public ValidationError execute(final Vote vote) {
		final ValidationError result = new ValidationError();
		final AuthenticationToken authToken;
		try {
			authToken = DomainObjectMapper.getNewInstance().readValue(vote.getAuthenticationToken(), AuthenticationToken.class);
			validateAuthToken(authToken);
		} catch (final JsonException | IOException | SyntaxErrorException | SemanticErrorException | IllegalArgumentException | NullPointerException e) {
			LOGGER.error(ERROR_CONVERTING_AUTH_TOKEN_JSON_TO_OBJECT, e);
			result.setErrorArgs(new String[] { ERROR_CONVERTING_AUTH_TOKEN_JSON_TO_OBJECT + ExceptionUtils.getRootCauseMessage(e) });
			return result;
		}

		try {
			validateInputParams(vote, authToken);
		} catch (final ApplicationException e) {
			LOGGER.error("Error trying to validate input params", e);
			result.setErrorArgs(new String[] { e.getMessage() });
			return result;
		}

		final List<ValidationIdItem> validations = buildValidationRules(vote, authToken);
		return executeValidations(validations);
	}

	/**
	 * Method for spy with mockito.
	 *
	 * @param authToken the auth token.
	 * @throws SyntaxErrorException
	 * @throws SemanticErrorException
	 */
	public void validateAuthToken(final AuthenticationToken authToken) throws SyntaxErrorException, SemanticErrorException {
		ValidationUtils.validate(authToken);
	}

	// validate input params
	private void validateInputParams(final Vote vote, final AuthenticationToken authToken) throws ApplicationException {
		if (vote == null) {
			LOGGER.error(VOTE_IS_NULL);
			throw new ApplicationException(VOTE_IS_NULL);
		}

		if (authToken == null) {
			LOGGER.error(AUTH_TOKEN_IS_NULL);
			throw new ApplicationException(AUTH_TOKEN_IS_NULL);
		}

		if (authToken.getVoterInformation() == null) {
			LOGGER.error(VOTER_INFORMATION_IS_NULL);
			throw new ApplicationException(VOTER_INFORMATION_IS_NULL);
		}
	}

	// executes rules
	private ValidationError executeValidations(final List<ValidationIdItem> validations) {
		final ValidationError result = new ValidationError();
		result.setValidationErrorType(ValidationErrorType.SUCCESS);
		for (final ValidationIdItem validation : validations) {
			if (!isValid(validation)) {
				result.setValidationErrorType(ValidationErrorType.FAILED);
				result.setErrorArgs(new String[] { validation.getErrorMessage() });
				break;
			}
		}
		return result;
	}

	// builds the validation rule list
	private List<ValidationIdItem> buildValidationRules(final Vote vote, final AuthenticationToken authToken) {
		final List<ValidationIdItem> validations = new ArrayList<>();
		validations.add(new ValidationIdItem(vote.getTenantId(), authToken.getVoterInformation().getTenantId(),
				TENANT_ID_IN_VOTE_AND_AUTH_TOKEN_ARE_DIFFERENT));
		validations.add(new ValidationIdItem(vote.getElectionEventId(), authToken.getVoterInformation().getElectionEventId(),
				ELECTION_EVENT_ID_IN_VOTE_AND_AUTH_TOKEN_ARE_DIFFERENT));
		validations.add(new ValidationIdItem(vote.getVotingCardId(), authToken.getVoterInformation().getVotingCardId(),
				VOTING_CARD_ID_IN_VOTE_AND_AUTH_TOKEN_ARE_DIFFERENT));
		validations.add(new ValidationIdItem(vote.getBallotBoxId(), authToken.getVoterInformation().getBallotBoxId(),
				BALLOT_BOX_ID_IN_VOTE_AND_AUTH_TOKEN_ARE_DIFFERENT));
		validations.add(new ValidationIdItem(vote.getBallotId(), authToken.getVoterInformation().getBallotId(),
				BALLOT_ID_IN_VOTE_AND_AUTH_TOKEN_ARE_DIFFERENT));
		validations.add(new ValidationIdItem(vote.getCredentialId(), authToken.getVoterInformation().getCredentialId(),
				CREDENTIAL_ID_IN_VOTE_AND_AUTH_TOKEN_ARE_DIFFERENT));
		return validations;
	}

	// applies validation rule
	private boolean isValid(final ValidationIdItem validation) {
		boolean result = false;
		if (!StringUtils.isBlank(validation.getId1()) && !StringUtils.isBlank(validation.getId2())) {
			result = validation.getId1().equals(validation.getId2());
		}
		return result;
	}

	@Override
	public String getName() {
		return RuleNames.VOTE_IDS.getText();
	}
}
