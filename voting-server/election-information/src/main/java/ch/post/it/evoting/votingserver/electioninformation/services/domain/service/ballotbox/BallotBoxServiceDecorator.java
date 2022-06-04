/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.ballotbox;

import java.io.IOException;
import java.io.OutputStream;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Decorator for ballot box service.
 */
@Decorator
public class BallotBoxServiceDecorator implements BallotBoxService {

	@Inject
	@Delegate
	private BallotBoxService ballotBoxService;

	@Override
	public ValidationResult checkIfBallotBoxesAreEmpty(final String tenantId, final String electionEventId, final String ballotBoxId) {
		return ballotBoxService.checkIfBallotBoxesAreEmpty(tenantId, electionEventId, ballotBoxId);
	}

	@Override
	public void writeEncryptedBallotBox(final OutputStream stream, final String tenantId, final String electionEventId, final String ballotBoxId,
			final boolean test) throws IOException {
		ballotBoxService.writeEncryptedBallotBox(stream, tenantId, electionEventId, ballotBoxId, test);
	}

	/**
	 * Check if a ballot box is a test ballot box
	 *
	 * @param tenantId        - tenant identifier
	 * @param electionEventId - election event identifier
	 * @param ballotBoxId     - ballot box identifier
	 * @return if the ballot box is a test ballot box or not
	 * @throws ResourceNotFoundException
	 */
	@Override
	public boolean checkIfTest(String tenantId, String electionEventId, String ballotBoxId) throws ResourceNotFoundException {
		return ballotBoxService.checkIfTest(tenantId, electionEventId, ballotBoxId);
	}

}
