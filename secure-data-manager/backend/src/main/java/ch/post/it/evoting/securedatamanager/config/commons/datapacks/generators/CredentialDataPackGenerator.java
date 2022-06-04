/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.datapacks.generators;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.time.ZonedDateTime;
import java.util.Properties;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomString;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.bean.CredentialProperties;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGenerator;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.domain.election.helpers.CertificateParametersLoader;
import ch.post.it.evoting.domain.election.helpers.ReplacementsHolder;
import ch.post.it.evoting.securedatamanager.config.commons.datapacks.beans.InputDataPack;

public class CredentialDataPackGenerator {

	protected final AsymmetricServiceAPI asymmetricService;

	protected final X509CertificateGenerator certificateGenerator;

	protected final KeyStoreService storesService;

	protected final CryptoAPIRandomString cryptoRandomString;

	public CredentialDataPackGenerator(final AsymmetricServiceAPI asymmetricService, final CryptoAPIRandomString cryptoRandomString,
			final X509CertificateGenerator certificateGenerator, final KeyStoreService storesService) {
		super();
		this.asymmetricService = asymmetricService;
		this.cryptoRandomString = cryptoRandomString;
		this.certificateGenerator = certificateGenerator;
		this.storesService = storesService;
	}

	protected CryptoAPIExtendedKeyStore setPrivateKeyToKeystore(final CryptoAPIExtendedKeyStore keystore, final String alias,
			final PrivateKey privateKey, final char[] keyStorePassword, final CryptoAPIX509Certificate... certs) throws GeneralCryptoLibException {

		final Certificate[] chain = new Certificate[certs.length];

		for (int i = 0; i < certs.length; i++) {
			chain[i] = certs[i].getCertificate();
		}

		keystore.setPrivateKeyEntry(alias, privateKey, keyStorePassword, chain);

		return keystore;
	}

	protected CryptoAPIX509Certificate createX509Certificate(final InputDataPack electionInputDataPack,
			final CertificateParameters certificateParameters, final KeyPair keyPair) throws GeneralCryptoLibException {

		final PublicKey publicKey = keyPair.getPublic();
		final PrivateKey parentPrivateKey;

		if (electionInputDataPack.getParentKeyPair() == null) {
			parentPrivateKey = keyPair.getPrivate();
		} else {
			parentPrivateKey = electionInputDataPack.getParentKeyPair().getPrivate();
		}

		return certificateGenerator.generate(certificateParameters, publicKey, parentPrivateKey);
	}

	protected CertificateParameters getCertificateParameters(final CredentialProperties credentialProperties, final ZonedDateTime startDate,
			final ZonedDateTime endDate, final ReplacementsHolder replacementsHolder, final Properties certificateProperties) {

		final CertificateParameters.Type type = credentialProperties.getCredentialType();
		final CertificateParametersLoader certificateParametersLoader = new CertificateParametersLoader(replacementsHolder);
		final CertificateParameters certificateParameters = certificateParametersLoader.load(certificateProperties, type);
		certificateParameters.setUserNotBefore(startDate);
		certificateParameters.setUserNotAfter(endDate);
		return certificateParameters;
	}
}
