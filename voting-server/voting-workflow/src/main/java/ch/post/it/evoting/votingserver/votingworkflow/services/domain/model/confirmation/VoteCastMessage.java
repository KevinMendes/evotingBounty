/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation;

/**
 * The class representing the vote cast return code
 */
public class VoteCastMessage {

	// the vote cast return code
	private String voteCastCode;

	public VoteCastMessage(String voteCastCode) {
		this.voteCastCode = voteCastCode;
	}

	/**
	 * Gets voteCastCode.
	 *
	 * @return Value of voteCastCode.
	 */
	public String getVoteCastCode() {
		return voteCastCode;
	}

	/**
	 * Sets new voteCastCode.
	 *
	 * @param voteCastCode New value of voteCastCode.
	 */
	public void setVoteCastCode(String voteCastCode) {
		this.voteCastCode = voteCastCode;
	}
}
