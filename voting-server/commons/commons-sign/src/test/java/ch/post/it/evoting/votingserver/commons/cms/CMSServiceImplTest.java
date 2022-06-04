/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cms;

import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedDataParser;
import org.bouncycastle.cms.CMSSignedDataStreamGenerator;
import org.bouncycastle.cms.CMSTypedStream;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Tests of {@link CMSServiceImpl}.
 */
public class CMSServiceImplTest {
	private static final byte[] DATA = { 0, 1, 2 };

	private static final char[] PASSWORD = "6JUZUTMZVEWGZVKM".toCharArray();

	private static final String ALIAS = "signing";

	private static KeyStore store = null;

	private static Path file;

	private PrivateKeyEntry entry;

	private PrivateKey key;

	private X509Certificate certificate;

	private X509Certificate signerCertificate;

	private CMSServiceImpl service;

	@AfterClass
	public static void afterClass() throws IOException {
		deleteIfExists(file);
	}

	@BeforeClass
	public static void beforeClass()
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, GeneralCryptoLibException {
		file = createTempFile("test", ".dat");
		try {
			write(file, DATA);
		} catch (IOException e) {
			delete(file);
			throw e;
		}
		store = CertificateUtil.createTestP12("testForCmsServiceImpl.p12", PASSWORD, ALIAS);

	}

	@Before
	public void setUp() throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, IOException, CertificateException,
			GeneralCryptoLibException {

		entry = (PrivateKeyEntry) store.getEntry(ALIAS, new PasswordProtection(PASSWORD));
		key = entry.getPrivateKey();
		certificate = (X509Certificate) entry.getCertificate();
		signerCertificate = (X509Certificate) entry.getCertificateChain()[1];
		service = CMSServiceImpl.newInstance();
	}

	@Test
	public void testNewCMSSignedDataStreamGeneratorPrivateKeyEntry()
			throws GeneralSecurityException, IOException, OperatorCreationException, CMSException {
		CMSSignedDataStreamGenerator generator = service.newCMSSignedDataStreamGenerator(entry);

		ByteArrayOutputStream signature = new ByteArrayOutputStream();
		try {
			try (OutputStream stream = generator.open(signature)) {
				stream.write(DATA);
			}
		} finally {
			signature.close();
		}
		assertSignatureIsCorrect(signature.toByteArray());
	}

	@Test
	public void testNewCMSSignedDataStreamGeneratorPrivateKeyX509Certificate()
			throws GeneralSecurityException, IOException, OperatorCreationException, CMSException {
		CMSSignedDataStreamGenerator generator = service.newCMSSignedDataStreamGenerator(key, certificate);

		ByteArrayOutputStream signature = new ByteArrayOutputStream();
		try {
			try (OutputStream stream = generator.open(signature)) {
				stream.write(DATA);
			}
		} finally {
			signature.close();
		}
		assertSignatureIsCorrect(signature.toByteArray());
	}

	@Test
	public void testNewCMSSignedDataStreamGeneratorPrivateKeyX509CertificateCollectionOfX509Certificate()
			throws GeneralSecurityException, IOException, OperatorCreationException, CMSException {
		CMSSignedDataStreamGenerator generator = service.newCMSSignedDataStreamGenerator(key, certificate, singleton(signerCertificate));

		ByteArrayOutputStream signature = new ByteArrayOutputStream();
		try {
			try (OutputStream stream = generator.open(signature)) {
				stream.write(DATA);
			}
		} finally {
			signature.close();
		}
		assertSignatureIsCorrect(signature.toByteArray());
	}

	@Test
	public void testNewCMSSignedDataStreamGeneratorPrivateKeyX509CertificateX509CertificateArray()
			throws GeneralSecurityException, IOException, OperatorCreationException, CMSException {
		CMSSignedDataStreamGenerator generator = service.newCMSSignedDataStreamGenerator(key, certificate, signerCertificate);

		ByteArrayOutputStream signature = new ByteArrayOutputStream();
		try {
			try (OutputStream stream = generator.open(signature)) {
				stream.write(DATA);
			}
		} finally {
			signature.close();
		}
		assertSignatureIsCorrect(signature.toByteArray());
	}

