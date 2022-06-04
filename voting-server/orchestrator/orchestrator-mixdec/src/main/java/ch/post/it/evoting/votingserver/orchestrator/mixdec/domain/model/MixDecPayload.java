/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * The results of mixing and decrypting a ballot box in one node.
 */
@Entity
@Table(name = "MIXDEC_PAYLOAD")
public class MixDecPayload {

	@EmbeddedId
	private MixDecId id;

	@Column(name = "PAYLOAD")
	@Lob
	private byte[] payload;

	@Column(name = "IS_INITIAL")
	private boolean initial;

	public String getNodeId() {
		return id.getNodeId();
	}

	public String getElectionEventId() {
		return id.getElectionEventId();
	}

	public String getBallotBoxId() {
		return id.getBallotBoxId();
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(final byte[] payload) {
		this.payload = payload;
	}

	public boolean isInitial() {
		return initial;
	}

	public void setInitial(final boolean initial) {
		this.initial = initial;
	}

	public void setId(final MixDecId id) {
		this.id = id;
	}

}
