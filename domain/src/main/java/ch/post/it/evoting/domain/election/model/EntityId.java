/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model;

/**
 * Generic class representing the id of an entity. Usually will be used to process collections of ids some operations
 */
public class EntityId {

	private String id;

	/**
	 * Gets id.
	 *
	 * @return Value of id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets new id.
	 *
	 * @param id New value of id.
	 */
	public void setId(String id) {
		this.id = id;
	}

}
