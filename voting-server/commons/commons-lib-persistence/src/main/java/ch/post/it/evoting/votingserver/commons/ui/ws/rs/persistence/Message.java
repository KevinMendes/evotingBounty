/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the message to be shown in the webservice result.
 */
public class Message {

	/**
	 * The text of the message to be shown by the webservice.
	 */
	private String text;

	/**
	 * If there are errors produced during the processing performed by the webservice, this attribute contains a list of such errors.
	 */
	private List<Error> errors = new ArrayList<>();

	/**
	 * Add a new error.
	 *
	 * @param resource  The resource where the error is found.
	 * @param field     The field where the error is found.
	 * @param errorCode The error code.
	 */
	public void addError(String resource, String field, String errorCode) {
		Error error = new Error();
		error.setResource(resource);
		error.setField(field);
		error.setCode(errorCode);
		this.errors.add(error);
	}

	/**
	 * Gets the text.
	 *
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text.
	 *
	 * @param text the new text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Gets the errors.
	 *
	 * @return the errors
	 */
	public List<Error> getErrors() {
		return errors;
	}

	/**
	 * Sets the errors.
	 *
	 * @param errors the new errors
	 */
	public void setErrors(List<Error> errors) {
		this.errors = errors;
	}
}
