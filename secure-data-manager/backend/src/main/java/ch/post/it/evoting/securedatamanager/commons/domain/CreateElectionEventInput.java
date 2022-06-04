/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.commons.domain;

public class CreateElectionEventInput {

	private String eeid;

	private String start;

	private String end;

	private Integer validityPeriod;

	private String challengeResExpTime;

	private String authTokenExpTime;

	private String challengeLength;

	private String numVotesPerVotingCard;

	private String numVotesPerAuthToken;

	private String maxNumberOfAttempts;

	private String outputPath;

	private String keyForProtectingKeystorePassword;

	private CreateElectionEventCertificatePropertiesContainer createElectionEventCertificateProperties;

	public String getEeid() {
		return eeid;
	}

	public void setEeid(final String eeid) {
		this.eeid = eeid;
	}

	public String getStart() {
		return start;
	}

	public void setStart(final String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(final String end) {
		this.end = end;
	}

	public Integer getValidityPeriod() {
		return validityPeriod;
	}

	public void setValidityPeriod(final Integer validityPeriod) {
		this.validityPeriod = validityPeriod;
	}

	public String getChallengeResExpTime() {
		return challengeResExpTime;
	}

	public void setChallengeResExpTime(final String challengeResExpTime) {
		this.challengeResExpTime = challengeResExpTime;
	}

	public String getAuthTokenExpTime() {
		return authTokenExpTime;
	}

	public void setAuthTokenExpTime(final String authTokenExpTime) {
		this.authTokenExpTime = authTokenExpTime;
	}

	public String getChallengeLength() {
		return challengeLength;
	}

	public void setChallengeLength(final String challengeLength) {
		this.challengeLength = challengeLength;
	}

	public String getNumVotesPerVotingCard() {
		return numVotesPerVotingCard;
	}

	public void setNumVotesPerVotingCard(final String numVotesPerVotingCard) {
		this.numVotesPerVotingCard = numVotesPerVotingCard;
	}

	public String getNumVotesPerAuthToken() {
		return numVotesPerAuthToken;
	}

	public void setNumVotesPerAuthToken(final String numVotesPerAuthToken) {
		this.numVotesPerAuthToken = numVotesPerAuthToken;
	}

	public String getMaxNumberOfAttempts() {
		return maxNumberOfAttempts;
	}

	public void setMaxNumberOfAttempts(final String maxNumberOfAttempts) {
		this.maxNumberOfAttempts = maxNumberOfAttempts;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(final String outputPath) {
		this.outputPath = outputPath;
	}

	public String getKeyForProtectingKeystorePassword() {
		return keyForProtectingKeystorePassword;
	}

	public void setKeyForProtectingKeystorePassword(final String keyForProtectingKeystorePassword) {
		this.keyForProtectingKeystorePassword = keyForProtectingKeystorePassword;
	}

	public CreateElectionEventCertificatePropertiesContainer getCertificatePropertiesInput() {
		return createElectionEventCertificateProperties;
	}

	public void setCertificatePropertiesInput(final CreateElectionEventCertificatePropertiesContainer createElectionEventCertificateProperties) {
		this.createElectionEventCertificateProperties = createElectionEventCertificateProperties;
	}
}