	@Test
	public void testNewSignedInfoGeneratorPrivateKeyEntry() throws GeneralSecurityException, CMSException, IOException, OperatorCreationException {
		SignerInfoGenerator signerGenerator = service.newSignedInfoGenerator(key, certificate);
		CMSSignedDataStreamGenerator generator = new CMSSignedDataStreamGenerator();
		generator.addSignerInfoGenerator(signerGenerator);
		generator.addCertificates(new JcaCertStore(asList(certificate, signerCertificate)));

		ByteArrayOutputStream signature = new ByteArrayOutputStream();
		try {
			try (OutputStream stream = generator.open(signature)) {
				stream.write(DATA);
			}
		} finally {
			signature.close();
		}
		assertSignatureIsCorrect(signature.toByteArray());
	}

	@Test
	public void testNewSignedInfoGeneratorPrivateKeyX509Certificate()
			throws GeneralSecurityException, CMSException, IOException, OperatorCreationException {
		SignerInfoGenerator signerGenerator = service.newSignedInfoGenerator(entry);
		CMSSignedDataStreamGenerator generator = new CMSSignedDataStreamGenerator();
		generator.addSignerInfoGenerator(signerGenerator);
		generator.addCertificates(new JcaCertStore(asList(certificate, signerCertificate)));

		ByteArrayOutputStream signature = new ByteArrayOutputStream();
		try {
			try (OutputStream stream = generator.open(signature)) {
				stream.write(DATA);
			}
		} finally {
			signature.close();
		}
		assertSignatureIsCorrect(signature.toByteArray());
	}

	@Test
	public void testNewSignerInfoGeneratorBuilder() throws GeneralSecurityException, CMSException, IOException, OperatorCreationException {
		SignerInfoGeneratorBuilder builder = service.newSignerInfoGeneratorBuilder();
		builder.setPrivateKey(key);
		builder.setCertificate(certificate);
		SignerInfoGenerator signerGenerator = builder.build();
		CMSSignedDataStreamGenerator generator = new CMSSignedDataStreamGenerator();
		generator.addSignerInfoGenerator(signerGenerator);
		generator.addCertificates(new JcaCertStore(asList(certificate, signerCertificate)));

		ByteArrayOutputStream signature = new ByteArrayOutputStream();
		try {
			try (OutputStream stream = generator.open(signature)) {
				stream.write(DATA);
			}
		} finally {
			signature.close();
		}
		assertSignatureIsCorrect(signature.toByteArray());
	}

	@Test
	public void testSignByteArrayPrivateKeyEntry() throws GeneralSecurityException, OperatorCreationException, CMSException, IOException {
		byte[] signature = service.sign(DATA, entry);
		assertSignatureIsCorrect(signature);
	}

	@Test
	public void testSignByteArrayPrivateKeyX509Certificate() throws GeneralSecurityException, OperatorCreationException, CMSException, IOException {
		byte[] signature = service.sign(DATA, key, certificate);
		assertSignatureIsCorrect(signature);
	}

	@Test
	public void testSignByteArrayPrivateKeyX509CertificateCollectionOfX509Certificate()
			throws IOException, GeneralSecurityException, OperatorCreationException, CMSException {
		byte[] signature = service.sign(DATA, key, certificate, singleton(signerCertificate));
		assertSignatureIsCorrect(signature);
	}

	@Test
	public void testSignByteArrayPrivateKeyX509CertificateX509CertificateArray()
			throws GeneralSecurityException, OperatorCreationException, CMSException, IOException {
		byte[] signature = service.sign(DATA, key, certificate, signerCertificate);
		assertSignatureIsCorrect(signature);
	}

	@Test
	public void testSignFilePrivateKeyEntry() throws IOException, GeneralSecurityException, OperatorCreationException, CMSException {
		byte[] signature = service.sign(file.toFile(), entry);
		assertSignatureIsCorrect(signature);
	}

	@Test
	public void testSignFilePrivateKeyX509Certificate() throws IOException, GeneralSecurityException, OperatorCreationException, CMSException {
		byte[] signature = service.sign(file.toFile(), key, certificate);
		assertSignatureIsCorrect(signature);
	}

