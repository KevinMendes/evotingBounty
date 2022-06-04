/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cms;

import static java.nio.file.Files.newInputStream;
import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedDataStreamGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;

import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.ConfigDigitalSignerAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.DigitalSignerPolicy;
import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.PaddingInfo;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.SecureRandomFactory;
import ch.post.it.evoting.votingserver.commons.signature.SignatureFactory;
import ch.post.it.evoting.votingserver.commons.signature.SignatureFactoryImpl;

/**
 * Implementation of {@link CMSServiceImpl}.
 */
public final class CMSServiceImpl implements CMSService {
	private final ContentSignerFactory factory;

	private CMSServiceImpl(final ContentSignerFactory factory) {
		this.factory = factory;
	}

	/**
	 * Creates a new instance which uses the algorithm and provider defined in the current {@code cryptolibPolicy.properties}.
	 *
	 * @return the instance.
	 */
	public static CMSServiceImpl newInstance() {
		return newInstance(createDigitalSignerPolicy());
	}

	/**
	 * Creates a new instance which uses the specified algorithm and provider.
	 *
	 * @param spec the specification
	 * @return the instance.
	 */
	public static CMSServiceImpl newInstance(final ConfigDigitalSignerAlgorithmAndSpec spec, ConfigSecureRandomAlgorithmAndProvider prngSpec) {
		requireNonNull(spec, "CMS signer algorithm specification is null.");
		requireNonNull(spec, "Asymmetric signer SecureRandom specification is null.");
		return new CMSServiceImpl(newContentSignerFactory(spec, prngSpec));
	}

	/**
	 * Creates a new instance which uses the algorithm and provider defined by a given policy.
	 *
	 * @param policy the policy
	 * @return the instance.
	 */
	public static CMSServiceImpl newInstance(final DigitalSignerPolicy policy) {
		requireNonNull(policy, "Policy is null.");
		return newInstance(policy.getDigitalSignerAlgorithmAndSpec(), policy.getSecureRandomAlgorithmAndProvider());
	}

