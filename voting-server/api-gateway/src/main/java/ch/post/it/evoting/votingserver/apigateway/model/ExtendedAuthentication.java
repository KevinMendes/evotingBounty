/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.model;

public class ExtendedAuthentication {

	private String authId;

	private String extraParam;

	public ExtendedAuthentication() {
		// This constructor is intentionally left blank
	}

	public ExtendedAuthentication(String authId, String extraParam) {
		super();
		this.authId = authId;
		this.extraParam = extraParam;
	}

	public String getExtraParam() {
		return extraParam;
	}

	public void setExtraParam(String extraParam) {
		this.extraParam = extraParam;
	}

	public String getAuthId() {
		return authId;
	}

	public void setAuthId(String authId) {
		this.authId = authId;
	}

}
