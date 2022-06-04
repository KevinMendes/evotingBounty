/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformation;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformationRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation.ElectionValidationRequest;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.election.ElectionService;

@RunWith(MockitoJUnitRunner.class)
public class ElectionServiceTest {

	public static final long SECONDS_30 = 30L;

	public static final String ZERO = "0";
	private static final Long days_4 = 4L;
	private static final Long days_8 = 8L;
	private static final long minutes_1 = 1L;
	public static String seconds_600 = "600";
	private static LocalDateTime now;
	@InjectMocks
	private final ElectionService electionService = new ElectionService();
	@Mock
	private BallotBoxInformationRepository ballotBoxInformationRepository;
	private DateTimeFormatter formatter;

	@Before
	public void setup() {
		formatter = DateTimeFormatter.ISO_DATE_TIME;
		now = LocalDateTime.now();
	}

	@Test
	public void electionInDatesTest() throws ResourceNotFoundException {
		final JsonObjectBuilder jsonObject = Json.createObjectBuilder();
		final BallotBoxInformation ballotBoxInformation = new BallotBoxInformation();
		jsonObject.add("startDate", now.minusDays(days_4).format(formatter)).add("endDate", now.plusDays(days_4).format(formatter))
				.add("gracePeriod", seconds_600);
		ballotBoxInformation.setJson(jsonObject.build().toString());
		when(ballotBoxInformationRepository.findByTenantIdElectionEventIdBallotBoxId(anyString(), anyString(), anyString()))
				.thenReturn(ballotBoxInformation);
		assertEquals(electionService.validateIfElectionIsOpen(ElectionValidationRequest.create("1", "1", "1", false)).getValidationErrorType(),
				ValidationErrorType.SUCCESS);
	}

	@Test
	public void electionBeforeDateFrom() throws ResourceNotFoundException {
		final JsonObjectBuilder jsonObject = Json.createObjectBuilder();
		final BallotBoxInformation ballotBoxInformation = new BallotBoxInformation();
		jsonObject.add("startDate", now.plusDays(days_4).format(formatter)).add("endDate", now.plusDays(days_8).format(formatter))
				.add("gracePeriod", seconds_600);
		ballotBoxInformation.setJson(jsonObject.build().toString());
		when(ballotBoxInformationRepository.findByTenantIdElectionEventIdBallotBoxId(anyString(), anyString(), anyString()))
				.thenReturn(ballotBoxInformation);
		assertEquals(electionService.validateIfElectionIsOpen(ElectionValidationRequest.create("1", "1", "1", false)).getValidationErrorType(),
				ValidationErrorType.ELECTION_NOT_STARTED);
	}

	@Test
	public void electionAfterDateTo() throws ResourceNotFoundException {
		final JsonObjectBuilder jsonObject = Json.createObjectBuilder();
		final BallotBoxInformation ballotBoxInformation = new BallotBoxInformation();
		jsonObject.add("startDate", now.minusDays(days_8).format(formatter)).add("endDate", now.minusDays(days_4).format(formatter))
				.add("gracePeriod", seconds_600);
		ballotBoxInformation.setJson(jsonObject.build().toString());
		when(ballotBoxInformationRepository.findByTenantIdElectionEventIdBallotBoxId(anyString(), anyString(), anyString()))
				.thenReturn(ballotBoxInformation);

		assertEquals(electionService.validateIfElectionIsOpen(ElectionValidationRequest.create("1", "1", "1", false)).getValidationErrorType(),
				ValidationErrorType.ELECTION_OVER_DATE);
	}

	@Test
	public void testGracePeriodOK() throws ResourceNotFoundException {
		final ZonedDateTime dateTime = ZonedDateTime.now(ZoneOffset.UTC);
		final JsonObjectBuilder jsonObject = Json.createObjectBuilder();
		final BallotBoxInformation ballotBoxInformation = new BallotBoxInformation();
		jsonObject.add("startDate", dateTime.minusMinutes(minutes_1).format(formatter))
				.add("endDate", dateTime.minusSeconds(SECONDS_30).format(formatter)).add("gracePeriod", seconds_600);
		ballotBoxInformation.setJson(jsonObject.build().toString());
		when(ballotBoxInformationRepository.findByTenantIdElectionEventIdBallotBoxId(anyString(), anyString(), anyString()))
				.thenReturn(ballotBoxInformation);

		assertEquals(electionService.validateIfElectionIsOpen(ElectionValidationRequest.create("1", "1", "1", true)).getValidationErrorType(),
				ValidationErrorType.SUCCESS);
	}

