/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.encryptionparameters;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.VerifiableElGamalEncryptionParameters;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

/**
 * Allows the generation of the {@link VerifiableElGamalEncryptionParameters}.
 */
@Service
public final class ConfigEncryptionParametersGenerator {

	private final ElGamalService elGamalService;

	@Autowired
	public ConfigEncryptionParametersGenerator(final ElGamalService elGamalService) {
		this.elGamalService = elGamalService;
	}

	/**
	 * Generates the {@link VerifiableElGamalEncryptionParameters}.
	 *
	 * @param seedPath the path to the file containing the seed needed by the generation process.
	 * @return the generated verifiable encryption parameters.
	 */
	public VerifiableElGamalEncryptionParameters generate(final Path seedPath) {
		checkNotNull(seedPath);

		final String seed = readSeedFromFile(seedPath);
		final GqGroup encryptionParameters = elGamalService.getEncryptionParameters(seed);

		try {
			return new VerifiableElGamalEncryptionParameters(encryptionParameters.getP(), encryptionParameters.getQ(),
					encryptionParameters.getGenerator().getValue(), seed);
		} catch (GeneralCryptoLibException e) {
			throw new CryptoLibException(e);
		}
	}

	private String readSeedFromFile(final Path seedPath) {
		final List<String> lines;
		try {
			lines = Files.readAllLines(seedPath);
		} catch (IOException e) {
			throw new UncheckedIOException(String.format("Failed to read seed located at: %s", seedPath), e);
		}

		if (lines.isEmpty()) {
			throw new IllegalArgumentException("The seed must not be an empty string.");
		}

		return lines.get(0).trim();
	}

}
