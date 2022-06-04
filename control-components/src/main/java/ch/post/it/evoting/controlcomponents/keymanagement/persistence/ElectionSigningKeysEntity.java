/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "ELECTION_SIGNING_KEYS")
@IdClass(ElectionSigningKeysEntityPrimaryKey.class)
public class ElectionSigningKeysEntity {

	@Id
	private String nodeId;

	@Id
	private String electionEventId;

	@Version
	@Column(name = "CHANGE_CONTROL_ID")
	private Integer changeControlId;

	private byte[] keys;

	private byte[] password;

	public ElectionSigningKeysEntity() {
		// Needed by the repository.
	}

	public ElectionSigningKeysEntity(final String nodeId, final String electionEventId, final byte[] keys, final byte[] password) {
		this.nodeId = nodeId;
		this.electionEventId = electionEventId;
		this.keys = keys;
		this.password = password;
	}

	public byte[] getPassword() {
		return this.password;
	}

	public byte[] getKeys() {
		return this.keys;
	}
}
