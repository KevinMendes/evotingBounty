/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "NODE_KEYS")
public class NodeKeysEntity {

	@Id
	private String nodeId;

	@Version
	@Column(name = "CHANGE_CONTROL_ID")
	private Integer changeControlId;

	private byte[] keys;

	public NodeKeysEntity() {
		// Needed by the repository.
	}

	public NodeKeysEntity(final String nodeId, final byte[] keys) {
		this.nodeId = nodeId;
		this.keys = keys;
	}

	public byte[] getKeys() {
		return this.keys;
	}
}
