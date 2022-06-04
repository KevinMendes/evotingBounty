/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
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

import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;

@Entity
@Table(name = "VERIFICATION_CARD_SET")
public class VerificationCardSetEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VERIFICATION_CARD_SET_SEQ_GENERATOR")
	@SequenceGenerator(sequenceName = "VERIFICATION_CARD_SET_SEQ", allocationSize = 1, name = "VERIFICATION_CARD_SET_SEQ_GENERATOR")
	private Long id;

	private String verificationCardSetId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ELECTION_EVENT_FK_ID", referencedColumnName = "ID")
	private ElectionEventEntity electionEventEntity;

	@Convert(converter = AllowListConverter.class)
	@Column(name = "ALLOW_LIST_JSON")
	private List<String> allowList;

	@Convert(converter = CombinedCorrectnessInformationConverter.class)
	private CombinedCorrectnessInformation combinedCorrectnessInformation;

	@Version
	private Integer changeControlId;

	public VerificationCardSetEntity() {
	}

	public VerificationCardSetEntity(final String verificationCardSetId, final ElectionEventEntity electionEventEntity) {
		this.verificationCardSetId = validateUUID(verificationCardSetId);
		this.electionEventEntity = checkNotNull(electionEventEntity);
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public ElectionEventEntity getElectionEventEntity() {
		return electionEventEntity;
	}

	public List<String> getAllowList() {
		return allowList;
	}

	public void setAllowList(final List<String> allowList) {
		this.allowList = checkNotNull(allowList);
	}

	public void setCombinedCorrectnessInformation(final CombinedCorrectnessInformation combinedCorrectnessInformation) {
		this.combinedCorrectnessInformation = checkNotNull(combinedCorrectnessInformation);
	}

	public CombinedCorrectnessInformation getCombinedCorrectnessInformation() {
		return combinedCorrectnessInformation;
	}
}
