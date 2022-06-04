/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.domain.model.information;

/**
 * The Class VotingCard.
 */
public class VotingCard {

	/**
	 * The voting card id.
	 */
	private String id;

	/**
	 * The voting card set id.
	 */
	private String votingCardSetId;

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Gets the voting card set id.
	 *
	 * @return Returns the votingCardSetId.
	 */
	public String getVotingCardSetId() {
		return votingCardSetId;
	}

	/**
	 * Sets the voting card set id.
	 *
	 * @param votingCardSetId The votingCardSetId to set.
	 */
	public void setVotingCardSetId(final String votingCardSetId) {
		this.votingCardSetId = votingCardSetId;
	}
}
