/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import static java.lang.System.arraycopy;

import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomString;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.ValidityDates;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;

@Service
public class KeysAndCertificateGenerator {
	private static final int PASSWORD_LENGTH = 26;

	private static final String FAILED_TO_GENERATE_NODE_ENCRYPTION_CERTIFICATE = "Failed to generate node encryption certificate.";

	private final AsymmetricServiceAPI asymmetricService;

	private final CertificatesServiceAPI certificatesService;

	private final PrimitivesServiceAPI primitivesService;

	private final String nodeId;

	public KeysAndCertificateGenerator(final AsymmetricServiceAPI asymmetricService, final CertificatesServiceAPI certificatesService,
			final PrimitivesServiceAPI primitivesService,
			@Value("${key.node.id}")
			final String nodeId) {
		this.asymmetricService = asymmetricService;
		this.certificatesService = certificatesService;
		this.primitivesService = primitivesService;
		this.nodeId = nodeId;
	}

	private static X509DistinguishedName generateSubjectDn(final String commonName, final X509DistinguishedName issuerDn)
			throws GeneralCryptoLibException {
		return new X509DistinguishedName.Builder(commonName, issuerDn.getCountry()).addLocality(issuerDn.getLocality())
				.addOrganization(issuerDn.getOrganization()).addOrganizationalUnit(issuerDn.getOrganizationalUnit()).build();
	}

	private static X509Certificate[] newCertificateChain(final X509Certificate certificate, final X509Certificate[] issuerChain) {
		final X509Certificate[] chain = new X509Certificate[issuerChain.length + 1];
		chain[0] = certificate;
		arraycopy(issuerChain, 0, chain, 1, issuerChain.length);
		return chain;
	}

	public ElectionSigningKeys generateElectionSigningKeys(final String electionEventId, final Date validFrom, final Date validTo,
			final NodeKeys nodeKeys)
			throws KeyManagementException {
		final KeyPair pair = asymmetricService.getKeyPairForSigning();
		final X509Certificate certificate = generateElectionSigningCertificate(electionEventId, validFrom, validTo, pair.getPublic(),
				nodeKeys.caPrivateKey(), nodeKeys.caCertificate());
		final X509Certificate[] certificateChain = newCertificateChain(certificate, nodeKeys.caCertificateChain());
		return new ElectionSigningKeys(pair.getPrivate(), certificateChain);
	}

	public NodeKeys generateNodeKeys(final PrivateKey caPrivateKey, final X509Certificate[] caCertificateChain) throws KeyManagementException {
		final NodeKeys.Builder builder = new NodeKeys.Builder();

		builder.setCAKeys(caPrivateKey, caCertificateChain);

		final KeyPair encryptionPair = asymmetricService.getKeyPairForEncryption();
		final X509Certificate encryptionCertificate = generateNodeEncryptionCertificate(encryptionPair.getPublic(), caPrivateKey,
				caCertificateChain[0]);

		final X509Certificate[] encryptionCertificateChain = newCertificateChain(encryptionCertificate, caCertificateChain);
		builder.setEncryptionKeys(encryptionPair.getPrivate(), encryptionCertificateChain);

		final KeyPair logSignPair = asymmetricService.getKeyPairForSigning();
		final X509Certificate logSignCertificate = generateNodeLogSignCertificate(logSignPair.getPublic(), caPrivateKey, caCertificateChain[0]);
		final X509Certificate[] logSignCertificateChain = newCertificateChain(logSignCertificate, caCertificateChain);
		builder.setLogSigningKeys(logSignPair.getPrivate(), logSignCertificateChain);

		final KeyPair logEncryptionPair = asymmetricService.getKeyPairForEncryption();
		final X509Certificate logEncryptionCertificate = generateNodeLogEncryptionCertificate(logEncryptionPair.getPublic(), caPrivateKey,
				caCertificateChain[0]);
		final X509Certificate[] logEncryptionCertificateChain = newCertificateChain(logEncryptionCertificate, caCertificateChain);
		builder.setLogEncryptionKeys(logEncryptionPair.getPrivate(), logEncryptionCertificateChain);

		return builder.build();
	}

	public PasswordProtection generatePassword() throws KeyManagementException {
		final CryptoAPIRandomString generator = primitivesService.get32CharAlphabetCryptoRandomString();
		final String password;
		try {
			password = generator.nextRandom(PASSWORD_LENGTH);
		} catch (final GeneralCryptoLibException e) {
			throw new KeyManagementException("Failed to generate password.", e);
		}
		return new PasswordProtection(password.toCharArray());
	}

