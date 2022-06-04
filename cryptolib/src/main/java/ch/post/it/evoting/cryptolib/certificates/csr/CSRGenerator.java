/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.csr;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateGeneratorPolicyFromProperties;

public final class CSRGenerator {

	/**
	 * Generate a Certificate Signing Request (CSR).
	 *
	 * @param publickey  the public key.
	 * @param privatekey the private key.
	 * @param subject    encapsulates information that describes the subject.
	 * @return a CSR in the form of a {org.bouncycastle.pkcs.PKCS10CertificationRequest}.
	 * @throws OperatorCreationException if there is any problem when generating the CSR or wiping the private key from memory.
	 */
	public PKCS10CertificationRequest generate(final PublicKey publickey, final PrivateKey privatekey, final X500Principal subject)
			throws OperatorCreationException {
		final String algorithm = new X509CertificateGeneratorPolicyFromProperties().getCertificateAlgorithmAndProvider().getAlgorithm();
		final ContentSigner signGen = new JcaContentSignerBuilder(algorithm).build(privatekey);

		final PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subject, publickey);
		return builder.build(signGen);
	}
}
