/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state;

/**
 * The state of a voting card to be checked during the client-server authentication.
 */
public enum VotingCardStates {
	NONE,
	NOT_SENT,
	SENT_BUT_NOT_CAST,
	CHOICE_CODES_FAILED,
	WRONG_BALLOT_CASTING_KEY,
	CAST,
	BLOCKED,
	NOT_FOUND
}
