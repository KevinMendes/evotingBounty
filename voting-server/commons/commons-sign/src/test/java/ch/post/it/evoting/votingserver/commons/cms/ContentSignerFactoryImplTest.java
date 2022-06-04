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
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.ContentSigner;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.DigitalSignerPolicy;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.SecureRandomFactory;
import ch.post.it.evoting.votingserver.commons.signature.SignatureFactoryImpl;

/**
 * Tests of {@link ContentSignerFactoryImpl}.
 */
public class ContentSignerFactoryImplTest {
	private static final byte[] DATA = { 0, 1, 2 };

	private static final char[] PASSWORD = "6JUZUTMZVEWGZVKM".toCharArray();

	private static final String ALIAS = "signing";
	private static KeyStore store = null;

	@BeforeClass
	public static void init() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, GeneralCryptoLibException {
		store = CertificateUtil.createTestP12("testForContentSignerFactory.p12", PASSWORD, ALIAS);
	}

	@Test
	public void testNewContentSigner()
			throws KeyStoreException, IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnrecoverableEntryException {
		PrivateKeyEntry entry = (PrivateKeyEntry) store.getEntry(ALIAS, new PasswordProtection(PASSWORD));

		final DigitalSignerPolicy digitalSignerPolicy = DigitalSignerPolicyImpl.newInstance();

		SignatureFactoryImpl signatureFactory = SignatureFactoryImpl.newInstance(digitalSignerPolicy);
		AlgorithmIdentifier identifier = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_RSASSA_PSS);
		SecureRandom secureRandom = new SecureRandomFactory(digitalSignerPolicy::getSecureRandomAlgorithmAndProvider).createSecureRandom();

		ContentSignerFactoryImpl factory = new ContentSignerFactoryImpl(signatureFactory, identifier, secureRandom);

		ContentSigner signer = factory.newContentSigner(entry.getPrivateKey());
		assertEquals(identifier, signer.getAlgorithmIdentifier());
		try (OutputStream stream = signer.getOutputStream()) {
			stream.write(DATA);
		}

		Signature signature = signatureFactory.newSignature();
		signature.initVerify(entry.getCertificate());
		signature.update(DATA);
		assertTrue(signature.verify(signer.getSignature()));
	}

}
