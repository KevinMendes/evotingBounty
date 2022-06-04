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
@Table(name = "CCR_RETURN_CODES_KEYS")
public class CcrjReturnCodesKeysEntity {

	@Id
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "ELECTION_EVENT_FK_ID")
	private ElectionEventEntity electionEventEntity;

	@Version
	private Integer changeControlId;

	private byte[] ccrjReturnCodesGenerationSecretKey;

	private byte[] ccrjChoiceReturnCodesEncryptionKeyPair;

	public CcrjReturnCodesKeysEntity() {
		// Needed by the repository.
	}

	public CcrjReturnCodesKeysEntity(
			final byte[] ccrjReturnCodesGenerationSecretKey,
			final byte[] ccrjChoiceReturnCodesEncryptionKeyPair) {
		this.ccrjReturnCodesGenerationSecretKey = checkNotNull(ccrjReturnCodesGenerationSecretKey);
		this.ccrjChoiceReturnCodesEncryptionKeyPair = checkNotNull(ccrjChoiceReturnCodesEncryptionKeyPair);
	}

	public byte[] getCcrjReturnCodesGenerationSecretKey() {
		return ccrjReturnCodesGenerationSecretKey;
	}

	public byte[] getCcrjChoiceReturnCodesEncryptionKeyPair() {
		return ccrjChoiceReturnCodesEncryptionKeyPair;
	}

	public void setElectionEventEntity(final ElectionEventEntity electionEventEntity) {
		this.electionEventEntity = checkNotNull(electionEventEntity);
	}
}
