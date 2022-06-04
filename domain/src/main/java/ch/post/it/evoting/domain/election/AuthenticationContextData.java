/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election;

/**
 * Bean representing the structure of the private information provided to the Authentication Context.
 */
public class AuthenticationContextData {

	private String electionEventId;

	private String authenticationTokenSignerKeystore;

	private String authenticationTokenSignerPassword;

	private AuthenticationParams authenticationParams;

	public String getElectionEventId() {
		return electionEventId;
	}

	public void setElectionEventId(final String electionEventId) {
		this.electionEventId = electionEventId;
	}

	public String getAuthenticationTokenSignerKeystore() {
		return authenticationTokenSignerKeystore;
	}

	public void setAuthenticationTokenSignerKeystore(final String authenticationTokenSignerKeystore) {
		this.authenticationTokenSignerKeystore = authenticationTokenSignerKeystore;
	}

	public String getAuthenticationTokenSignerPassword() {
		return authenticationTokenSignerPassword;
	}

	public void setAuthenticationTokenSignerPassword(final String authenticationTokenSignerPassword) {
		this.authenticationTokenSignerPassword = authenticationTokenSignerPassword;
	}

	public AuthenticationParams getAuthenticationParams() {
		return authenticationParams;
	}

	public void setAuthenticationParams(final AuthenticationParams authenticationParams) {
		this.authenticationParams = authenticationParams;
	}

}
