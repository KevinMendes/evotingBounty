/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.confirmation.ConfirmationInformationResult;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.ConfirmationInformationRepository;

import okhttp3.ResponseBody;

@RunWith(MockitoJUnitRunner.class)
public class ConfirmationInformationRepositoryDecoratorTest {

	private final String TENANT_ID = "100";
	private final String ELECTION_EVENT_ID = "1";
	private final String VOTING_CARD_ID = "1";
	private final String TOKEN = "token";

	@InjectMocks
	ConfirmationInformationRepositoryDecorator rut = new ConfirmationInformationRepositoryDecorator();

	@Mock
	private ConfirmationInformationRepository confirmationInformationRepository;

	@Test
	public void testValidateConfirmationMessageIsValidSuccessful() throws ResourceNotFoundException {
		ConfirmationInformation confirmationInformationMock = new ConfirmationInformation();
		ConfirmationInformationResult confirmationInformationResultMock = new ConfirmationInformationResult();
		confirmationInformationResultMock.setValid(true);

		when(confirmationInformationRepository
				.validateConfirmationMessage(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, confirmationInformationMock, TOKEN))
				.thenReturn(confirmationInformationResultMock);

		ConfirmationInformationResult confirmationInformationResult = rut
				.validateConfirmationMessage(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, confirmationInformationMock, TOKEN);

		assertTrue(confirmationInformationResult.isValid());
	}

	@Test
	public void testValidateConfirmationMessageIsNotValidSuccessful() throws ResourceNotFoundException {
		ConfirmationInformation confirmationInformationMock = new ConfirmationInformation();
		ConfirmationInformationResult confirmationInformationResultMock = new ConfirmationInformationResult();
		confirmationInformationResultMock.setValid(false);

		when(confirmationInformationRepository
				.validateConfirmationMessage(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, confirmationInformationMock, TOKEN))
				.thenReturn(confirmationInformationResultMock);

		ConfirmationInformationResult confirmationInformationResult = rut
				.validateConfirmationMessage(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, confirmationInformationMock, TOKEN);

		assertFalse(confirmationInformationResult.isValid());
	}

	@Test(expected = RetrofitException.class)
	public void testValidateConfirmationMessageRetrofitError() throws ResourceNotFoundException {
		ConfirmationInformation confirmationInformationMock = new ConfirmationInformation();
		ConfirmationInformationResult confirmationInformationResultMock = new ConfirmationInformationResult();
		confirmationInformationResultMock.setValid(false);

		RetrofitException retrofitErrorMock = new RetrofitException(404, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0]));

		when(confirmationInformationRepository
				.validateConfirmationMessage(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, confirmationInformationMock, TOKEN))
				.thenThrow(retrofitErrorMock);

		rut.validateConfirmationMessage(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, confirmationInformationMock, TOKEN);
	}

}
