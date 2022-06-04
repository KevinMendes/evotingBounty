/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model.operation;

public class OperationResult {

	private int error;
	private String message;
	private String exception;

	public int getError() {
		return error;
	}

	public void setError(final int errorCode) {
		this.error = errorCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public String getException() {
		return exception;
	}

	public void setException(final String exceptionName) {
		this.exception = exceptionName;
	}

}
