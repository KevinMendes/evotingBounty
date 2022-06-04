/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.ws.application.operation;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Class representing a ExtendedAuthResponse
 */
@JsonInclude(Include.NON_NULL)
public class ExtendedAuthResponse {

	private final String responseCode;

	private final Integer numberOfRemainingAttempts;

	private final String encryptedSVK;

	private ExtendedAuthResponse(final String responseCode, final Integer numberOfRemainingAttempts, final String encryptedSVK) {

		this.responseCode = responseCode;
		this.numberOfRemainingAttempts = numberOfRemainingAttempts;
		this.encryptedSVK = encryptedSVK;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public int getNumberOfRemainingAttempts() {
		return numberOfRemainingAttempts;
	}

	public String getEncryptedSVK() {
		return encryptedSVK;
	}

	public static class Builder {

		private Response.Status responseCode;

		private Integer numberOfRemainingAttempts;

		private String encryptedSVK;

		public Builder setResponseCode(Response.Status responseCode) {
			this.responseCode = responseCode;
			return this;
		}

		public Builder setNumberOfRemainingAttempts(Integer numberOfRemainingAttempts) {
			this.numberOfRemainingAttempts = numberOfRemainingAttempts;
			return this;
		}

		public Builder setEncryptedSVK(String encryptedSVK) {
			this.encryptedSVK = encryptedSVK;
			return this;
		}

		public ExtendedAuthResponse build() {
			return new ExtendedAuthResponse(responseCode.name(), numberOfRemainingAttempts, encryptedSVK);
		}

	}
}
