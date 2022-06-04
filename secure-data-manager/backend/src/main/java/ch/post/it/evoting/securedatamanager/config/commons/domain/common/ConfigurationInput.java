/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.domain.common;

import java.util.Map;

import ch.post.it.evoting.cryptolib.certificates.bean.CredentialProperties;

/**
 * A bean representing the 'configuration.json' file. This file is saved as an internal configuration on the properties folder (inside distribution
 * zip file) and contains certificates and properties to be created.
 */
public class ConfigurationInput {

	private Map<String, CredentialProperties> configProperties;

	private CredentialProperties authTokenSigner;

	private CredentialProperties secureDataManager;

	private CredentialProperties ballotBox;

	private CredentialProperties credentialSign;

	private CredentialProperties credentialAuth;

	private CredentialProperties verificationCardSet;

	private CredentialProperties verificationCard;

	private CredentialProperties votingCardSet;

	public CredentialProperties getVerificationCard() {
		return verificationCard;
	}

	public void setVerificationCard(final CredentialProperties verificationCard) {
		this.verificationCard = verificationCard;
	}

	public CredentialProperties getVerificationCardSet() {
		return verificationCardSet;
	}

	public void setVerificationCardSet(final CredentialProperties verificationCardSet) {
		this.verificationCardSet = verificationCardSet;
	}

	public CredentialProperties getBallotBox() {
		return ballotBox;
	}

	public void setBallotBox(final CredentialProperties ballotBox) {
		this.ballotBox = ballotBox;
	}

	public CredentialProperties getAuthTokenSigner() {
		return authTokenSigner;
	}

	public void setAuthTokenSigner(final CredentialProperties authTokenSigner) {
		this.authTokenSigner = authTokenSigner;
	}

	public Map<String, CredentialProperties> getConfigProperties() {
		return configProperties;
	}

	public void setConfigProperties(final Map<String, CredentialProperties> configProperties) {
		this.configProperties = configProperties;
	}

	public CredentialProperties getCredentialSign() {
		return credentialSign;
	}

	public void setCredentialSign(final CredentialProperties credentialSign) {
		this.credentialSign = credentialSign;
	}

	public CredentialProperties getCredentialAuth() {
		return credentialAuth;
	}

	public void setCredentialAuth(final CredentialProperties credentialAuth) {
		this.credentialAuth = credentialAuth;
	}

	public CredentialProperties getVotingCardSet() {
		return votingCardSet;
	}

	public void setVotingCardSet(final CredentialProperties votingCardSet) {
		this.votingCardSet = votingCardSet;
	}

	public CredentialProperties getSecureDataManager() {
		return secureDataManager;
	}

	public void setSecureDataManager(final CredentialProperties secureDataManager) {
		this.secureDataManager = secureDataManager;
	}
}
