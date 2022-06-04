/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the relation between a ballot and a set of elections.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BallotElection {

	/**
	 * The id of a ballot.
	 */
	private String id;

	/**
	 * The list of elections.
	 */
	private Set<Election> contests;

	/**
	 * Returns the current value of the field id.
	 *
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the value of the field id.
	 *
	 * @param id The id to set.
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Returns the current value of the field electionsSet.
	 *
	 * @return Returns the electionsSet.
	 */

	public Set<Election> getContests() {
		return contests;
	}

	/**
	 * Sets the value of the field electionsSet.
	 *
	 * @param contests The electionsSet to set.
	 */

	public void setContests(final Set<Election> contests) {
		this.contests = contests;
	}
}
