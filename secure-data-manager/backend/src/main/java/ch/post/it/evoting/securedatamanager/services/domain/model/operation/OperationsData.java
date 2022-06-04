/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model.operation;

public class OperationsData {

	private String path;
	private String privateKeyInBase64;
	private char[] password;
	private boolean electionEventData;
	private boolean votingCardsData;
	private boolean customerData;
	private boolean computedChoiceCodes;
	private boolean preComputedChoiceCodes;
	private boolean ballotBoxes;
	private boolean electionEventContextAndControlComponentKeys;

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public String getPrivateKeyInBase64() {
		return privateKeyInBase64;
	}

	public void setPrivateKeyInBase64(final String privateKeyInBase64) {
		this.privateKeyInBase64 = privateKeyInBase64;
	}

	public char[] getPassword() {
		return password;
	}

	public void setPassword(final char[] password) {
		this.password = password;
	}

	public boolean isElectionEventData() {
		return electionEventData;
	}

	public void setElectionEventData(final boolean electionEventData) {
		this.electionEventData = electionEventData;
	}

	public boolean isVotingCardsData() {
		return votingCardsData;
	}

	public void setVotingCardsData(final boolean votingCardsData) {
		this.votingCardsData = votingCardsData;
	}

	public boolean isCustomerData() {
		return customerData;
	}

	public void setCustomerData(final boolean customerData) {
		this.customerData = customerData;
	}

	public boolean isComputedChoiceCodes() {
		return computedChoiceCodes;
	}

	public void setComputedChoiceCodes(final boolean computedChoiceCodes) {
		this.computedChoiceCodes = computedChoiceCodes;
	}

	public boolean isPreComputedChoiceCodes() {
		return preComputedChoiceCodes;
	}

	public void setPreComputedChoiceCodes(final boolean preComputedChoiceCodes) {
		this.preComputedChoiceCodes = preComputedChoiceCodes;
	}

	public boolean isBallotBoxes() {
		return ballotBoxes;
	}

	public void setBallotBoxes(final boolean ballotBoxes) {
		this.ballotBoxes = ballotBoxes;
	}

	public boolean isElectionEventContextAndControlComponentKeys() {
		return electionEventContextAndControlComponentKeys;
	}

	public void setElectionEventContextAndControlComponentKeys(final boolean electionEventContextAndControlComponentKeys) {
		this.electionEventContextAndControlComponentKeys = electionEventContextAndControlComponentKeys;
	}
}
