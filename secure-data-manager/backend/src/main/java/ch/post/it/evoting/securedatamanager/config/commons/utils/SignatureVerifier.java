/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.utils;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.cmssigner.CMSSigner;
import ch.post.it.evoting.cryptolib.commons.serialization.JsonSignatureService;
import ch.post.it.evoting.cryptolib.elgamal.bean.VerifiableElGamalEncryptionParameters;
import ch.post.it.evoting.securedatamanager.config.commons.config.commons.ChainValidator;
import ch.post.it.evoting.securedatamanager.config.commons.domain.common.SignedObject;

@Service
public class SignatureVerifier {

	private static final ConfigObjectMapper mapper = new ConfigObjectMapper();
	private final CertificateFactory cf;

	public SignatureVerifier() throws CertificateException, NoSuchProviderException {
		Security.addProvider(new BouncyCastleProvider());
		cf = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
	}

	/**
	 * Verify a file and its chain. P7
	 *
	 * @param filePath      path to file to verity
	 * @param signaturePath the path where the signatures are stored
	 * @return a certificate chain
	 * @throws IOException
	 * @throws CMSException
	 * @throws GeneralCryptoLibException
	 */
	public Certificate[] verifyPkcs7(final Path filePath, final Path signaturePath, final Path trustedCAPath)
			throws IOException, CMSException, GeneralCryptoLibException, CertificateException {
		final Certificate trusted = getTrustedCA(trustedCAPath);

		final Certificate[][] verifiedChains;
		try (final InputStream is = Files.newInputStream(filePath)) {
			verifiedChains = CMSSigner.verify(Files.readAllBytes(signaturePath), is);
		}

		if (verifiedChains.length != 1 && verifiedChains[0].length < 1) {
			throw new IllegalArgumentException("Unexpected structure of the chain.");
		}

		final Certificate[] chain = new Certificate[verifiedChains[0].length - 1];
		System.arraycopy(verifiedChains[0], 1, chain, 0, verifiedChains[0].length - 1);

		ChainValidator.validateChain(trusted, chain, verifiedChains[0][0], X509CertificateType.SIGN);

		return verifiedChains[0];
	}

	private Certificate getTrustedCA(final Path trustedCAPath) throws IOException, CertificateException {
		final List<Certificate> trustedCA = readPemCertificates(trustedCAPath);
		if (trustedCA.size() != 1) {
			throw new IllegalArgumentException("Trusted CA PEM file doesn't contain exactly 1 certificate.");
		}

		return trustedCA.get(0);
	}

	/**
	 * Save the trusted chain in files for use in JWT verifying.
	 *
	 * @param trustedChain     the certificate chain to be saved
	 * @param trustedChainPath path on which to save the secured trusted chain
	 * @throws IOException
	 */
	public void saveOfflineTrustedChain(final Certificate[] trustedChain, final Path trustedChainPath) throws IOException {

		try (final JcaPEMWriter pemWriter = new JcaPEMWriter(new FileWriter(trustedChainPath.toFile()))) {
			for (final Certificate chainElem : trustedChain) {
				pemWriter.writeObject(chainElem);
				pemWriter.flush();
			}
		}

	}

	/**
	 * Verifies a jwt with a trusted public key
	 *
	 * @param jwtPath
	 * @param trustedKey
	 * @return
	 * @throws IOException
	 */
	public VerifiableElGamalEncryptionParameters verifyJwt(final Path jwtPath, final PublicKey trustedKey) throws IOException {
		final SignedObject jwtObject = mapper.fromJSONFileToJava(jwtPath.toFile(), SignedObject.class);
		return JsonSignatureService.verify(trustedKey, jwtObject.getSignature(), VerifiableElGamalEncryptionParameters.class);
	}

	/**
	 * Reads a pem file containing a certificate chain
	 *
	 * @param filePath the path from which to read the certificates
	 * @return a list of certificates
	 * @throws IOException
	 * @throws CertificateException
	 */
	public List<Certificate> readPemCertificates(final Path filePath) throws IOException, CertificateException {
		final List<Certificate> certificates = new ArrayList<>();
		try (final PemReader reader = new PemReader(new FileReader(filePath.toFile()))) {
			for (PemObject elem = reader.readPemObject(); elem != null; elem = reader.readPemObject()) {
				final InputStream x509InputStream = new ByteArrayInputStream(elem.getContent());
				certificates.add(cf.generateCertificate(x509InputStream));
			}
		}
		return certificates;
	}
}
