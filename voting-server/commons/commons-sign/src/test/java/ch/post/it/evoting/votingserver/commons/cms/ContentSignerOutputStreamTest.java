/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cms;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Tests of {@link ContentSignerOutputStream}.
 */
public class ContentSignerOutputStreamTest {
	private static final byte[] DATA = { 0, 1, 2 };

	private static final char[] PASSWORD = "6JUZUTMZVEWGZVKM".toCharArray();

	private static final String ALIAS = "signing";

	private static KeyStore store = null;

	@BeforeClass
	public static void init() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, GeneralCryptoLibException {
		store = CertificateUtil.createTestP12("testForContentSignerOutputStream.p12", PASSWORD, ALIAS);
	}

	@Test
	public void testWriteInt()
			throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, InvalidKeyException, SignatureException {

		PrivateKeyEntry entry = (PrivateKeyEntry) store.getEntry(ALIAS, new PasswordProtection(PASSWORD));
		Signature signer = Signature.getInstance("SHA256withRSA");
		signer.initSign(entry.getPrivateKey());

		try (OutputStream stream = new ContentSignerOutputStream(signer)) {
			stream.write(DATA);
		}

		Signature verifier = Signature.getInstance("SHA256withRSA");
		verifier.initVerify(entry.getCertificate());
		verifier.update(DATA);
		assertTrue(verifier.verify(signer.sign()));
	}
}