	@Test
	public void testSignFilePrivateKeyX509CertificateCollectionOfX509Certificate()
			throws IOException, GeneralSecurityException, OperatorCreationException, CMSException {
		byte[] signature = service.sign(file.toFile(), key, certificate, singleton(signerCertificate));
		assertSignatureIsCorrect(signature);
	}

	@Test
	public void testSignFilePrivateKeyX509CertificateX509CertificateArray()
			throws IOException, GeneralSecurityException, OperatorCreationException, CMSException {
		byte[] signature = service.sign(file.toFile(), key, certificate, signerCertificate);
		assertSignatureIsCorrect(signature);
	}

	@Test
	public void testSignInputStreamPrivateKeyEntry() throws IOException, GeneralSecurityException, OperatorCreationException, CMSException {
		try (InputStream stream = new ByteArrayInputStream(DATA)) {
			byte[] signature = service.sign(stream, entry);
			assertSignatureIsCorrect(signature);
		}
	}

	@Test
	public void testSignInputStreamPrivateKeyX509Certificate() throws IOException, GeneralSecurityException, OperatorCreationException, CMSException {
		try (InputStream stream = new ByteArrayInputStream(DATA)) {
			byte[] signature = service.sign(stream, key, certificate);
			assertSignatureIsCorrect(signature);
		}
	}

	@Test
	public void testSignInputStreamPrivateKeyX509CertificateCollectionOfX509Certificate()
			throws IOException, GeneralSecurityException, OperatorCreationException, CMSException {
		try (InputStream stream = new ByteArrayInputStream(DATA)) {
			byte[] signature = service.sign(stream, key, certificate, singleton(signerCertificate));
			assertSignatureIsCorrect(signature);
		}
	}

	@Test
	public void testSignInputStreamPrivateKeyX509CertificateX509CertificateArray()
			throws IOException, GeneralSecurityException, OperatorCreationException, CMSException {
		try (InputStream stream = new ByteArrayInputStream(DATA)) {
			byte[] signature = service.sign(stream, key, certificate, signerCertificate);
			assertSignatureIsCorrect(signature);
		}
	}

	@Test
	public void testSignPathPrivateKeyEntry() throws IOException, GeneralSecurityException, OperatorCreationException, CMSException {
		byte[] signature = service.sign(file, entry);
		assertSignatureIsCorrect(signature);
	}

	@Test
	public void testSignPathPrivateKeyX509Certificate() throws IOException, GeneralSecurityException, OperatorCreationException, CMSException {
		byte[] signature = service.sign(file, key, certificate);
		assertSignatureIsCorrect(signature);
	}

	@Test
	public void testSignPathPrivateKeyX509CertificateCollectionOfX509Certificate()
			throws IOException, GeneralSecurityException, OperatorCreationException, CMSException {
		byte[] signature = service.sign(file, key, certificate, singleton(signerCertificate));
		assertSignatureIsCorrect(signature);
	}

	@Test
	public void testSignPathPrivateKeyX509CertificateX509CertificateArray()
			throws IOException, GeneralSecurityException, OperatorCreationException, CMSException {
		byte[] signature = service.sign(file, key, certificate, signerCertificate);
		assertSignatureIsCorrect(signature);
	}

	private void assertSignatureIsCorrect(final byte[] signature) throws OperatorCreationException, CMSException, IOException, CertificateException {
		CMSTypedStream stream = new CMSTypedStream(new ByteArrayInputStream(DATA));
		CMSSignedDataParser parser = new CMSSignedDataParser(new JcaDigestCalculatorProviderBuilder().build(), stream, signature);
		parser.getSignedContent().drain();
		Store<?> certificates = parser.getCertificates();
		Collection<?> signers = parser.getSignerInfos().getSigners();
		for (Object s : signers) {
			SignerInformation signer = (SignerInformation) s;
			@SuppressWarnings("unchecked")
			Collection<?> signerCertificates = certificates.getMatches(signer.getSID());
			for (Object c : signerCertificates) {
				X509CertificateHolder holder = (X509CertificateHolder) c;
				SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder().build(holder);
				assertTrue(signer.verify(verifier));
			}
		}
	}
}
