/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.validation.ValidationRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.ValidationVoteResult;

import okhttp3.ResponseBody;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRepositoryDecoratorTest {

	@InjectMocks
	ValidationRepositoryDecorator rut = new ValidationRepositoryDecorator() {
	};

	@Mock
	private ValidationRepository validationRepository;

	@BeforeClass
	public static void setup() {
		MockitoAnnotations.openMocks(ValidationRepositoryDecoratorTest.class);
	}

	@Test
	public void testValidateElectionDatesInEISuccessful() throws ResourceNotFoundException {
		String tenantId = "100";
		String electionEventId = "1";
		String ballotBoxId = "1";

		ValidationResult validationResultMock = new ValidationResult();
		validationResultMock.setResult(true);
		validationResultMock.setValidationError(new ValidationError(ValidationErrorType.SUCCESS));

		when(validationRepository.validateElectionDatesInEI(tenantId, electionEventId, ballotBoxId)).thenReturn(validationResultMock);
		ValidationResult validationResult = rut.validateElectionDatesInEI(tenantId, electionEventId, ballotBoxId);

		assertEquals(validationResultMock.isResult(), validationResult.isResult());
	}

	@Test
	public void testValidateElectionDatesInEIResultFalseSuccessful() throws ResourceNotFoundException {
		String tenantId = "100";
		String electionEventId = "1";
		String ballotBoxId = "1";

		ValidationResult validationResultMock = new ValidationResult();
		validationResultMock.setResult(false);
		validationResultMock.setValidationError(new ValidationError(ValidationErrorType.SUCCESS));

		when(validationRepository.validateElectionDatesInEI(tenantId, electionEventId, ballotBoxId)).thenReturn(validationResultMock);
		ValidationResult validationResult = rut.validateElectionDatesInEI(tenantId, electionEventId, ballotBoxId);

		assertEquals(validationResultMock.isResult(), validationResult.isResult());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testValidateElectionDatesInEIResultNotFoundException() throws ResourceNotFoundException {
		String tenantId = "100";
		String electionEventId = "1";
		String ballotBoxId = "1";

		when(validationRepository.validateElectionDatesInEI(tenantId, electionEventId, ballotBoxId))
				.thenThrow(new ResourceNotFoundException("exception"));
		rut.validateElectionDatesInEI(tenantId, electionEventId, ballotBoxId);
	}

	@Test
	public void testValidateVoteInEISuccessful() throws ResourceNotFoundException {
		String tenantId = "100";
		String electionEventId = "1";
		Vote voteMock = new Vote();

		ValidationResult validationResultMock = new ValidationResult();
		validationResultMock.setResult(true);
		validationResultMock.setValidationError(new ValidationError(ValidationErrorType.SUCCESS));

		when(validationRepository.validateVoteInEI(tenantId, electionEventId, voteMock)).thenReturn(validationResultMock);
		ValidationResult validationResult = rut.validateVoteInEI(tenantId, electionEventId, voteMock);

		assertEquals(validationResultMock.isResult(), validationResult.isResult());
	}

	@Test
	public void testValidateVoteInEIResultFalseSuccessful() throws ResourceNotFoundException {
		String tenantId = "100";
		String electionEventId = "1";
		Vote voteMock = new Vote();

		ValidationResult validationResultMock = new ValidationResult();
		validationResultMock.setResult(false);
		validationResultMock.setValidationError(new ValidationError(ValidationErrorType.SUCCESS));

		when(validationRepository.validateVoteInEI(tenantId, electionEventId, voteMock)).thenReturn(validationResultMock);
		ValidationResult validationResult = rut.validateVoteInEI(tenantId, electionEventId, voteMock);

		assertEquals(validationResultMock.isResult(), validationResult.isResult());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testValidateVoteInEIResultNotFoundException() throws ResourceNotFoundException {
		String tenantId = "100";
		String electionEventId = "1";
		Vote voteMock = new Vote();

		when(validationRepository.validateVoteInEI(tenantId, electionEventId, voteMock)).thenThrow(new ResourceNotFoundException("exception"));
		rut.validateVoteInEI(tenantId, electionEventId, voteMock);
	}

	@Test
	public void testValidateVoteSuccessful() throws ResourceNotFoundException {
		String tenantId = "100";
		String electionEventId = "1";
		Vote voteMock = new Vote();

		ValidationVoteResult validationResultMock = new ValidationVoteResult();
		validationResultMock.setValid(true);
		validationResultMock.setValidationError(new ValidationError(ValidationErrorType.SUCCESS));

		when(validationRepository.validateVote(tenantId, electionEventId, voteMock)).thenReturn(validationResultMock);
		ValidationVoteResult validationResult = rut.validateVote(tenantId, electionEventId, voteMock);

		assertEquals(validationResultMock.isValid(), validationResult.isValid());
	}

	@Test
	public void testValidateVoteResultFalseSuccessful() throws ResourceNotFoundException {
		String tenantId = "100";
		String electionEventId = "1";
		Vote voteMock = new Vote();

		ValidationVoteResult validationResultMock = new ValidationVoteResult();
		validationResultMock.setValid(false);
		validationResultMock.setValidationError(new ValidationError(ValidationErrorType.SUCCESS));

		when(validationRepository.validateVote(tenantId, electionEventId, voteMock)).thenReturn(validationResultMock);
		ValidationVoteResult validationResult = rut.validateVote(tenantId, electionEventId, voteMock);

		assertEquals(validationResultMock.isValid(), validationResult.isValid());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testValidateVoteResultNotFoundException() throws ResourceNotFoundException {
		String tenantId = "100";
		String electionEventId = "1";
		Vote voteMock = new Vote();

		int notFoundStatus = 404;

		RetrofitException error = new RetrofitException(notFoundStatus, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0]));

		when(validationRepository.validateVote(tenantId, electionEventId, voteMock)).thenThrow(error);
		rut.validateVote(tenantId, electionEventId, voteMock);
	}

	@Test
	public void testValidateVoteResultInvalidCausedByError() throws ResourceNotFoundException {
		String tenantId = "100";
		String electionEventId = "1";
		Vote voteMock = new Vote();

		int internalServerError = 500;

		RetrofitException error = new RetrofitException(internalServerError, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0]));

		when(validationRepository.validateVote(tenantId, electionEventId, voteMock)).thenThrow(error);
		ValidationVoteResult validationResult = rut.validateVote(tenantId, electionEventId, voteMock);

		assertFalse(validationResult.isValid());
	}
}
