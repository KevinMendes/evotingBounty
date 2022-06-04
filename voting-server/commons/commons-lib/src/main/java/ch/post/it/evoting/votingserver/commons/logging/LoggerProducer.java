/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.logging;

import javax.enterprise.inject.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logger producer.
 */
public class LoggerProducer {

	/**
	 * Develop logger name.
	 */
	public static final String LOGGER_NAME_STANDARD = "std";

	/**
	 * Instantiates a develop logger.
	 *
	 * @return a develop logger.
	 */
	@Produces
	public Logger getDevelopLoggingInstance() {
		return LoggerFactory.getLogger(LOGGER_NAME_STANDARD);
	}

}
