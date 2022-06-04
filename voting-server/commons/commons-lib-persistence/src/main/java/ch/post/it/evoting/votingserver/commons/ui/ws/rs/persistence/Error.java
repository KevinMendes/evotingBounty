/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence;

/**
 * This class represents the errors detected in a specific field of a resource by a webservice processing.
 */
public class Error {

	/* The resource where the error is found. */
	private String resource;

	/* The field of a resource where the error is found. */
	private String field;

	/* The code error. */
	private String code;

	/**
	 * Returns the current value of the field resource.
	 *
	 * @return Returns the resource.
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * Sets the value of the field resource.
	 *
	 * @param resource The resource to set.
	 */
	public void setResource(String resource) {
		this.resource = resource;
	}

	/**
	 * Returns the current value of the field field.
	 *
	 * @return Returns the field.
	 */
	public String getField() {
		return field;
	}

	/**
	 * Sets the value of the field field.
	 *
	 * @param field The field to set.
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * Returns the current value of the field code.
	 *
	 * @return Returns the code.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the value of the field code.
	 *
	 * @param code The code to set.
	 */
	public void setCode(String code) {
		this.code = code;
	}
}
