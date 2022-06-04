/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config;

/**
 * Represents an execution command line.
 * <p>
 * A Command contains the following parts:
 * <ul>
 * <li>A command identifier.</li>
 * <li>A parameters map</li>
 * </ul>
 */
public class Command {

	private final MutuallyExclusiveCommand identifier;

	private final Parameters parameters;

	/**
	 * Instantiates a new Command.
	 *
	 * @param command    the command.
	 * @param parameters the parameters.
	 */
	public Command(final MutuallyExclusiveCommand command, final Parameters parameters) {
		identifier = command;
		this.parameters = parameters;
	}

	/**
	 * Gets identifier.
	 *
	 * @return the identifier
	 */
	public MutuallyExclusiveCommand getIdentifier() {
		return identifier;
	}

	/**
	 * Gets parameters.
	 *
	 * @return the parameters
	 */
	public Parameters getParameters() {
		return parameters;
	}

}
