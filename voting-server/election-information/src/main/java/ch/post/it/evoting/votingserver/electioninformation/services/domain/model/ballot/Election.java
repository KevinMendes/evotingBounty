/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The class represents an election.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Election {

	/**
	 * The id of the election.
	 */
	private String id;

	/**
	 * The election type.
	 */
	private String type;

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
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the current value of the field type.
	 *
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the value of the field type.
	 *
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}

}
