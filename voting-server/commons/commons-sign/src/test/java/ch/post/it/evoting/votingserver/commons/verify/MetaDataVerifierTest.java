/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.verify;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.Json;

import org.junit.Before;
import org.junit.Test;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.votingserver.commons.sign.MetadataFileSigner;
import ch.post.it.evoting.votingserver.commons.sign.SignatureMetadata;

public class MetaDataVerifierTest {

	private KeyPair keyPairForSigning;

	private AsymmetricServiceAPI asymmetricService;

	@Before
	public void init() throws GeneralCryptoLibException {

		asymmetricService = new AsymmetricService();
		keyPairForSigning = asymmetricService.getKeyPairForSigning();

	}

	@Test
	public void verifyOK() throws GeneralCryptoLibException, IOException {

		final Path tempFile = Files.createTempFile("result", "metadata.csv");
		Map<String, String> metadataMap = new LinkedHashMap<>();
		metadataMap.put("eeId", "eeid");
		MetadataFileSigner signer = new MetadataFileSigner(asymmetricService);
		SignatureMetadata signature;
		MetadataFileVerifier metadataFileVerifier = new MetadataFileVerifier(asymmetricService);
		try (InputStream encryptedBallotInputStream = ClassLoader.getSystemResourceAsStream("encryptedBallot.csv");
				OutputStream os = Files.newOutputStream(tempFile, StandardOpenOption.CREATE)) {
			signature = signer.createSignature(keyPairForSigning.getPrivate(), encryptedBallotInputStream, metadataMap);
			Json.createWriter(os).writeObject(signature.toJsonObject());
		}

		try (InputStream metadataInputStream = Files.newInputStream(tempFile);
				InputStream encryptedBallotInputStream = ClassLoader.getSystemResourceAsStream("encryptedBallot.csv")) {
			final boolean verify = metadataFileVerifier
					.verifySignature(keyPairForSigning.getPublic(), metadataInputStream, encryptedBallotInputStream);
			assertTrue(verify);
		}

	}

	@Test
	public void verifyKO() throws GeneralCryptoLibException, IOException {

		final Path tempFile = Files.createTempFile("result", "metadata.csv");
		Map<String, String> metadataMap = new LinkedHashMap<>();
		metadataMap.put("eeId", "eeid");
		MetadataFileSigner signer = new MetadataFileSigner(asymmetricService);
		SignatureMetadata signature;
		MetadataFileVerifier metadataFileVerifier = new MetadataFileVerifier(asymmetricService);
		try (InputStream encryptedBallotInputStream = ClassLoader.getSystemResourceAsStream("encryptedBallot.csv");
				OutputStream os = Files.newOutputStream(tempFile, StandardOpenOption.CREATE)) {
			signature = signer.createSignature(keyPairForSigning.getPrivate(), encryptedBallotInputStream, metadataMap);
			Json.createWriter(os).writeObject(signature.toJsonObject());
		}

		try (InputStream metadataInputStream = Files.newInputStream(tempFile);
				InputStream encryptedBallotInputStream = ClassLoader.getSystemResourceAsStream("encryptedBallotWrong.csv")) {
			final boolean verify = metadataFileVerifier
					.verifySignature(keyPairForSigning.getPublic(), metadataInputStream, encryptedBallotInputStream);
			assertFalse(verify);
		}
	}

}
