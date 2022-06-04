/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.platform;

/**
 * Used to represent the state of whether the logging system has been initialized or not.
 */
public class LoggingInitializationState {

	private boolean initialized;

	/**
	 * @return the current logging state.
	 */
	public boolean getInitialized() {
		return initialized;
	}

	/**
	 * Sets the current logging state to the received value.
	 *
	 * @param initialized the value to set the logging state to.
	 */
	public void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}
}
