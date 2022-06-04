/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "CCM_ELECTION_KEY")
public class CcmjElectionKeysEntity {

	@Id
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "ELECTION_EVENT_FK_ID")
	private ElectionEventEntity electionEventEntity;

	@Version
	private Integer changeControlId;

	private byte[] ccmjElectionKeyPair;

	public CcmjElectionKeysEntity() {
		// Needed by the repository.
	}

	public CcmjElectionKeysEntity(final byte[] ccmjElectionKeyPair) {
		this.ccmjElectionKeyPair = checkNotNull(ccmjElectionKeyPair);
	}

	public byte[] getCcmjElectionKeyPair() {
		return ccmjElectionKeyPair;
	}

	public void setElectionEventEntity(final ElectionEventEntity electionEventEntity) {
		this.electionEventEntity = checkNotNull(electionEventEntity);
	}
}
