/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters;

import java.nio.file.Path;

public class NodeContributionsPath {

	private Path input;

	private Path output;

	public NodeContributionsPath(final Path input, final Path output) {
		this.setInput(input);
		this.setOutput(output);
	}

	public Path getInput() {
		return input;
	}

	public void setInput(final Path input) {
		this.input = input;
	}

	public Path getOutput() {
		return output;
	}

	public void setOutput(final Path output) {
		this.output = output;
	}

}
