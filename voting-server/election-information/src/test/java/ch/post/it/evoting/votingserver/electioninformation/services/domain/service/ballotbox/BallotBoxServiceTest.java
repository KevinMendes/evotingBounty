/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.ballotbox;

import static ch.post.it.evoting.votingserver.electioninformation.services.domain.model.util.Constants.BALLOT_BOX_ID;
import static ch.post.it.evoting.votingserver.electioninformation.services.domain.model.util.Constants.ELECTION_EVENT_ID;
import static ch.post.it.evoting.votingserver.electioninformation.services.domain.model.util.Constants.TENANT_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.domain.election.model.EntityId;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBox;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxRepository;

@RunWith(MockitoJUnitRunner.class)
public class BallotBoxServiceTest {

	private static final EntityId ENTITY_ID = new EntityId();
	@InjectMocks
	private static final BallotBoxService ballotBoxService = new BallotBoxServiceImpl();

	static {
		ENTITY_ID.setId(BALLOT_BOX_ID);
	}

	@Mock
	private BallotBoxRepository ballotBoxRepository;
	@Mock
	private BallotBoxInformationService ballotBoxInformationService;

	@Test
	public void emptyBallotBoxes() {
		when(ballotBoxRepository.findByTenantIdElectionEventIdBallotBoxId(anyString(), anyString(), anyString())).thenReturn(new ArrayList<>());

		final String tenantId = "1";
		final String electionEventId = "2";
		final String ballotBoxId = "3";
		assertTrue(ballotBoxService.checkIfBallotBoxesAreEmpty(tenantId, electionEventId, ballotBoxId).isResult());
	}

	@Test
	public void notEmptyBallotBoxes() {
		final ArrayList<BallotBox> result = new ArrayList<>();
		result.add(new BallotBox());
		when(ballotBoxRepository.findByTenantIdElectionEventIdBallotBoxId(anyString(), anyString(), anyString())).thenReturn(result);

		final String tenantId = "1";
		final String electionEventId = "2";
		final String ballotBoxId = "3";
		assertFalse(ballotBoxService.checkIfBallotBoxesAreEmpty(tenantId, electionEventId, ballotBoxId).isResult());
		assertEquals(
				ballotBoxService.checkIfBallotBoxesAreEmpty(tenantId, electionEventId, ballotBoxId).getValidationError().getValidationErrorType(),
				ValidationErrorType.FAILED);
	}

	@Test
	public void checkIfTest() throws ResourceNotFoundException {

		when(ballotBoxInformationService.isBallotBoxForTest(anyString(), anyString(), anyString())).thenReturn(true);
		assertTrue(ballotBoxService.checkIfTest(TENANT_ID, ELECTION_EVENT_ID, BALLOT_BOX_ID));
	}
}