	@Test
	public void testGracePeriodKO() throws ResourceNotFoundException {
		final ZonedDateTime dateTime = ZonedDateTime.now(ZoneOffset.UTC);
		final JsonObjectBuilder jsonObject = Json.createObjectBuilder();
		final BallotBoxInformation ballotBoxInformation = new BallotBoxInformation();
		jsonObject.add("startDate", dateTime.minusMinutes(minutes_1).format(formatter))
				.add("endDate", dateTime.minusSeconds(SECONDS_30).format(formatter)).add("gracePeriod", ZERO);
		ballotBoxInformation.setJson(jsonObject.build().toString());
		when(ballotBoxInformationRepository.findByTenantIdElectionEventIdBallotBoxId(anyString(), anyString(), anyString()))
				.thenReturn(ballotBoxInformation);
		assertEquals(electionService.validateIfElectionIsOpen(ElectionValidationRequest.create("1", "1", "1", false)).getValidationErrorType(),
				ValidationErrorType.ELECTION_OVER_DATE);
	}

	@Test
	public void test_when_election_valid_should_not_log_anything() throws Exception {
		final ZonedDateTime start = ZonedDateTime.now(ZoneOffset.UTC);
		final ZonedDateTime end = start.plus(1, ChronoUnit.YEARS);

		final JsonObjectBuilder jsonObject = Json.createObjectBuilder();
		final BallotBoxInformation ballotBoxInformation = new BallotBoxInformation();
		jsonObject.add("startDate", start.format(formatter)).add("endDate", end.format(formatter)).add("gracePeriod", ZERO);
		ballotBoxInformation.setJson(jsonObject.build().toString());
		when(ballotBoxInformationRepository.findByTenantIdElectionEventIdBallotBoxId(anyString(), anyString(), anyString()))
				.thenReturn(ballotBoxInformation);
		assertEquals(ValidationErrorType.SUCCESS,
				electionService.validateIfElectionIsOpen(ElectionValidationRequest.create("1", "1", "1", false)).getValidationErrorType());
	}

	@Test
	public void test_when_election_closed_invalid_should_log_event() throws Exception {
		// election closed 1 minute ago
		final ZonedDateTime start = ZonedDateTime.now(ZoneOffset.UTC).minus(10, ChronoUnit.MINUTES);
		final ZonedDateTime end = start.minus(1, ChronoUnit.MINUTES);

		final JsonObjectBuilder jsonObject = Json.createObjectBuilder();
		final BallotBoxInformation ballotBoxInformation = new BallotBoxInformation();
		jsonObject.add("startDate", start.format(formatter)).add("endDate", end.format(formatter)).add("gracePeriod", ZERO);
		ballotBoxInformation.setJson(jsonObject.build().toString());
		when(ballotBoxInformationRepository.findByTenantIdElectionEventIdBallotBoxId(anyString(), anyString(), anyString()))
				.thenReturn(ballotBoxInformation);
		assertEquals(ValidationErrorType.ELECTION_OVER_DATE,
				electionService.validateIfElectionIsOpen(ElectionValidationRequest.create("1", "1", "1", false)).getValidationErrorType());
	}

	@Test
	public void test_when_election_not_started_should_log_event() throws Exception {
		// election starting 10 minutes from now
		final ZonedDateTime start = ZonedDateTime.now(ZoneOffset.UTC).plus(10, ChronoUnit.MINUTES);
		final ZonedDateTime end = start.plus(10, ChronoUnit.MINUTES);

		final JsonObjectBuilder jsonObject = Json.createObjectBuilder();
		final BallotBoxInformation ballotBoxInformation = new BallotBoxInformation();
		jsonObject.add("startDate", start.format(formatter)).add("endDate", end.format(formatter)).add("gracePeriod", ZERO);
		ballotBoxInformation.setJson(jsonObject.build().toString());
		when(ballotBoxInformationRepository.findByTenantIdElectionEventIdBallotBoxId(anyString(), anyString(), anyString()))
				.thenReturn(ballotBoxInformation);
		assertEquals(ValidationErrorType.ELECTION_NOT_STARTED,
				electionService.validateIfElectionIsOpen(ElectionValidationRequest.create("1", "1", "1", false)).getValidationErrorType());
	}
}
