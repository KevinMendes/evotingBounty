/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cms;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSAttributeTableGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.SecureRandomFactory;
import ch.post.it.evoting.votingserver.commons.signature.SignatureFactory;

/**
 * Tests of {@link SignerInfoGeneratorBuilder}.
 */
public class SignerInfoGeneratorBuilderTest {
	private static final char[] PASSWORD = "6JUZUTMZVEWGZVKM".toCharArray();

	private static final String ALIAS = "signing";

	private static final AlgorithmIdentifier IDENTIFIER = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
	private static KeyStore store = null;
	private ContentSignerFactory signerFactory;
	private PrivateKeyEntry entry;
	private CMSAttributeTableGenerator signedAttributeGenerator;
	private CMSAttributeTableGenerator unsignedAttributeGenerator;

	@BeforeClass
	public static void init() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, GeneralCryptoLibException {
		store = CertificateUtil.createTestP12("testForSignerInfoGeneratorBuilder.p12", PASSWORD, ALIAS);
	}

	@Before
	public void setUp() throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, UnrecoverableEntryException {
		SignatureFactory signatureFactory = mock(SignatureFactory.class);
		when(signatureFactory.newSignature()).thenReturn(Signature.getInstance("SHA256withRSA"));
		SecureRandom secureRandom = new SecureRandomFactory(DigitalSignerPolicyImpl.newInstance()::getSecureRandomAlgorithmAndProvider)
				.createSecureRandom();
		signerFactory = new ContentSignerFactoryImpl(signatureFactory, IDENTIFIER, secureRandom);
		entry = (PrivateKeyEntry) store.getEntry(ALIAS, new PasswordProtection(PASSWORD));
		signedAttributeGenerator = mock(CMSAttributeTableGenerator.class);
		unsignedAttributeGenerator = mock(CMSAttributeTableGenerator.class);
	}

	@Test
	public void testBuild() throws IOException, GeneralSecurityException {
		SignerInfoGenerator generator = new SignerInfoGeneratorBuilder(signerFactory).setPrivateKey(entry.getPrivateKey())
				.setCertificate((X509Certificate) entry.getCertificate()).setDirectSignature(false)
				.setSignedAttributeGenerator(signedAttributeGenerator).setUnsignedAttributeGenerator(unsignedAttributeGenerator).build();
		assertEquals(signedAttributeGenerator, generator.getSignedAttributeTableGenerator());
		assertEquals(unsignedAttributeGenerator, generator.getUnsignedAttributeTableGenerator());
		assertArrayEquals(entry.getCertificate().getEncoded(), generator.getAssociatedCertificate().getEncoded());
	}

	@Test
	public void testBuildDirectSignature() throws IOException, GeneralSecurityException {
		SignerInfoGenerator generator = new SignerInfoGeneratorBuilder(signerFactory).setPrivateKey(entry.getPrivateKey())
				.setCertificate((X509Certificate) entry.getCertificate()).setDirectSignature(true)
				.setSignedAttributeGenerator(signedAttributeGenerator).setUnsignedAttributeGenerator(unsignedAttributeGenerator).build();
		assertNull(generator.getSignedAttributeTableGenerator());
		assertNull(generator.getUnsignedAttributeTableGenerator());
		assertArrayEquals(entry.getCertificate().getEncoded(), generator.getAssociatedCertificate().getEncoded());
	}
}