	private static AlgorithmIdentifier createAlgorithmIdentifier(final ConfigDigitalSignerAlgorithmAndSpec spec) {
		AlgorithmIdentifier identifier;
		switch (spec) {
		case SHA256_WITH_RSA_SHA256_BC:
		case SHA256_WITH_RSA_SHA256_DEFAULT:
			identifier = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
			break;
		case SHA256_WITH_RSA_AND_PSS_SHA256_MGF1_SHA256_32_1_BC:
		case SHA256_WITH_RSA_AND_PSS_SHA256_MGF1_SHA256_32_1_DEFAULT:
			PaddingInfo paddingInfo = spec.getPaddingInfo();
			AlgorithmIdentifier hashIdentifier = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, DERNull.INSTANCE);
			ASN1Encodable parameters = new RSASSAPSSparams(hashIdentifier, new AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, hashIdentifier),
					new ASN1Integer(paddingInfo.getPaddingSaltBitLength()), new ASN1Integer(paddingInfo.getPaddingTrailerField()));
			identifier = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_RSASSA_PSS, parameters);
			break;
		default:
			throw new IllegalStateException(format("Unsupported signature specification ''{0}''.", spec));
		}
		return identifier;
	}

	private static ContentSignerFactory newContentSignerFactory(final ConfigDigitalSignerAlgorithmAndSpec spec,
			final ConfigSecureRandomAlgorithmAndProvider prngSpec) {
		SignatureFactory factory = createSignatureFactory(spec);
		AlgorithmIdentifier identifier = createAlgorithmIdentifier(spec);
		SecureRandom prng = new SecureRandomFactory(() -> prngSpec).createSecureRandom();
		return new ContentSignerFactoryImpl(factory, identifier, prng);
	}

	private static DigitalSignerPolicy createDigitalSignerPolicy() {
		return DigitalSignerPolicyImpl.newInstance();
	}

	private static SignatureFactory createSignatureFactory(final ConfigDigitalSignerAlgorithmAndSpec spec) {
		return SignatureFactoryImpl.newInstance(spec);
	}

	@Override
	public CMSSignedDataStreamGenerator newCMSSignedDataStreamGenerator(final PrivateKey key, final X509Certificate certificate)
			throws GeneralSecurityException {
		return newCMSSignedDataStreamGenerator(key, certificate, emptySet());
	}

	@Override
	public CMSSignedDataStreamGenerator newCMSSignedDataStreamGenerator(final PrivateKey key, final X509Certificate signerCertificate,
			final Collection<X509Certificate> otherCertificates) throws GeneralSecurityException {
		CMSSignedDataStreamGenerator generator = new CMSSignedDataStreamGenerator();
		generator.addSignerInfoGenerator(newSignedInfoGenerator(key, signerCertificate));
		try {
			generator.addCertificate(new JcaX509CertificateHolder(signerCertificate));
			generator.addCertificates(new JcaCertStore(otherCertificates));
		} catch (CertificateEncodingException | CMSException e) {
			throw new IllegalArgumentException("Invalid certificates.", e);
		}
		return generator;
	}

	@Override
	public CMSSignedDataStreamGenerator newCMSSignedDataStreamGenerator(final PrivateKey key, final X509Certificate signerCertificate,
			final X509Certificate... otherCertificates) throws GeneralSecurityException {
		return newCMSSignedDataStreamGenerator(key, signerCertificate, asList(otherCertificates));
	}

	@Override
	public CMSSignedDataStreamGenerator newCMSSignedDataStreamGenerator(final PrivateKeyEntry entry) throws GeneralSecurityException {
		CMSSignedDataStreamGenerator generator = new CMSSignedDataStreamGenerator();
		generator.addSignerInfoGenerator(newSignedInfoGenerator(entry));
		try {
			generator.addCertificates(new JcaCertStore(asList(entry.getCertificateChain())));
		} catch (CertificateEncodingException | CMSException e) {
			throw new IllegalArgumentException("Invalid certificates.", e);
		}
		return generator;
	}

	@Override
	public SignerInfoGenerator newSignedInfoGenerator(final PrivateKey key, final X509Certificate certificate) throws GeneralSecurityException {
		SignerInfoGeneratorBuilder builder = newSignerInfoGeneratorBuilder();
		builder.setPrivateKey(key);
		builder.setCertificate(certificate);
		return builder.build();
	}

	@Override
	public SignerInfoGenerator newSignedInfoGenerator(final PrivateKeyEntry entry) throws GeneralSecurityException {
		return newSignedInfoGenerator(entry.getPrivateKey(), (X509Certificate) entry.getCertificate());
	}

	@Override
	public SignerInfoGeneratorBuilder newSignerInfoGeneratorBuilder() {
		return new SignerInfoGeneratorBuilder(factory);
	}

	@Override
	public byte[] sign(final byte[] data, final PrivateKey key, final X509Certificate certificate) throws GeneralSecurityException {
		return sign(data, newCMSSignedDataStreamGenerator(key, certificate));
	}

	@Override
	public byte[] sign(final byte[] data, final PrivateKey key, final X509Certificate signerCertificate,
			final Collection<X509Certificate> otherCertificates) throws GeneralSecurityException {
		return sign(data, newCMSSignedDataStreamGenerator(key, signerCertificate, otherCertificates));
	}

	@Override
	public byte[] sign(final byte[] data, final PrivateKey key, final X509Certificate signerCertificate, final X509Certificate... otherCertificates)
			throws GeneralSecurityException {
		return sign(data, newCMSSignedDataStreamGenerator(key, signerCertificate, otherCertificates));
	}

	@Override
	public byte[] sign(final byte[] data, final PrivateKeyEntry entry) throws GeneralSecurityException {
		return sign(data, newCMSSignedDataStreamGenerator(entry));
	}

	@Override
	public byte[] sign(final File file, final PrivateKey key, final X509Certificate certificate) throws IOException, GeneralSecurityException {
		return sign(file, newCMSSignedDataStreamGenerator(key, certificate));
	}

	@Override
	public byte[] sign(final File file, final PrivateKey key, final X509Certificate signerCertificate,
			final Collection<X509Certificate> otherCertificates) throws IOException, GeneralSecurityException {
		return sign(file, newCMSSignedDataStreamGenerator(key, signerCertificate, otherCertificates));
	}

	@Override
	public byte[] sign(final File file, final PrivateKey key, final X509Certificate signerCertificate, final X509Certificate... otherCertificates)
			throws IOException, GeneralSecurityException {
		return sign(file, newCMSSignedDataStreamGenerator(key, signerCertificate, otherCertificates));
	}

	@Override
	public byte[] sign(final File file, final PrivateKeyEntry entry) throws IOException, GeneralSecurityException {
		return sign(file, newCMSSignedDataStreamGenerator(entry));
	}

	@Override
	public byte[] sign(final InputStream stream, final PrivateKey key, final X509Certificate certificate)
			throws IOException, GeneralSecurityException {
		return sign(stream, newCMSSignedDataStreamGenerator(key, certificate));
	}

	@Override
	public byte[] sign(final InputStream stream, final PrivateKey key, final X509Certificate signerCertificate,
			final Collection<X509Certificate> otherCertificates) throws IOException, GeneralSecurityException {
		return sign(stream, newCMSSignedDataStreamGenerator(key, signerCertificate, otherCertificates));
	}

	@Override
	public byte[] sign(final InputStream stream, final PrivateKey key, final X509Certificate signerCertificate,
			final X509Certificate... otherCertificates) throws IOException, GeneralSecurityException {
		return sign(stream, newCMSSignedDataStreamGenerator(key, signerCertificate, otherCertificates));
	}

	@Override
	public byte[] sign(final InputStream stream, final PrivateKeyEntry entry) throws IOException, GeneralSecurityException {
		return sign(stream, newCMSSignedDataStreamGenerator(entry));
	}

	@Override
	public byte[] sign(final Path file, final PrivateKey key, final X509Certificate certificate) throws IOException, GeneralSecurityException {
		return sign(file, newCMSSignedDataStreamGenerator(key, certificate));
	}

	@Override
	public byte[] sign(final Path file, final PrivateKey key, final X509Certificate signerCertificate,
			final Collection<X509Certificate> otherCertificates) throws IOException, GeneralSecurityException {
		return sign(file, newCMSSignedDataStreamGenerator(key, signerCertificate, otherCertificates));
	}

	@Override
	public byte[] sign(final Path file, final PrivateKey key, final X509Certificate signerCertificate, final X509Certificate... otherCertificates)
			throws IOException, GeneralSecurityException {
		return sign(file, newCMSSignedDataStreamGenerator(key, signerCertificate, otherCertificates));
	}

	@Override
	public byte[] sign(final Path file, final PrivateKeyEntry entry) throws IOException, GeneralSecurityException {
		return sign(file, newCMSSignedDataStreamGenerator(entry));
	}

	private byte[] sign(final byte[] data, final CMSSignedDataStreamGenerator generator) {
		byte[] signature;
		try (InputStream stream = new ByteArrayInputStream(data)) {
			signature = sign(stream, generator);
		} catch (IOException e) {
			// all I/O operations use byte[], no errors are expected.
			throw new IllegalStateException("Failed to sign data.", e);
		}
		return signature;
	}

	private byte[] sign(final File file, final CMSSignedDataStreamGenerator generator) throws IOException {
		return sign(file.toPath(), generator);
	}

	private byte[] sign(final InputStream in, final CMSSignedDataStreamGenerator generator) throws IOException {
		ByteArrayOutputStream signature = new ByteArrayOutputStream();
		try (OutputStream out = generator.open(signature)) {
			IOUtils.copy(in, out);
		}
		return signature.toByteArray();
	}

	private byte[] sign(final Path file, final CMSSignedDataStreamGenerator generator) throws IOException {
		byte[] signature;
		try (InputStream stream = newInputStream(file)) {
			signature = sign(stream, generator);
		}
		return signature;
	}
}
