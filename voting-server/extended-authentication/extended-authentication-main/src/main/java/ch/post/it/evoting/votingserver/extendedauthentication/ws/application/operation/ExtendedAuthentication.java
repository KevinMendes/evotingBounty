/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.ws.application.operation;

public class ExtendedAuthentication {

	private String authId;

	private String extraParam;

	public String getAuthId() {
		return authId;
	}

	public void setAuthId(String authId) {
		this.authId = authId;
	}

	public String getExtraParam() {
		return extraParam;
	}

	public void setExtraParam(String extraParam) {
		this.extraParam = extraParam;
	}

}

