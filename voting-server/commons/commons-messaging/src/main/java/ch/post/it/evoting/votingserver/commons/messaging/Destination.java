/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

/**
 * Destination.
 */
@Immutable
public abstract class Destination {
	private final String name;

	/**
	 * Constructor. For internal use only.
	 *
	 * @param name the name
	 */
	Destination(String name) {
		this.name = requireNonNull(name, "Name is null.");
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Destination other = (Destination) obj;
		if (name == null) {
			return other.name == null;
		} else {
			return name.equals(other.name);
		}
	}

	@Override
	public final int hashCode() {
		return Objects.hash(name);
	}

	/**
	 * Returns the name.
	 *
	 * @return the name.
	 */
	public final String name() {
		return name;
	}

	@Override
	public final String toString() {
		return getClass().getSimpleName() + " [name=" + name + "]";
	}
}
