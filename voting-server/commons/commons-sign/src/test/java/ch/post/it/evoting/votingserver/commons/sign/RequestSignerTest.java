/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.sign;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;
import ch.post.it.evoting.votingserver.commons.exception.OvCommonsSignException;
import ch.post.it.evoting.votingserver.commons.sign.beans.SignedRequestContent;
import ch.post.it.evoting.votingserver.commons.verify.RequestVerifier;

public class RequestSignerTest {

	private static PublicKey publicKey;

	private static PrivateKey privateKey;

	private static AsymmetricService asymmetricService;

	@BeforeClass
	public static void setupTests() {

		asymmetricService = new AsymmetricService();
		KeyPair keyPair = asymmetricService.getKeyPairForSigning();
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();
	}

	@Test
	public void testGenerateAndValidateValidSignature() throws IOException, GeneralCryptoLibException, OvCommonsSignException {

		RequestSigner requestSigner = new RequestSigner();
		SignedRequestContent signedRequestContent = new SignedRequestContent("localhost", "get", "body", "originator");

		byte[] signatureBytes = requestSigner.sign(signedRequestContent, privateKey);

		Assert.assertNotNull(signatureBytes);

		RequestVerifier requestVerifier = new RequestVerifier();

		boolean isValidSignature = requestVerifier.verifySignature(signedRequestContent, signatureBytes, publicKey);

		Assert.assertTrue(isValidSignature);

	}

	@Test
	public void testGetBytesMethod() {

		SignedRequestContent object1 = new SignedRequestContent("localhost", "get", "body", "originator");
		byte[] bytes1 = object1.getBytes();

		SignedRequestContent object2 = new SignedRequestContent("localhost", "get", "body", "originator");
		byte[] bytes2 = object2.getBytes();

		Assert.assertArrayEquals(bytes1, bytes2);

		SignedRequestContent object3 = new SignedRequestContent("localhost", "post", "body", "originator");
		byte[] bytes3 = object3.getBytes();

		Assert.assertFalse(Arrays.equals(bytes1, bytes3));

	}

	@Test(expected = OvCommonsSignException.class)
	public void testNothingToSign() throws IOException, GeneralCryptoLibException, OvCommonsSignException {

		RequestSigner requestSigner = new RequestSigner();

		requestSigner.sign(null, privateKey);

	}

	@Test(expected = OvCommonsSignException.class)
	public void testInvalidKey() throws IOException, GeneralCryptoLibException, OvCommonsSignException {

		RequestSigner requestSigner = new RequestSigner();
		SignedRequestContent signedRequestContent = new SignedRequestContent("localhost", "get", "body", "originator");

		requestSigner.sign(signedRequestContent, null);

	}

	@Test
	public void testKeys() throws GeneralCryptoLibException {

		SignedRequestContent signedRequestContent = new SignedRequestContent("localhost", "get", "body", "originator");

		byte[] objectBytes = signedRequestContent.getBytes();

		PrimitivesServiceAPI primitivesService = new PrimitivesService();
		byte[] objectHash = primitivesService.getHash(objectBytes);

		byte[] signatureBytes = asymmetricService.sign(privateKey, objectHash);

		boolean isValidSignature = asymmetricService.verifySignature(signatureBytes, publicKey, objectHash);

		Assert.assertTrue(isValidSignature);
	}

	@Test
	public void testInvalidSignatureBodyModified() throws IOException, GeneralCryptoLibException, OvCommonsSignException {

		RequestSigner requestSigner = new RequestSigner();
		SignedRequestContent originalSignedRequestContent = new SignedRequestContent("localhost", "get", "body", "originator");

		byte[] signatureBytes = requestSigner.sign(originalSignedRequestContent, privateKey);

		Assert.assertNotNull(signatureBytes);

		RequestVerifier requestVerifier = new RequestVerifier();

		SignedRequestContent modifiedSignedRequestContent = new SignedRequestContent("localhost", "get", "body modified", "originator");

		boolean isValidSignature = requestVerifier.verifySignature(modifiedSignedRequestContent, signatureBytes, publicKey);

		Assert.assertFalse(isValidSignature);

	}

	@Test
	public void testEmptyBody() throws IOException, GeneralCryptoLibException, OvCommonsSignException {

		RequestSigner requestSigner = new RequestSigner();
		SignedRequestContent signedRequestContent = new SignedRequestContent("localhost", "get", "", "originator");

		byte[] signatureBytes = requestSigner.sign(signedRequestContent, privateKey);

		Assert.assertNotNull(signatureBytes);

		RequestVerifier requestVerifier = new RequestVerifier();

		boolean isValidSignature = requestVerifier.verifySignature(signedRequestContent, signatureBytes, publicKey);

		Assert.assertTrue(isValidSignature);

	}
}
