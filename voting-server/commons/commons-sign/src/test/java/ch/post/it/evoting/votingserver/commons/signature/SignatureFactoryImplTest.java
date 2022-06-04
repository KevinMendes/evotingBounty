/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.signature;

import static org.junit.Assert.assertTrue;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.Signature;
import java.security.SignatureException;

import org.junit.Before;
import org.junit.Test;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;

/**
 * Tests of {@link SignatureFactoryImpl}.
 */
public class SignatureFactoryImplTest {
	private static final byte[] DATA = { 0, 1, 2 };

	private AsymmetricServiceAPI service;

	private KeyPair pair;

	@Before
	public void setUp() throws GeneralCryptoLibException {
		service = new AsymmetricService();
		pair = service.getKeyPairForSigning();
	}

	@Test
	public void testNewSignatureSign() throws InvalidKeyException, SignatureException, GeneralCryptoLibException {
		SignatureFactory factory = SignatureFactoryImpl.newInstance();
		Signature signature = factory.newSignature();
		signature.initSign(pair.getPrivate());

		signature.update(DATA);
		byte[] result = signature.sign();

		assertTrue(service.verifySignature(result, pair.getPublic(), DATA));
	}

	@Test
	public void testNewSignatureVerify() throws InvalidKeyException, SignatureException, GeneralCryptoLibException {
		SignatureFactory factory = SignatureFactoryImpl.newInstance();
		Signature signature = factory.newSignature();

		byte[] result = service.sign(pair.getPrivate(), DATA);

		signature.initVerify(pair.getPublic());
		signature.update(DATA);
		assertTrue(signature.verify(result));
	}
}
