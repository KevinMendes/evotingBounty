/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.util.Base64;

import org.apache.commons.io.output.FileWriterWithEncoding;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;

public class CSVSigner {

	public static final boolean APPEND = true;

	/**
	 * Signs a given CSV file with a private key. The signature is then appended to the CSV file in Base64 encoded format.
	 */
	public void sign(PrivateKey privateKey, Path csvFilePath) throws IOException, GeneralCryptoLibException {

		validateFilePath(csvFilePath);

		File csvFile = csvFilePath.toFile();
		byte[] signature = signFile(privateKey, csvFile);
		appendSignatureToFile(csvFilePath, signature);
	}

	private void appendSignatureToFile(final Path csvFilePath, final byte[] signature) throws IOException {
		try (PrintWriter output = new PrintWriter(new FileWriterWithEncoding(csvFilePath.toString(), StandardCharsets.UTF_8, APPEND))) {
			String signatureB64 = Base64.getEncoder().encodeToString(signature);
			output.print("\n" + signatureB64);
		}
	}

	private byte[] signFile(final PrivateKey privateKey, final File csvFile) throws IOException, GeneralCryptoLibException {
		final byte[] signature;
		try (FileInputStream csvFileIn = new FileInputStream(csvFile)) {

			AsymmetricService asymmetricService = new AsymmetricService();
			signature = asymmetricService.sign(privateKey, csvFileIn);
		}
		return signature;
	}

	private void validateFilePath(final Path csvFilePath) throws IOException {
		if (csvFilePath == null) {
			throw new IOException("Error to validate CSV file path. The given file path cannot be null.");
		} else if (!csvFilePath.toFile().exists()) {
			throw new IOException("Error to validate CSV file path. The given file path " + csvFilePath.toString() + ", should exist.");
		}
	}
}
