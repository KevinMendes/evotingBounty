/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot;

import javax.validation.constraints.NotNull;

/**
 * Data structure used to upload the ballot related data.
 */
public class BallotData {

	@NotNull
	private String ballot;

	@NotNull
	private String ballottext;

	@NotNull
	private String adminBoardId;

	/**
	 * Returns the current value of the field ballot.
	 *
	 * @return Returns the ballot.
	 */
	public String getBallot() {
		return ballot;
	}

	/**
	 * Sets the value of the field ballot.
	 *
	 * @param ballot The ballot to set.
	 */
	public void setBallot(final String ballot) {
		this.ballot = ballot;
	}

	/**
	 * Returns the current value of the field ballot text.
	 *
	 * @return Returns the ballotText
	 */
	public String getBallottext() {
		return ballottext;
	}

	/**
	 * Sets the value of the field ballot text.
	 *
	 * @param ballottext
	 */
	public void setBallottext(final String ballottext) {
		this.ballottext = ballottext;
	}

	/**
	 * Returns the value of the admin board that signs the information
	 *
	 * @return Returns the id of the admin board.
	 */
	public String getAdminBoardId() {
		return adminBoardId;
	}

	/**
	 * Sets the value of the admin board
	 *
	 * @param adminBoardId
	 */
	public void setAdminBoardId(final String adminBoardId) {
		this.adminBoardId = adminBoardId;
	}
}
