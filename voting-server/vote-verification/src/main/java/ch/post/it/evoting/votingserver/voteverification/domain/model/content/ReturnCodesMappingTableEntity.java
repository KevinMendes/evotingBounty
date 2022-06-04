/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.votingserver.voteverification.domain.model.content;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "RETURN_CODES_MAPPPING_TABLE", uniqueConstraints = {
		@UniqueConstraint(name = "RETURN_CODES_MAPPPING_TABLE_UK1", columnNames = { "ELECTION_EVENT_ID", "VERIFICATION_CARD_SET_ID" }) })
public class ReturnCodesMappingTableEntity {

	@Id
	@Column(name = "VERIFICATION_CARD_SET_ID")
	private String verificationCardSetId;

	@Column(name = "ELECTION_EVENT_ID")
	@NotNull
	private String electionEventId;

	@Column(name = "RETURN_CODES_MAPPING_TABLE")
	@NotNull
	private byte[] returnCodesMappingTable;

	@Version
	@Column(name = "CHANGE_CONTROL_ID")
	private Integer changeControlId;

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public void setVerificationCardSetId(final String verificationCardSetId) {
		this.verificationCardSetId = verificationCardSetId;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public void setElectionEventId(final String electionEventId) {
		this.electionEventId = electionEventId;
	}

	public byte[] getReturnCodesMappingTable() {
		return returnCodesMappingTable;
	}

	public void setReturnCodesMappingTable(final byte[] returnCodesMappingTable) {
		this.returnCodesMappingTable = returnCodesMappingTable;
	}

	public Integer getChangeControlId() {
		return changeControlId;
	}

	public void setChangeControlId(final Integer changeControlId) {
		this.changeControlId = changeControlId;
	}
}