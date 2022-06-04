/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.cryptolib.cmssigner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1ParsingException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedDataParser;
import org.bouncycastle.cms.CMSSignedDataStreamGenerator;
import org.bouncycastle.cms.CMSTypedStream;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.CollectionStore;

import ch.post.it.evoting.cryptolib.CryptolibInitializer;

/**
 * Class to sign files
 */
public class CMSSigner {

	public static final String SIGNATURE_FILE_EXTENSION = ".p7";

	private static final String PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;

	private static final String SIGNER_ALGORITHM = "SHA256WITHRSAANDMGF1";

	static {
		CryptolibInitializer.initialize();
	}

	private CMSSigner() {
	}

	public static void sign(final File data, final File signature, final Certificate signerCert, final PrivateKey key)
			throws IOException, CMSException {
		sign(data, signature, signerCert, null, key);
	}

	public static void sign(final File data, final File signature, final Certificate signerCert, final List<Certificate> certificateChain,
			final PrivateKey key) throws IOException, CMSException {
		try (final FileInputStream fis = new FileInputStream(data); final FileOutputStream fos = new FileOutputStream(signature)) {
			sign(fis, fos, signerCert, certificateChain, key);
		}
	}

	/**
	 * Create a detached p7 signature and output file in PEM
	 */
	public static void sign(final InputStream data, final OutputStream signature, final Certificate signerCert, final PrivateKey key)
			throws IOException, CMSException {
		sign(data, signature, signerCert, null, key);
	}

	/**
	 * Create a detached p7 signature and output file in PEM
	 */
	public static void sign(final InputStream data, final OutputStream signature, final Certificate signerCert,
			final List<Certificate> certificateChain, final PrivateKey key) throws IOException, CMSException {

		final X509Certificate x509 = (X509Certificate) signerCert;
		final CMSSignedDataStreamGenerator generator = new CMSSignedDataStreamGenerator();
		final JcaContentSignerBuilder jcaContentSignerBuilder = new JcaContentSignerBuilder(SIGNER_ALGORITHM);

		try {
			final ContentSigner signer = jcaContentSignerBuilder.setProvider(PROVIDER_NAME).build(key);
			final DigestCalculatorProvider digestCalculatorProvider = new JcaDigestCalculatorProviderBuilder().setProvider(PROVIDER_NAME).build();
			final JcaSignerInfoGeneratorBuilder jcaSignerInfoGeneratorBuilder = new JcaSignerInfoGeneratorBuilder(digestCalculatorProvider);

			generator.addSignerInfoGenerator(jcaSignerInfoGeneratorBuilder.build(signer, x509));

			final ArrayList<X509Certificate> certList = new ArrayList<>();
			certList.add(x509);
			if (certificateChain != null) {
				for (final Certificate certificate : certificateChain) {
					if (certificate != null) {
						certList.add((X509Certificate) certificate);
					}
				}
			}
			generator.addCertificates(new JcaCertStore(certList));

			try (final OutputStream outputStream = generator.open(signature, false)) {
				IOUtils.copy(data, outputStream);
			}
		} catch (final CertificateEncodingException cee) {
			throw new CMSException("Certificate encoding exception", cee);
		} catch (final OperatorCreationException oce) {
			throw new CMSException("Operation creation exception", oce);
		}
	}

	/**
	 * Validates a detached p7 signature.
	 */
	@SuppressWarnings("unchecked")
	public static Certificate[][] verify(final byte[] signature, final InputStream data) throws IOException, CMSException {
		CMSSignedDataParser s = null;
		try {
			s = new CMSSignedDataParser(new JcaDigestCalculatorProviderBuilder().build(), new CMSTypedStream(data), signature);
			s.getSignedContent().drain();
			final SignerInformationStore signers = s.getSignerInfos();
			final List<Certificate[]> verifiedChains = new ArrayList<>();

			for (final SignerInformation signer : signers.getSigners()) {
				final List<Certificate> verifiedCertificates = new ArrayList<>();

				final Collection<?> certCollection = s.getCertificates().getMatches(signer.getSID());

				if (!certCollection.isEmpty()) {
					final X509CertificateHolder certHolder = (X509CertificateHolder) certCollection.iterator().next();

					final X509Certificate certificate = new JcaX509CertificateConverter().setProvider(PROVIDER_NAME).getCertificate(certHolder);

					if (signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(PROVIDER_NAME).build(certificate.getPublicKey()))) {
						verifiedCertificates.add(certificate);
						fillCertificateChain(verifiedCertificates, certificate, s);
					}
					verifiedChains.add(verifiedCertificates.toArray(new Certificate[0]));
				}
			}
			return verifiedChains.toArray(new Certificate[verifiedChains.size()][]);
		} catch (final OperatorCreationException oce) {
			throw new CMSException("Operation creation exception", oce);
		} catch (final ASN1ParsingException asn1pe) {
			throw new CMSException("Cannot parse signature", asn1pe);
		} catch (final CertificateException ce) {
			throw new CMSException("Certificate exception", ce);
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	private static void fillCertificateChain(final List<Certificate> verifiedCertificates, final X509Certificate certificate,
			final CMSSignedDataParser s) throws CMSException, CertificateException {

		final CollectionStore<?> certificates = (CollectionStore<?>) s.getCertificates();
		for (final Object o : certificates) {
			final X509Certificate parent = new JcaX509CertificateConverter().setProvider(PROVIDER_NAME).getCertificate((X509CertificateHolder) o);
			if (parent.getSubjectX500Principal().equals(certificate.getIssuerX500Principal()) && !verifiedCertificates.contains(parent)) {
				verifiedCertificates.add(parent);
				fillCertificateChain(verifiedCertificates, parent, s);
			}
		}
	}

}
