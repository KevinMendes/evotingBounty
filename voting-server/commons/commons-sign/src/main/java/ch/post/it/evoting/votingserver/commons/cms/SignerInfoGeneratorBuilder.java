/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cms;

import static java.util.Objects.requireNonNull;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cms.CMSAttributeTableGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

/**
 * Builder for creating {@link SignerInfoGenerator}. It uses the cryptography providers and algorithms specified in {@code cryptolibPolicy.ptoperties}
 * when possible.
 */
public final class SignerInfoGeneratorBuilder {

	private final ContentSignerFactory signerFactory;

	private PrivateKey privateKey;

	private X509Certificate certificate;

	private boolean directSignature;

	private CMSAttributeTableGenerator signedAttributeGenerator;

	private CMSAttributeTableGenerator unsignedAttributeGenerator;

	/**
	 * Constructor.
	 *
	 * @param signerFactory
	 */
	SignerInfoGeneratorBuilder(final ContentSignerFactory signerFactory) {
		this.signerFactory = signerFactory;
	}

	/**
	 * Builds a {@link SignerInfoGenerator} instance.
	 *
	 * @return the instance
	 * @throws InvalidKeyException          the private key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to build the instance
	 * @throws NullPointerException         private key or certificate is not set.
	 * @throws IllegalStateException        invalid configuration of algorithms and providers in {@cryptolibPolicy.properties}.
	 */
	public SignerInfoGenerator build() throws GeneralSecurityException {
		SignerInfoGenerator generator;
		requireNonNull(privateKey, "Private key is null.");
		requireNonNull(certificate, "Certificate is null.");
		try {
			DigestCalculatorProvider digestProvider = new JcaDigestCalculatorProviderBuilder().build();
			org.bouncycastle.cms.SignerInfoGeneratorBuilder builder = new org.bouncycastle.cms.SignerInfoGeneratorBuilder(digestProvider);
			builder.setDirectSignature(directSignature);
			builder.setSignedAttributeGenerator(signedAttributeGenerator);
			builder.setUnsignedAttributeGenerator(unsignedAttributeGenerator);
			ContentSigner signer = signerFactory.newContentSigner(privateKey);
			X509CertificateHolder holder = new JcaX509CertificateHolder(certificate);
			generator = builder.build(signer, holder);
		} catch (OperatorCreationException e) {
			if (e.getCause() instanceof GeneralSecurityException) {
				throw (GeneralSecurityException) e.getCause();
			} else {
				throw new GeneralSecurityException("Failed to create signer info generator.", e);
			}
		}
		return generator;
	}

	/**
	 * Sets the certificate.
	 *
	 * @param certificate the certificate
	 * @return this instance.
	 */
	public SignerInfoGeneratorBuilder setCertificate(final X509Certificate certificate) {
		this.certificate = certificate;
		return this;
	}

	/**
	 * Sets if the signature is direct, see {@link org.bouncycastle.cms.SignerInfoGeneratorBuilder#setDirectSignature(boolean)} for details.
	 *
	 * @param directSignature the signature is direct
	 * @return this instance.
	 */
	public SignerInfoGeneratorBuilder setDirectSignature(final boolean directSignature) {
		this.directSignature = directSignature;
		return this;
	}

	/**
	 * Sets the private key.
	 *
	 * @param privateKey the private key
	 * @return this instance.
	 */
	public SignerInfoGeneratorBuilder setPrivateKey(final PrivateKey privateKey) {
		this.privateKey = privateKey;
		return this;
	}

	/**
	 * Sets signed attribute generator, see {@link org.bouncycastle.cms.SignerInfoGeneratorBuilder#setSignedAttributeGenerator(CMSAttributeTableGenerator)}
	 * for details.
	 *
	 * @param signedAttributeGenerator the generator
	 * @return this instance.
	 */
	public SignerInfoGeneratorBuilder setSignedAttributeGenerator(final CMSAttributeTableGenerator signedAttributeGenerator) {
		this.signedAttributeGenerator = signedAttributeGenerator;
		return this;
	}

	/**
	 * Sets unsigned attribute generator, see {@link org.bouncycastle.cms.SignerInfoGeneratorBuilder#setUnsignedAttributeGenerator(CMSAttributeTableGenerator)}
	 * for details.
	 *
	 * @param unsignedAttributeGenerator the generator
	 * @return this instance.
	 */
	public SignerInfoGeneratorBuilder setUnsignedAttributeGenerator(final CMSAttributeTableGenerator unsignedAttributeGenerator) {
		this.unsignedAttributeGenerator = unsignedAttributeGenerator;
		return this;
	}
}
