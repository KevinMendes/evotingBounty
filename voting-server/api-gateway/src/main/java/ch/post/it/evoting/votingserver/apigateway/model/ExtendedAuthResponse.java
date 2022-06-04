/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ExtendedAuthResponse {

	private String responseCode;

	private Integer numberOfRemainingAttempts;

	private String encryptedSVK;

	public String getEncryptedSVK() {
		return encryptedSVK;
	}

	public void setEncryptedSVK(String encryptedSVK) {
		this.encryptedSVK = encryptedSVK;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public Integer getNumberOfRemainingAttempts() {
		return numberOfRemainingAttempts;
	}

	public void setNumberOfRemainingAttempts(Integer numberOfRemainingAttempts) {
		this.numberOfRemainingAttempts = numberOfRemainingAttempts;
	}

}
