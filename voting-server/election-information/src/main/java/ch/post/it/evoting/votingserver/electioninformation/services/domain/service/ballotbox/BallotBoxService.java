/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.ballotbox;

import java.io.IOException;
import java.io.OutputStream;

import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Interface defining operations to handle ballot box.
 */
public interface BallotBoxService {

	/**
	 * Check if all ballot boxes for a given tenant and election event are empty.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param ballotBoxId     - the ballot box identifier.
	 * @return the result of the validation.
	 */
	ValidationResult checkIfBallotBoxesAreEmpty(String tenantId, String electionEventId, String ballotBoxId);

	/**
	 * Writes the specified signed encrypted ballot box to a given stream.
	 *
	 * @param stream          the stream
	 * @param tenantId        the tenant identifier
	 * @param electionEventId the election event identifier
	 * @param ballotBoxId     the ballot box identifier
	 * @param test            ballot box is for testing
	 * @throws IOException I/O error occurred.
	 */
	void writeEncryptedBallotBox(OutputStream stream, String tenantId, String electionEventId, String ballotBoxId, boolean test) throws IOException;

	/**
	 * Check if a ballot box is a test ballot box
	 *
	 * @param tenantId        - tenant identifier
	 * @param electionEventId - election event identifier
	 * @param ballotBoxId     - ballot box identifier
	 * @return if the ballot box is a test ballot box or not
	 * @throws ResourceNotFoundException
	 */
	boolean checkIfTest(String tenantId, String electionEventId, String ballotBoxId) throws ResourceNotFoundException;

}
