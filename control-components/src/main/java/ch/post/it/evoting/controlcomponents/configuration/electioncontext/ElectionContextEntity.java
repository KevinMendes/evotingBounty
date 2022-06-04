/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.configuration.electioncontext;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import ch.post.it.evoting.controlcomponents.ElectionEventEntity;

@Entity
@Table(name = "ELECTION_EVENT_CONTEXT")
public class ElectionContextEntity {

	@Id
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "ELECTION_EVENT_FK_ID")
	private ElectionEventEntity electionEventEntity;

	private byte[] combinedControlComponentPublicKeys;

	private byte[] electoralBoardPublicKey;

	private byte[] electionPublicKey;

	private byte[] choiceReturnCodesEncryptionPublicKey;

	private LocalDateTime startTime;

	private LocalDateTime finishTime;

	@Version
	@Column(name = "CHANGE_CONTROL_ID")
	private Integer changeControlId;

	public ElectionContextEntity() {

	}

	private ElectionContextEntity(final ElectionEventEntity electionEventEntity, final byte[] combinedControlComponentPublicKeys,
			final byte[] electoralBoardPublicKey, final byte[] electionPublicKey, final byte[] choiceReturnCodesEncryptionPublicKey,
			final LocalDateTime startTime, final LocalDateTime finishTime) {

		this.electionEventEntity = checkNotNull(electionEventEntity);
		this.combinedControlComponentPublicKeys = checkNotNull(combinedControlComponentPublicKeys);
		this.electoralBoardPublicKey = checkNotNull(electoralBoardPublicKey);
		this.electionPublicKey = checkNotNull(electionPublicKey);
		this.choiceReturnCodesEncryptionPublicKey = checkNotNull(choiceReturnCodesEncryptionPublicKey);

		checkNotNull(startTime);
		checkNotNull(finishTime);
		checkArgument(startTime.isBefore(finishTime), "Start time must be before finish time.");

		this.startTime = startTime;
		this.finishTime = finishTime;
	}

	public byte[] getCombinedControlComponentPublicKeys() {
		return combinedControlComponentPublicKeys;
	}

	public byte[] getElectoralBoardPublicKey() {
		return electoralBoardPublicKey;
	}

	public byte[] getElectionPublicKey() {
		return electionPublicKey;
	}

	public byte[] getChoiceReturnCodesEncryptionPublicKey() {
		return choiceReturnCodesEncryptionPublicKey;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getFinishTime() {
		return finishTime;
	}

	public static class Builder {

		private ElectionEventEntity electionEventEntity;
		private byte[] combinedControlComponentPublicKeys;
		private byte[] electoralBoardPublicKey;
		private byte[] electionPublicKey;
		private byte[] choiceReturnCodesEncryptionPublicKey;
		private LocalDateTime startTime;
		private LocalDateTime finishTime;

		public Builder() {
			// Do nothing
		}

		public Builder setElectionEventEntity(final ElectionEventEntity electionEventEntity) {
			this.electionEventEntity = checkNotNull(electionEventEntity);
			return this;
		}

		public Builder setCombinedControlComponentPublicKey(final byte[] combinedControlComponentPublicKeys) {
			checkNotNull(combinedControlComponentPublicKeys);
			this.combinedControlComponentPublicKeys = combinedControlComponentPublicKeys;
			return this;
		}

		public Builder setElectoralBoardPublicKey(final byte[] electoralBoardPublicKey) {
			checkNotNull(electoralBoardPublicKey);
			this.electoralBoardPublicKey = electoralBoardPublicKey;
			return this;
		}

		public Builder setElectionPublicKey(final byte[] electionPublicKey) {
			checkNotNull(electionPublicKey);
			this.electionPublicKey = electionPublicKey;
			return this;
		}

		public Builder setChoiceReturnCodesEncryptionPublicKey(final byte[] choiceReturnCodesEncryptionPublicKey) {
			checkNotNull(choiceReturnCodesEncryptionPublicKey);
			this.choiceReturnCodesEncryptionPublicKey = choiceReturnCodesEncryptionPublicKey;
			return this;
		}

		public Builder setStartTime(final LocalDateTime startTime) {
			checkNotNull(startTime);
			this.startTime = startTime;
			return this;
		}

		public Builder setFinishTime(final LocalDateTime finishTime) {
			checkNotNull(finishTime);
			this.finishTime = finishTime;
			return this;
		}

		public ElectionContextEntity build() {
			return new ElectionContextEntity(electionEventEntity, combinedControlComponentPublicKeys, electoralBoardPublicKey, electionPublicKey,
					choiceReturnCodesEncryptionPublicKey, startTime, finishTime);
		}
	}
}
