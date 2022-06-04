/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.post.it.evoting.domain.election.errors.SemanticErrorGroup;
import ch.post.it.evoting.domain.election.errors.SyntaxErrorGroup;
import ch.post.it.evoting.votingserver.commons.domain.model.Constants;

/**
 * Entity for representing the state of a voting card.
 */
@Entity
@Table(name = "VOTING_CARD_STATE")
@IdClass(value = VotingCardStatePK.class)
public class VotingCardState {
	/**
	 * Maximum number of attempts.
	 */
	public static final long MAX_ATTEMPTS = 5L;

	// The tenant identifier.
	@Id
	@Column(name = "TENANT_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String tenantId;

	// The election event identifier.
	@Id
	@Column(name = "ELECTION_EVENT_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String electionEventId;

	// The identifier of a voting card
	@Id
	@Column(name = "VOTING_CARD_ID")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SemanticErrorGroup.class)
	private String votingCardId;

	// The current state of the voting card
	@Column(name = "STATE")
	@NotNull(groups = SyntaxErrorGroup.class)
	@Enumerated(EnumType.STRING)
	private VotingCardStates state;

	// The number of attempts of the voting card
	@Column(name = "ATTEMPTS")
	@NotNull(groups = SyntaxErrorGroup.class)
	private long attempts;

	/**
	 * Returns the current value of the field votingCardId.
	 *
	 * @return Returns the votingCardId.
	 */
	public String getVotingCardId() {
		return votingCardId;
	}

	/**
	 * Sets the value of the field votingCardId.
	 *
	 * @param votingCardId The votingCardId to set.
	 */
	public void setVotingCardId(final String votingCardId) {
		this.votingCardId = votingCardId;
	}

	/**
	 * Returns the current value of the field state.
	 *
	 * @return Returns the state.
	 */
	public VotingCardStates getState() {
		return state;
	}

	/**
	 * Sets the value of the field state.
	 *
	 * @param state The state to set.
	 */
	public void setState(final VotingCardStates state) {
		this.state = state;
	}

	/**
	 * Returns the current value of the field tenantId.
	 *
	 * @return Returns the tenantId.
	 */
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * Sets the value of the field tenantId.
	 *
	 * @param tenantId The tenantId to set.
	 */
	public void setTenantId(final String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * Returns the current value of the field electionEventId.
	 *
	 * @return Returns the electionEventId.
	 */
	public String getElectionEventId() {
		return electionEventId;
	}

	/**
	 * Sets the value of the field electionEventId.
	 *
	 * @param electionEventId The electionEventId to set.
	 */
	public void setElectionEventId(final String electionEventId) {
		this.electionEventId = electionEventId;
	}

	/**
	 * Returns the current value of the field attempts.
	 *
	 * @return Returns the attempts.
	 */
	public long getAttempts() {
		return attempts;
	}

	/**
	 * Sets the value of the field attempts.
	 *
	 * @param attempts The attempts to set.
	 */
	public void setAttempts(final long attempts) {
		this.attempts = attempts;
	}

	/**
	 * Increments the number of the attempts.
	 */
	public void incrementAttempts() {
		attempts++;
	}

	/**
	 * Returns the number of remaining attempts. If the number of attempts obtained with
	 * {@link #getAttempts()} is greater than {@link #MAX_ATTEMPTS} then {@code 0} is returned.
	 *
	 * @return the number of remaining attempts.
	 */
	public long getRemainingAttempts() {
		return MAX_ATTEMPTS > attempts ? MAX_ATTEMPTS - attempts : 0;
	}

	/**
	 * Returns whether there are remaining attempts.
	 *
	 * @return there are remaining attempts.
	 */
	public boolean hasRemainingAttempts() {
		return getRemainingAttempts() > 0;
	}
}
