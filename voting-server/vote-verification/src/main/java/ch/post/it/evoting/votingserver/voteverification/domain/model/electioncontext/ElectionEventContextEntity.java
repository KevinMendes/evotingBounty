/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.domain.model.electioncontext;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "ELECTION_EVENT_CONTEXT")
public class ElectionEventContextEntity {

	@Id
	private String electionEventId;

	private byte[] combinedControlComponentPublicKeys;

	private byte[] electoralBoardPublicKey;

	private byte[] electionPublicKey;

	private byte[] choiceReturnCodesEncryptionPublicKey;

	private LocalDateTime startTime;

	private LocalDateTime finishTime;

	@Version
	@Column(name = "CHANGE_CONTROL_ID")
	private Integer changeControlId;

	public String getElectionEventId() {
		return electionEventId;
	}

	public void setElectionEventId(final String electionEventId) {
		this.electionEventId = electionEventId;
	}

	public byte[] getCombinedControlComponentPublicKeys() {
		return combinedControlComponentPublicKeys;
	}

	public void setCombinedControlComponentPublicKeys(final byte[] combinedControlComponentPublicKeys) {
		this.combinedControlComponentPublicKeys = combinedControlComponentPublicKeys;
	}

	public byte[] getElectoralBoardPublicKey() {
		return electoralBoardPublicKey;
	}

	public void setElectoralBoardPublicKey(final byte[] electoralBoardPublicKey) {
		this.electoralBoardPublicKey = electoralBoardPublicKey;
	}

	public byte[] getElectionPublicKey() {
		return electionPublicKey;
	}

	public void setElectionPublicKey(final byte[] electionPublicKey) {
		this.electionPublicKey = electionPublicKey;
	}

	public byte[] getChoiceReturnCodesEncryptionPublicKey() {
		return choiceReturnCodesEncryptionPublicKey;
	}

	public void setChoiceReturnCodesEncryptionPublicKey(final byte[] choiceReturnCodesEncryptionPublicKey) {
		this.choiceReturnCodesEncryptionPublicKey = choiceReturnCodesEncryptionPublicKey;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(final LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(final LocalDateTime finishTime) {
		this.finishTime = finishTime;
	}

	public Integer getChangeControlId() {
		return changeControlId;
	}

	public void setChangeControlId(final Integer changeControlId) {
		this.changeControlId = changeControlId;
	}
}
