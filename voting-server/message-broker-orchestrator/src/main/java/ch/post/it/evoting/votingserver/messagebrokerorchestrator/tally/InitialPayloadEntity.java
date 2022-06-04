/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.messagebrokerorchestrator.tally;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "INITIAL_PAYLOAD")
public class InitialPayloadEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "initialPayloadSeq")
	@SequenceGenerator(name = "initialPayloadSeq", sequenceName = "INITIAL_PAYLOAD_SEQ", allocationSize = 1)
	private Long id;

	private String electionEventId;

	private String ballotBoxId;

	@Column(name = "PAYLOAD")
	private byte[] initialPayload;

	@Version
	private Integer changeControlId ;

	@SuppressWarnings("unused") // No-argument constructor to keep JPA happy.
	protected InitialPayloadEntity() {
	}

	public InitialPayloadEntity(final String electionEventId, final String ballotBoxId, final byte[] initialPayload) {
		this.electionEventId = validateUUID(electionEventId);
		this.ballotBoxId = validateUUID(ballotBoxId);
		this.initialPayload = checkNotNull(initialPayload);
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public byte[] getInitialPayload() {
		return initialPayload;
	}
}
