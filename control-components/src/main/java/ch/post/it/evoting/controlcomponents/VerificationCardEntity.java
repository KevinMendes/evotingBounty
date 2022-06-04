/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import ch.post.it.evoting.controlcomponents.voting.VerificationCardStateEntity;

@Entity
@Table(name = "VERIFICATION_CARD")
public class VerificationCardEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VERIFICATION_CARD_SEQ_GENERATOR")
	@SequenceGenerator(sequenceName = "VERIFICATION_CARD_SEQ", allocationSize = 1, name = "VERIFICATION_CARD_SEQ_GENERATOR")
	private Long id;

	private String verificationCardId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "VERIFICATION_CARD_SET_FK_ID", referencedColumnName = "ID")
	private VerificationCardSetEntity verificationCardSetEntity;

	private byte[] verificationCardPublicKey;

	@OneToOne(mappedBy = "verificationCardEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
	@PrimaryKeyJoinColumn
	private VerificationCardStateEntity verificationCardStateEntity;

	@Version
	private Integer changeControlId;

	public VerificationCardEntity() {
	}

	public VerificationCardEntity(
			final String verificationCardId,
			final VerificationCardSetEntity verificationCardSetEntity,
			final byte[] verificationCardPublicKey) {
		this.verificationCardId = validateUUID(verificationCardId);
		this.verificationCardSetEntity = verificationCardSetEntity;
		this.verificationCardPublicKey = checkNotNull(verificationCardPublicKey);
	}

	public String getVerificationCardId() {
		return verificationCardId;
	}

	public byte[] getVerificationCardPublicKey() {
		return verificationCardPublicKey;
	}

	public VerificationCardSetEntity getVerificationCardSetEntity() {
		return verificationCardSetEntity;
	}

	public VerificationCardStateEntity getVerificationCardStateEntity() {
		return verificationCardStateEntity;
	}

	public void setVerificationCardStateEntity(final VerificationCardStateEntity verificationCardStateEntity) {
		this.verificationCardStateEntity = verificationCardStateEntity;
	}
}
