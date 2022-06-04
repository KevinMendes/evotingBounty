/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Base64;

import org.apache.commons.io.input.ReversedLinesFileReader;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;

public class CSVVerifier {

	/**
	 * Verifies the signature of a given CSV file, and removes the signature from it.
	 */
	public boolean verify(PublicKey publicKey, Path csvSignedFile) throws IOException, GeneralCryptoLibException {

		validateFilePath(csvSignedFile);

		File csvFile = csvSignedFile.toFile();
		String signatureB64 = getSignatureFromFile(csvFile);
		removeSignatureFromFile(signatureB64, csvFile);

		return validateSignature(publicKey, signatureB64, csvFile);
	}

	private boolean validateSignature(final PublicKey publicKey, final String signatureB64, final File csvFile)
			throws IOException, GeneralCryptoLibException {
		try (FileInputStream csvFileIn = new FileInputStream(csvFile)) {

			byte[] signatureBytes = Base64.getDecoder().decode(signatureB64);
			AsymmetricService asymmetricService = new AsymmetricService();
			return asymmetricService.verifySignature(signatureBytes, publicKey, csvFileIn);
		}
	}

	private void removeSignatureFromFile(final String signatureB64, final File csvFile) throws IOException {
		try (RandomAccessFile randomAccessFile = new RandomAccessFile(csvFile, "rw")) {

			long length = randomAccessFile.length();
			int sizeLastLine = signatureB64.getBytes(StandardCharsets.UTF_8).length + 1;
			randomAccessFile.setLength(length - sizeLastLine);
		}
	}

	private String getSignatureFromFile(final File csvFile) throws IOException {
		final String signatureB64;
		try (ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(csvFile, 4096, StandardCharsets.UTF_8)) {
			signatureB64 = reversedLinesFileReader.readLine();
		}
		return signatureB64;
	}

	private void validateFilePath(final Path csvFilePath) throws IOException {
		if (csvFilePath == null) {
			throw new IOException("Error to validate CSV file path. The given file path cannot be null.");
		} else if (!csvFilePath.toFile().exists()) {
			throw new IOException("Error to validate CSV file path. The given file path " + csvFilePath.toString() + ", should exist.");
		}
	}
}
