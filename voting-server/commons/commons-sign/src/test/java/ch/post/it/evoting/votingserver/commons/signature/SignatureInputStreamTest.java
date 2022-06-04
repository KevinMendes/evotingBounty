/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.signature;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
 * Tests of {@link SignatureInputStream}.
 */
public class SignatureInputStreamTest {
	private static final byte[] DATA = { 0, 1, 2 };

	private AsymmetricServiceAPI service;

	private KeyPair pair;

	private Signature signature;

	private SignatureInputStream stream;

	@Before
	public void setUp() throws GeneralCryptoLibException {
		service = new AsymmetricService();
		pair = service.getKeyPairForSigning();
		signature = SignatureFactoryImpl.newInstance().newSignature();
		stream = new SignatureInputStream(new ByteArrayInputStream(DATA), signature);
	}

	@Test
	public void testRead() throws IOException, SignatureException, InvalidKeyException, GeneralCryptoLibException {
		signature.initSign(pair.getPrivate());

		assertEquals(0, stream.read());
		assertEquals(1, stream.read());
		assertEquals(2, stream.read());
		assertEquals(-1, stream.read());

		assertTrue(service.verifySignature(signature.sign(), pair.getPublic(), DATA));
	}

	@Test(expected = IOException.class)
	public void testReadArrayIntIntSignatureException() throws IOException {
		stream.read(new byte[3], 0, 3);
	}

	@Test
	public void testReadByteArrayIntInt() throws InvalidKeyException, IOException, SignatureException, GeneralCryptoLibException {
		signature.initSign(pair.getPrivate());

		byte[] buffer = new byte[3];
		assertEquals(2, stream.read(buffer, 0, 2));
		assertEquals(1, stream.read(buffer, 2, 1));
		assertEquals(-1, stream.read());
		assertArrayEquals(DATA, buffer);

		assertTrue(service.verifySignature(signature.sign(), pair.getPublic(), DATA));
	}

	@Test(expected = IOException.class)
	public void testReadSignatureException() throws IOException {
		stream.read();
	}
}
