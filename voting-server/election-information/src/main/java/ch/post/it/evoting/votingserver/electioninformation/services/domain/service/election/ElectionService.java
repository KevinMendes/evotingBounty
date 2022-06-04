/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.election;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformation;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformationRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation.ElectionValidationRequest;

/**
 * Handles operations on election.
 */
@Stateless
public class ElectionService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionService.class);
	private static final String JSON_PARAMETER_DATE_FROM = "startDate";
	private static final String JSON_PARAMETER_DATE_TO = "endDate";
	private static final String GRACE_PERIOD = "gracePeriod";

	@Inject
	private BallotBoxInformationRepository ballotBoxInformationRepository;

	/**
	 * Validates if the election is open
	 *
	 * @param request a request object including the ids and whether the grace period has to be applied or not
	 */
	public ValidationError validateIfElectionIsOpen(final ElectionValidationRequest request) {
		final ValidationError result = new ValidationError();
		final BallotBoxInformation info;
		try {
			info = ballotBoxInformationRepository.findByTenantIdElectionEventIdBallotBoxId(request.getTenantId(), request.getElectionEventId(),
					request.getBallotBoxId());
		} catch (final ResourceNotFoundException e) {
			LOGGER.error("Failed to validate is election is open.", e);
			return result;
		}

		result.setValidationErrorType(ValidationErrorType.SUCCESS);
		final JsonObject json = JsonUtils.getJsonObject(info.getJson());
		final String dateFromString = json.getString(JSON_PARAMETER_DATE_FROM);
		final String dateToString = json.getString(JSON_PARAMETER_DATE_TO);

		final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		final ZonedDateTime dateTo = ZonedDateTime.parse(dateToString, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC));
		final ZonedDateTime dateFrom = ZonedDateTime.parse(dateFromString, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC));
		final DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);
		final int gracePeriodInSeconds = Integer.parseInt(json.getString(GRACE_PERIOD));
		final boolean isElectionOverDate = request.isValidatedWithGracePeriod() ?
				isElectionOverDateWithGracePeriod(now, dateTo, gracePeriodInSeconds) :
				isElectionOverDate(now, dateTo);
		if (isElectionOverDate) {
			result.setValidationErrorType(ValidationErrorType.ELECTION_OVER_DATE);
			final String dateError = request.isValidatedWithGracePeriod() ?
					dateTo.plusSeconds(gracePeriodInSeconds).format(formatter) :
					dateTo.format(formatter);
			final String[] errorArgs = { dateError };
			result.setErrorArgs(errorArgs);
		} else if (hasNotElectionStarted(now, dateFrom)) {
			result.setValidationErrorType(ValidationErrorType.ELECTION_NOT_STARTED);
			final String[] errorArgs = { dateFrom.format(formatter) };
			result.setErrorArgs(errorArgs);
		}
		return result;
	}

	private boolean isElectionOverDate(final ZonedDateTime now, final ZonedDateTime dateTo) {
		return now.isAfter(dateTo);
	}

	private boolean isElectionOverDateWithGracePeriod(final ZonedDateTime now, final ZonedDateTime dateTo, final int gracePeriodInSeconds) {
		return now.isAfter(dateTo.plusSeconds(gracePeriodInSeconds));
	}

	private boolean hasNotElectionStarted(final ZonedDateTime now, final ZonedDateTime dateFrom) {
		return now.isBefore(dateFrom);
	}
}
