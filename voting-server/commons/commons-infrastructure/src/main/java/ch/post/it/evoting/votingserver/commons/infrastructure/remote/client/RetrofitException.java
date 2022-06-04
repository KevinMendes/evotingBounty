/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.remote.client;

import java.util.Optional;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

import okhttp3.ResponseBody;

public class RetrofitException extends ResourceNotFoundException {

	private static final long serialVersionUID = -3140954400153986602L;

	private int httpCode = 0;

	private Optional<ResponseBody> errorMsg = Optional.ofNullable(null);

	public RetrofitException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public RetrofitException(final int httpCode, final String message, final Throwable cause) {
		super(message, cause);
		this.httpCode = httpCode;
	}

	public RetrofitException(final String message) {
		super(message);
	}

	public RetrofitException(final int httpCode, final ResponseBody body) {
		super(Integer.toString(httpCode));
		errorMsg = Optional.ofNullable(body);
		this.httpCode = httpCode;
	}

	public int getHttpCode() {
		return httpCode;
	}

	public ResponseBody getErrorBody() {
		if (errorMsg.isPresent()) {
			return errorMsg.get();
		}
		return null;
	}

}
