/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation;

import ch.post.it.evoting.domain.election.validation.ValidationError;

/**
 * Class representing the return values after confirming the vote.
 */
public class VoteCastResult {

	// The result of confirmation message validation
	private boolean valid;

	private String electionEventId;

	private String votingCardId;

	// The vote cast return code and its signature
	private VoteCastMessage voteCastMessage;

	// The error if the vote cannot be confirmed (i.e., election is out of date)
	private ValidationError validationError;

	private String verificationCardId;

	/**
	 * Returns the current value of the field valid.
	 *
	 * @return Returns the valid.
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Sets the value of the field valid.
	 *
	 * @param valid The valid to set.
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
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
	public void setElectionEventId(String electionEventId) {
		this.electionEventId = electionEventId;
	}

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
	public void setVotingCardId(String votingCardId) {
		this.votingCardId = votingCardId;
	}

	/**
	 * Returns the current value of the field voteCastMessage.
	 *
	 * @return Returns the voteCastMessage.
	 */
	public VoteCastMessage getVoteCastMessage() {
		return voteCastMessage;
	}

	/**
	 * Sets the value of the field voteCastMessage.
	 *
	 * @param voteCastMessage The voteCastMessage to set.
	 */
	public void setVoteCastMessage(VoteCastMessage voteCastMessage) {
		this.voteCastMessage = voteCastMessage;
	}

	/**
	 * Returns the current value of the field validationError.
	 *
	 * @return Returns the validationError.
	 */
	public ValidationError getValidationError() {
		return validationError;
	}

	/**
	 * Sets the value of the field validationError.
	 *
	 * @param validationError The validationError to set.
	 */
	public void setValidationError(ValidationError validationError) {
		this.validationError = validationError;
	}

	/**
	 * Returns the current value of the field verification card Id
	 *
	 * @return
	 */
	public String getVerificationCardId() {
		return verificationCardId;
	}

	/**
	 * Sets the value of the field verificationCardId
	 *
	 * @param verificationCardId
	 */
	public void setVerificationCardId(String verificationCardId) {
		this.verificationCardId = verificationCardId;
	}

}
