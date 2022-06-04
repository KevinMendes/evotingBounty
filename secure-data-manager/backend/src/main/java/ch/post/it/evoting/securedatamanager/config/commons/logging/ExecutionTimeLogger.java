/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionTimeLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionTimeLogger.class);

	private final String operationName;

	private long lastActionInvocationTimestamp = System.currentTimeMillis();

	private final long firstActionInvocationTimestamp = lastActionInvocationTimestamp;

	public ExecutionTimeLogger(final String operationName) {
		this.operationName = operationName;
	}

	public void log(final String actionName) {
		lastActionInvocationTimestamp = logAction(actionName, lastActionInvocationTimestamp);
	}

	private long logAction(final String actionName, long invocationTimestamp) {
		long currentActionInvocationTimestamp = System.currentTimeMillis();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(operationName + "." + actionName + ": " + (currentActionInvocationTimestamp - invocationTimestamp));
		}
		return currentActionInvocationTimestamp;
	}

	public void resetTimer() {
		lastActionInvocationTimestamp = System.currentTimeMillis();
	}

	public void logTotalElapsedTime() {
		logAction("totalTime", firstActionInvocationTimestamp);
	}

}
