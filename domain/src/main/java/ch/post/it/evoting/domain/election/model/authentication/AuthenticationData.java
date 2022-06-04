/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.authentication;

/**
 * Class representing the authentication information for an election event
 */
public class AuthenticationData {

	private AuthenticationContent authenticationContent;

	private String certificates;

	/**
	 * Gets authenticationContent.
	 *
	 * @return Value of authenticationContent.
	 */
	public AuthenticationContent getAuthenticationContent() {
		return authenticationContent;
	}

	/**
	 * Sets new authenticationContent.
	 *
	 * @param authenticationContent New value of authenticationContent.
	 */
	public void setAuthenticationContent(final AuthenticationContent authenticationContent) {
		this.authenticationContent = authenticationContent;
	}

	/**
	 * Gets certificates.
	 *
	 * @return Value of certificates.
	 */
	public String getCertificates() {
		return certificates;
	}

	/**
	 * Sets new certificates.
	 *
	 * @param certificates New value of certificates.
	 */
	public void setCertificates(final String certificates) {
		this.certificates = certificates;
	}
}
