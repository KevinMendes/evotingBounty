/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election;

/**
 * Bean representing the structure of the AuthenticationVoterData.json file, generated as an output of the Create Election Event command
 */
public class AuthenticationVoterData {

	private String electionEventId;

	private String electionRootCA;

	private String servicesCA;

	private String authoritiesCA;

	private String credentialsCA;

	private String adminBoard;

	private String authenticationTokenSignerCert;

	public String getElectionEventId() {
		return electionEventId;
	}

	public void setElectionEventId(final String electionEventId) {
		this.electionEventId = electionEventId;
	}

	public String getElectionRootCA() {
		return electionRootCA;
	}

	public void setElectionRootCA(final String electionRootCA) {
		this.electionRootCA = electionRootCA;
	}

	public String getAuthoritiesCA() {
		return authoritiesCA;
	}

	public void setAuthoritiesCA(final String authoritiesCA) {
		this.authoritiesCA = authoritiesCA;
	}

	public String getServicesCA() {
		return servicesCA;
	}

	public void setServicesCA(final String servicesCA) {
		this.servicesCA = servicesCA;
	}

	public String getCredentialsCA() {
		return credentialsCA;
	}

	public void setCredentialsCA(final String credentialsCA) {
		this.credentialsCA = credentialsCA;
	}

	public String getAdminBoard() {
		return adminBoard;
	}

	public void setAdminBoard(final String adminBoard) {
		this.adminBoard = adminBoard;
	}

	public String getAuthenticationTokenSignerCert() {
		return authenticationTokenSignerCert;
	}

	public void setAuthenticationTokenSignerCert(final String authenticationTokenSignerCert) {
		this.authenticationTokenSignerCert = authenticationTokenSignerCert;
	}
}
