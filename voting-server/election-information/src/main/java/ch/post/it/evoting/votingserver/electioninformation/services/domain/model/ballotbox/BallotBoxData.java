/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox;

import javax.validation.constraints.NotNull;

/**
 * Data structure used to upload the ballot box related data.
 */
public class BallotBoxData {

	@NotNull
	private String ballotBox;

	/**
	 * Returns the current value of the field ballotBox.
	 *
	 * @return Returns the ballotBox.
	 */
	public String getBallotBox() {
		return ballotBox;
	}

	/**
	 * Sets the value of the field ballotBox.
	 *
	 * @param ballotBox The ballotBox to set.
	 */
	public void setBallotBox(final String ballotBox) {
		this.ballotBox = ballotBox;
	}

}
