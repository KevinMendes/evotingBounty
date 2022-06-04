/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@Entity
@Table(name = "ELECTION_EVENT")
public class ElectionEventEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ELECTION_EVENT_SEQ_GENERATOR")
	@SequenceGenerator(sequenceName = "ELECTION_EVENT_SEQ", allocationSize = 1, name = "ELECTION_EVENT_SEQ_GENERATOR")
	private Long id;

	private String electionEventId;

	@Convert(converter = EncryptionGroupConverter.class)
	private GqGroup encryptionGroup;

	@Version
	private Integer changeControlId;

	public ElectionEventEntity() {
	}

	public ElectionEventEntity(final String electionEventId, final GqGroup encryptionGroup) {
		this.electionEventId = validateUUID(electionEventId);
		this.encryptionGroup = checkNotNull(encryptionGroup);
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public GqGroup getEncryptionGroup() {
		return encryptionGroup;
	}
}
