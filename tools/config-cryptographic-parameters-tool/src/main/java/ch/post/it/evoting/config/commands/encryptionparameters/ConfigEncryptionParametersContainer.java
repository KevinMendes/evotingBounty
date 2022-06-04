/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.encryptionparameters;

import java.nio.file.Path;

/**
 * Encapsulates the data.
 */
public final class ConfigEncryptionParametersContainer {

	private final Path p12Path;
	private final Path seedPath;
	private final Path seedSignaturePath;
	private final Path outputPath;
	private final Path trustedCAPath;

	public ConfigEncryptionParametersContainer(final Path p12Path, final Path seedPath, final Path seedSignaturePath, final Path outputPath,
			final Path trustedCAPath) {
		this.p12Path = p12Path;
		this.seedPath = seedPath;
		this.seedSignaturePath = seedSignaturePath;
		this.outputPath = outputPath;
		this.trustedCAPath = trustedCAPath;
	}

	public Path getP12Path() {
		return p12Path;
	}

	public Path getSeedPath() {
		return seedPath;
	}

	public Path getSeedSignaturePath() {
		return seedSignaturePath;
	}

	public Path getOutputPath() {
		return outputPath;
	}

	public Path getTrustedCAPath() {
		return trustedCAPath;
	}
}
