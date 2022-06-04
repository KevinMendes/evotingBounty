/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.CleansedBallotBox;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.CleansedBallotBoxRepository;

@Stateless
@javax.ejb.ApplicationException(rollback = true)
public class CleansedBallotBoxAccess {

	@EJB
	private CleansedBallotBoxRepository cleansedBallotBoxRepository;

	public CleansedBallotBox save(CleansedBallotBox ballotBox) throws DuplicateEntryException {
		return cleansedBallotBoxRepository.save(ballotBox);
	}
}
