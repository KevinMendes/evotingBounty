/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.vote;

import java.io.IOException;

import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;

public interface VoteCastCodeService {

	/**
	 * Saves the cast code that has been derived from the vote confirmation code
	 */
	void save(String tenantId, String electionEventId, String votingCardId, ShortVoteCastReturnCodeAndComputeResults voteCastCode)
			throws ApplicationException, IOException;

}
