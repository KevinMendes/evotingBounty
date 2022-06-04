/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch;

import org.springframework.batch.item.ExecutionContext;

import ch.post.it.evoting.securedatamanager.commons.Constants;

public class VotingCardGenerationJobExecutionContext {

	private final ExecutionContext executionContext;

	public VotingCardGenerationJobExecutionContext(final ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

	public String getTenantId() {
		return getExecutionContextValue(Constants.TENANT_ID);
	}

	public void setTenantId(final String value) {
		setExecutionContextValue(Constants.TENANT_ID, value);
	}

	public String getElectionEventId() {
		return getExecutionContextValue(Constants.ELECTION_EVENT_ID);
	}

	public void setElectionEventId(final String value) {
		setExecutionContextValue(Constants.ELECTION_EVENT_ID, value);
	}

	public String getJobInstanceId() {
		return getExecutionContextValue(Constants.JOB_INSTANCE_ID);
	}

	public void setJobInstanceId(final String value) {
		setExecutionContextValue(Constants.JOB_INSTANCE_ID, value);
	}

	public String getBallotBoxId() {
		return getExecutionContextValue(Constants.BALLOT_BOX_ID);
	}

	public void setBallotBoxId(final String value) {
		setExecutionContextValue(Constants.BALLOT_BOX_ID, value);
	}

	public String getBallotId() {
		return getExecutionContextValue(Constants.BALLOT_ID);
	}

	public void setBallotId(final String value) {
		setExecutionContextValue(Constants.BALLOT_ID, value);
	}

	public void setBasePath(final String value) {
		setExecutionContextValue(Constants.BASE_PATH, value);
	}

	public String getVotingCardSetId() {
		return getExecutionContextValue(Constants.VOTING_CARD_SET_ID);
	}

	public void setVotingCardSetId(final String value) {
		setExecutionContextValue(Constants.VOTING_CARD_SET_ID, value);
	}

	public String getElectoralAuthorityId() {
		return getExecutionContextValue(Constants.ELECTORAL_AUTHORITY_ID);
	}

	public void setElectoralAuthorityId(final String value) {
		setExecutionContextValue(Constants.ELECTORAL_AUTHORITY_ID, value);
	}

	public void setValidityPeriodStart(final String value) {
		setExecutionContextValue(Constants.VALIDITY_PERIOD_START, value);
	}

	public void setValidityPeriodEnd(final String value) {
		setExecutionContextValue(Constants.VALIDITY_PERIOD_END, value);
	}

	public int getNumberOfVotingCards() {
		return getExecutionContextValue(Constants.NUMBER_VOTING_CARDS);
	}

	public void setNumberOfVotingCards(final int value) {
		setExecutionContextValue(Constants.NUMBER_VOTING_CARDS, value);
	}

	private <R extends Object> R getExecutionContextValue(final String key) {
		return (R) executionContext.get(key);
	}

	private void setExecutionContextValue(final String key, final String value) {
		executionContext.putString(key, value);
	}

	private void setExecutionContextValue(final String key, final int value) {
		executionContext.putInt(key, value);
	}

	public String getVerificationCardSetId() {
		return getExecutionContextValue(Constants.VERIFICATION_CARD_SET_ID);
	}

	public void setVerificationCardSetId(final String value) {
		setExecutionContextValue(Constants.VERIFICATION_CARD_SET_ID, value);
	}

	public int getGeneratedCardCount() {
		return getExecutionContextValue(Constants.GENERATED_VC_COUNT);
	}

	public void setGeneratedCardCount(final int value) {
		setExecutionContextValue(Constants.GENERATED_VC_COUNT, value);
	}

	public int getErrorCount() {
		return getExecutionContextValue(Constants.ERROR_COUNT);
	}

	public void setErrorCount(final int value) {
		setExecutionContextValue(Constants.ERROR_COUNT, value);
	}

	public String getSaltCredentialId() {
		return getExecutionContextValue(Constants.SALT_CREDENTIAL_ID);
	}

	public void setSaltCredentialId(final String value) {
		setExecutionContextValue(Constants.SALT_CREDENTIAL_ID, value);
	}

	public String getSaltKeystoreSymmetricEncryptionKey() {
		return getExecutionContextValue(Constants.SALT_KEYSTORE_SYM_ENC_KEY);
	}

	public void setSaltKeystoreSymmetricEncryptionKey(final String value) {
		setExecutionContextValue(Constants.SALT_KEYSTORE_SYM_ENC_KEY, value);
	}

	public void setVotingCardSetName(final String value) {
		setExecutionContextValue(Constants.VOTING_CARD_SET_NAME, value);
	}

	public void setPlatformRootCACertificate(final String value) {
		setExecutionContextValue(Constants.PLATFORM_ROOT_CA_CERTIFICATE, value);
	}

}
