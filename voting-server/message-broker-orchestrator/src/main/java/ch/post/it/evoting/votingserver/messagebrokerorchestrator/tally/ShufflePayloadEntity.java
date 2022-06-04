/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.messagebrokerorchestrator.tally;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "SHUFFLE_PAYLOAD")
public class ShufflePayloadEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "shufflePayloadSeq")
	@SequenceGenerator(name = "shufflePayloadSeq", sequenceName = "SHUFFLE_PAYLOAD_SEQ", allocationSize = 1)
	private Long id;

	@NotNull
	private String electionEventId;

	@NotNull
	private String ballotBoxId;

	@NotNull
	private int nodeId;

	@Column(name = "PAYLOAD")
	private byte[] shufflePayload;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "INITIAL_PAYLOAD_FK_ID")
	InitialPayloadEntity initialPayload;

	@Version
	@Column(name = "CHANGE_CONTROL_ID")
	private int version;

	@SuppressWarnings("unused") // No-argument constructor to keep JPA happy.
	protected ShufflePayloadEntity() {
	}

	public ShufflePayloadEntity(final String electionEventId, final String ballotBoxId, final int nodeId,
			final byte[] shufflePayload, final InitialPayloadEntity initialPayloadEntity) {
		this.electionEventId = validateUUID(electionEventId);
		this.ballotBoxId = validateUUID(ballotBoxId);
		this.nodeId = nodeId;
		this.shufflePayload = checkNotNull(shufflePayload);
		this.initialPayload = initialPayloadEntity;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public int getNodeId() {
		return nodeId;
	}

	public byte[] getShufflePayload() {
		return shufflePayload;
	}

	public InitialPayloadEntity getInitialPayload() {
		return initialPayload;
	}
}
