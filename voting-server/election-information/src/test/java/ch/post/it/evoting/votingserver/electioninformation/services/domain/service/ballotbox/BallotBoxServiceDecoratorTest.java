/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.ballotbox;

import static ch.post.it.evoting.votingserver.electioninformation.services.domain.model.util.Constants.BALLOT_BOX_ID;
import static ch.post.it.evoting.votingserver.electioninformation.services.domain.model.util.Constants.ELECTION_EVENT_ID;
import static ch.post.it.evoting.votingserver.electioninformation.services.domain.model.util.Constants.TENANT_ID;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.domain.election.model.EntityId;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Test class to cover the decorator of the service
 */
@RunWith(MockitoJUnitRunner.class)
public class BallotBoxServiceDecoratorTest {
	private static final EntityId ENTITY_ID = new EntityId();

	static {
		ENTITY_ID.setId(BALLOT_BOX_ID);
	}

	@InjectMocks
	private final BallotBoxServiceDecorator decorator = new BallotBoxServiceDecorator();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private BallotBoxService ballotBoxService;

	@Mock
	private OutputStream outputStream;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this.getClass());
	}

	@Test
	public void checkIfEmpty() {
		when(ballotBoxService.checkIfBallotBoxesAreEmpty(anyString(), anyString(), anyString())).thenReturn(new ValidationResult(false));
		decorator.checkIfBallotBoxesAreEmpty(TENANT_ID, ELECTION_EVENT_ID, BALLOT_BOX_ID);
	}

	@Test
	public void checkIfTest() throws ResourceNotFoundException {
		when(ballotBoxService.checkIfTest(anyString(), anyString(), anyString())).thenReturn(false);
		decorator.checkIfTest(TENANT_ID, ELECTION_EVENT_ID, BALLOT_BOX_ID);
	}

	@Test
	public void writeEncryptedBallotBox() throws IOException {
		doNothing().when(ballotBoxService).writeEncryptedBallotBox(any(OutputStream.class), anyString(), anyString(), anyString(), anyBoolean());
		try {
			decorator.writeEncryptedBallotBox(outputStream, TENANT_ID, ELECTION_EVENT_ID, BALLOT_BOX_ID, true);
		} catch (final Exception e) {
			fail("Test shouldn't throw exception " + e.getMessage());
		}
	}

	@Test
	public void writeEncryptedBallotBoxIOException() throws IOException {

		expectedException.expect(IOException.class);
		doThrow(IOException.class).when(ballotBoxService)
				.writeEncryptedBallotBox(any(OutputStream.class), anyString(), anyString(), anyString(), anyBoolean());
		decorator.writeEncryptedBallotBox(outputStream, TENANT_ID, ELECTION_EVENT_ID, BALLOT_BOX_ID, true);
	}
}
