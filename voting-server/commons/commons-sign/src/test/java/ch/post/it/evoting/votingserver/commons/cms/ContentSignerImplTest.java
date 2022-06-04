/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cms;

import static org.junit.Assert.assertEquals;
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

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Tests of {@link ContentSignerImpl}.
 */
public class ContentSignerImplTest {
	private static final byte[] DATA = { 0, 1, 2 };

	private static final char[] PASSWORD = "6JUZUTMZVEWGZVKM".toCharArray();

	private static final String ALIAS = "signing";

	private static final AlgorithmIdentifier IDENTIFIER = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_RSASSA_PSS);

	private static KeyStore store = null;

	private PrivateKeyEntry entry;

	private ContentSignerImpl signer;

	@BeforeClass
	public static void init() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, GeneralCryptoLibException {
		store = CertificateUtil.createTestP12("testForContentSigner.p12", PASSWORD, ALIAS);
	}

	@Before
	public void setUp()
			throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, InvalidKeyException {
		entry = (PrivateKeyEntry) store.getEntry(ALIAS, new PasswordProtection(PASSWORD));
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(entry.getPrivateKey());
		signer = new ContentSignerImpl(signature, IDENTIFIER);
	}

	@Test
	public void testGetAlgorithmIdentifier() {
		assertEquals(IDENTIFIER, signer.getAlgorithmIdentifier());
	}

	@Test
	public void testGetSignature() throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		try (OutputStream stream = signer.getOutputStream()) {
			stream.write(DATA);
		}

		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initVerify(entry.getCertificate());
		signature.update(DATA);
		assertTrue(signature.verify(signer.getSignature()));
	}
}
