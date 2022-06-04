/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.domain.model.information;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class VotingCardList.
 */
public class VotingCardList {

	/**
	 * The voting cards.
	 */
	private final List<VotingCard> votingCards = new ArrayList<>();

	/**
	 * The pagination.
	 */
	private ListPagination pagination;

	/**
	 * Gets the voting cards.
	 *
	 * @return Returns the votingCards.
	 */
	public List<VotingCard> getVotingCards() {
		return votingCards;
	}

	/**
	 * Gets the pagination.
	 *
	 * @return Returns the pagination.
	 */
	public ListPagination getPagination() {
		return pagination;
	}

	/**
	 * Sets the pagination.
	 *
	 * @param pagination The pagination to set.
	 */
	public void setPagination(final ListPagination pagination) {
		this.pagination = pagination;
	}
}
