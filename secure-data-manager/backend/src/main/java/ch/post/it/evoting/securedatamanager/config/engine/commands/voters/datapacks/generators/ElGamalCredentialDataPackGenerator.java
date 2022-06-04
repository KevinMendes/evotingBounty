/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.generators;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomString;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGenerator;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPrivateKey;
import ch.post.it.evoting.cryptolib.extendedkeystore.factory.CryptoExtendedKeyStoreWithPBKDF;
import ch.post.it.evoting.cryptolib.extendedkeystore.service.ExtendedKeyStoreService;
import ch.post.it.evoting.securedatamanager.config.commons.datapacks.generators.CredentialDataPackGenerator;

public class ElGamalCredentialDataPackGenerator extends CredentialDataPackGenerator {

	public ElGamalCredentialDataPackGenerator(final AsymmetricServiceAPI asymmetricService, final CryptoAPIRandomString cryptoRandomString,
			final X509CertificateGenerator certificateGenerator, final KeyStoreService storesService) {
		super(asymmetricService, cryptoRandomString, certificateGenerator, storesService);
	}

	protected CryptoExtendedKeyStoreWithPBKDF createCryptoKeyStoreWithPBKDF() {

		final KeyStoreService service = new ExtendedKeyStoreService();
		return (CryptoExtendedKeyStoreWithPBKDF) service.createKeyStore();
	}

	protected void putKeyInKeystore(final CryptoExtendedKeyStoreWithPBKDF keyStore, final ElGamalPrivateKey privateKey, final char[] keyStorePassword,
			final String alias) throws GeneralCryptoLibException {

		keyStore.setElGamalPrivateKeyEntry(alias, privateKey, keyStorePassword);
	}
}
