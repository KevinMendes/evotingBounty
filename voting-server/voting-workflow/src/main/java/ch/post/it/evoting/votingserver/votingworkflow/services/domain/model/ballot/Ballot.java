/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.ballot;

/**
 * Represents a ballot. Contains all the voting options for a selection of elections within an
 * election event.
 */
public class Ballot {

	/**
	 * The identifier of a ballot.
	 */
	private String id;

	/**
	 * The ballot in json format.
	 */
	private String json;

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
	 * Returns the current value of the field json.
	 *
	 * @return Returns the json.
	 */
	public String getJson() {
		return json;
	}

	/**
	 * Sets the value of the field json.
	 *
	 * @param json The json to set.
	 */
	public void setJson(String json) {
		this.json = json;
	}

}
