/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.ballotbox;

import java.io.Serializable;

/**
 * A reference to a ballot box.
 */
public interface BallotBoxId extends Serializable {

	/**
	 * @return the identifier of the tenant that owns the election event
	 */
	String getTenantId();

	/**
	 * @return the identifier of the election event the ballot box belongs to
	 */
	String getElectionEventId();

	/**
	 * @return the unique identifier of the ballot box within its election event
	 */
	String getId();
}
