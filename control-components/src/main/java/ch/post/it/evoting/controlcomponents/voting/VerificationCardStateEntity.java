/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import ch.post.it.evoting.controlcomponents.VerificationCardEntity;

@Entity
@Table(name = "VERIFICATION_CARD_STATE")
public class VerificationCardStateEntity {

	@Id
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "VERIFICATION_CARD_FK_ID")
	private VerificationCardEntity verificationCardEntity;

	@Convert(converter = BooleanConverter.class)
	private boolean partiallyDecrypted;

	@Convert(converter = BooleanConverter.class)
	private boolean lccShareCreated;

	private int confirmationAttempts;

	@Convert(converter = BooleanConverter.class)
	private boolean confirmed;

	@Version
	private Integer changeControlId;

	public VerificationCardStateEntity() {
		this.partiallyDecrypted = false;
		this.lccShareCreated = false;
		this.confirmed = false;
		this.confirmationAttempts = 0;
	}

	public void setVerificationCardEntity(final VerificationCardEntity verificationCardEntity) {
		this.verificationCardEntity = verificationCardEntity;
	}

	public boolean isPartiallyDecrypted() {
		return partiallyDecrypted;
	}

	public boolean isLccShareCreated() {
		return lccShareCreated;
	}

	public int getConfirmationAttempts() {
		return confirmationAttempts;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setPartiallyDecrypted(boolean partiallyDecrypted) {
		this.partiallyDecrypted = partiallyDecrypted;
	}

	public void setLccShareCreated(boolean lccShareCreated) {
		this.lccShareCreated = lccShareCreated;
	}

	public void setConfirmed(final boolean confirmed) {
		this.confirmed = confirmed;
	}

	public void setConfirmationAttempts(int confirmationAttempts) {
		this.confirmationAttempts = confirmationAttempts;
	}

}
