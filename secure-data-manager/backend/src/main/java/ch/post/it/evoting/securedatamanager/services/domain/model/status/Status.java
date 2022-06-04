/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model.status;

/**
 * Defines a set of status for entities.
 */
public enum Status {
	NEW(0),
	LOCKED(1),
	READY(2),
	APPROVED(3),
	PRECOMPUTING(4),
	PRECOMPUTED(5),
	COMPUTING(6),
	COMPUTED(7),
	VCS_DOWNLOADED(8),
	GENERATING(9),
	GENERATED(10),
	CONSTITUTED(11),
	SIGNED(12),
	MIXING(13),
	MIXED(14),
	BB_DOWNLOADED(15),
	DECRYPTING(16),
	DECRYPTED(17);

	private final int index;

	Status(final int index) {
		this.index = index;
	}

	/**
	 * Returns whether this status is before a given one.
	 *
	 * @param other the other status
	 * @return is before.
	 */
	public boolean isBefore(final Status other) {
		return index < other.index;
	}
}
