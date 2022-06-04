/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static java.text.MessageFormat.format;

import java.io.IOException;

/**
 * Utility class for working with destinations.
 */
final class Destinations {
	private static final String DEFAULT_EXCHANGE = "";

	private static final String DEFAULT_ROUTING_KEY = "";

	private Destinations() {
	}

	/**
	 * Returns the exchange of a given destination.
	 *
	 * @param destination the destination
	 * @return the exchange.
	 */
	public static String getExchange(Destination destination) {
		if (destination instanceof Queue) {
			return DEFAULT_EXCHANGE;
		} else if (destination instanceof Topic) {
			return destination.name();
		} else {
			throw new IllegalArgumentException(format("Unsupported destination ''{0}''.", destination));
		}
	}

	/**
	 * Returns the routing key for a given destination.
	 *
	 * @param destination the destination
	 * @return the routing key.
	 */
	public static String getRoutingKey(Destination destination) {
		if (destination instanceof Queue) {
			return destination.name();
		} else if (destination instanceof Topic) {
			return DEFAULT_ROUTING_KEY;
		} else {
			throw new IllegalArgumentException(format("Unsupported destination ''{0}''.", destination));
		}
	}

	/**
	 * Returns whether a given exception means that destination does not exist.
	 *
	 * @param e the exception
	 * @return destination does not exist.
	 */
	public static boolean isDestinationNotFound(IOException e) {
		String message = e.getMessage();
		return message != null && (e.getMessage().contains("no queue") || e.getMessage().contains("no exchange"));
	}
}