	private X509Certificate generateElectionSigningCertificate(final String electionEventId, final Date validFrom, final Date validTo,
			final PublicKey publicKey,
			final PrivateKey caPrivateKey, final X509Certificate caCertificate) throws KeyManagementException {
		try {
			final CryptoX509Certificate issuerCertificate = new CryptoX509Certificate(caCertificate);
			final X509DistinguishedName issuerDn = issuerCertificate.getSubjectDn();

			final CertificateData data = new CertificateData();
			final X509DistinguishedName subjectDn = generateSubjectDn(electionEventId, issuerDn);
			data.setSubjectDn(subjectDn);
			data.setSubjectPublicKey(publicKey);
			final ValidityDates dates = new ValidityDates(validFrom, validTo);
			data.setValidityDates(dates);
			data.setIssuerDn(issuerDn);

			return certificatesService.createSignX509Certificate(data, caPrivateKey).getCertificate();
		} catch (final GeneralCryptoLibException e) {
			throw new KeyManagementException("Failed to generate election signing certificate.", e);
		}
	}

	private X509Certificate generateNodeEncryptionCertificate(final PublicKey publicKey, final PrivateKey caPrivateKey,
			final X509Certificate caCertificate)
			throws KeyManagementException {
		try {
			final CryptoX509Certificate issuerCertificate = new CryptoX509Certificate(caCertificate);
			final X509DistinguishedName issuerDn = issuerCertificate.getSubjectDn();

			final CertificateData data = new CertificateData();
			final String commonName = nodeId + " Encryption";
			data.setSubjectDn(generateSubjectDn(commonName, issuerDn));
			data.setSubjectPublicKey(publicKey);
			data.setValidityDates(new ValidityDates(issuerCertificate.getNotBefore(), issuerCertificate.getNotAfter()));
			data.setIssuerDn(issuerDn);

			return certificatesService.createEncryptionX509Certificate(data, caPrivateKey).getCertificate();
		} catch (final GeneralCryptoLibException e) {
			throw new KeyManagementException(FAILED_TO_GENERATE_NODE_ENCRYPTION_CERTIFICATE, e);
		}
	}

	private X509Certificate generateNodeLogEncryptionCertificate(final PublicKey publicKey, final PrivateKey caPrivateKey,
			final X509Certificate caCertificate)
			throws KeyManagementException {
		try {
			final CryptoX509Certificate issuerCertificate = new CryptoX509Certificate(caCertificate);
			final X509DistinguishedName issuerDn = issuerCertificate.getSubjectDn();

			final CertificateData data = new CertificateData();
			final String commonName = nodeId + " Log Encryption";
			data.setSubjectDn(generateSubjectDn(commonName, issuerDn));
			data.setSubjectPublicKey(publicKey);
			data.setValidityDates(new ValidityDates(issuerCertificate.getNotBefore(), issuerCertificate.getNotAfter()));
			data.setIssuerDn(issuerDn);

			return certificatesService.createEncryptionX509Certificate(data, caPrivateKey).getCertificate();
		} catch (final GeneralCryptoLibException e) {
			throw new KeyManagementException(FAILED_TO_GENERATE_NODE_ENCRYPTION_CERTIFICATE, e);
		}
	}

	private X509Certificate generateNodeLogSignCertificate(final PublicKey publicKey, final PrivateKey caPrivateKey,
			final X509Certificate caCertificate)
			throws KeyManagementException {
		try {
			final CryptoX509Certificate issuerCertificate = new CryptoX509Certificate(caCertificate);
			final X509DistinguishedName issuerDn = issuerCertificate.getSubjectDn();

			final CertificateData data = new CertificateData();
			final String commonName = nodeId + " Log Sign";
			data.setSubjectDn(generateSubjectDn(commonName, issuerDn));
			data.setSubjectPublicKey(publicKey);
			data.setValidityDates(new ValidityDates(issuerCertificate.getNotBefore(), issuerCertificate.getNotAfter()));
			data.setIssuerDn(issuerDn);

			return certificatesService.createSignX509Certificate(data, caPrivateKey).getCertificate();
		} catch (final GeneralCryptoLibException e) {
			throw new KeyManagementException(FAILED_TO_GENERATE_NODE_ENCRYPTION_CERTIFICATE, e);
		}
	}

}
