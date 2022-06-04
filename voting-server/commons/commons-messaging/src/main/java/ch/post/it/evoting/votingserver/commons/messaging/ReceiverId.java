/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

/**
 * Receiver identifier.
 */
@Immutable
final class ReceiverId {
	private final Destination destination;

	private final MessageListener listener;

	/**
	 * Constructor.
	 *
	 * @param destination
	 * @param listener
	 */
	public ReceiverId(Destination destination, MessageListener listener) {
		this.destination = destination;
		this.listener = listener;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ReceiverId other = (ReceiverId) obj;
		if (listener == null) {
			if (other.listener != null) {
				return false;
			}
		} else if (listener != other.listener) {
			return false;
		}
		if (destination == null) {
			return other.destination == null;
		} else {
			return destination.equals(other.destination);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(listener, destination);
	}
}
