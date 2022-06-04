/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.utils;

import java.security.PrivateKey;
import java.security.PublicKey;

import ch.post.it.evoting.cryptolib.api.asymmetric.utils.KeyPairConverterAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;

/**
 * This class implements {@link KeyPairConverterAPI}.
 */
public class KeyPairConverter implements KeyPairConverterAPI {

	@Override
	public PublicKey getPublicKeyForSigningFromPem(final String publicKeyPem) throws GeneralCryptoLibException {

		Validate.notNullOrBlank(publicKeyPem, "Public key PEM string");

		return PemUtils.publicKeyFromPem(publicKeyPem);
	}

	@Override
	public PublicKey getPublicKeyForEncryptingFromPem(final String publicKeyPem) throws GeneralCryptoLibException {

		Validate.notNullOrBlank(publicKeyPem, "Public key PEM string");

		return PemUtils.publicKeyFromPem(publicKeyPem);
	}

	@Override
	public PrivateKey getPrivateKeyForSigningFromPem(final String privateKeyPem) throws GeneralCryptoLibException {

		Validate.notNullOrBlank(privateKeyPem, "Private key PEM string");

		return PemUtils.privateKeyFromPem(privateKeyPem);
	}

	@Override
	public PrivateKey getPrivateKeyForEncryptingFromPem(final String privateKeyPem) throws GeneralCryptoLibException {

		Validate.notNullOrBlank(privateKeyPem, "Private key PEM string");

		return PemUtils.privateKeyFromPem(privateKeyPem);
	}

	@Override
	public String exportPublicKeyForSigningToPem(final PublicKey publicKey) throws GeneralCryptoLibException {

		Validate.notNull(publicKey, "Public key");
		Validate.notNullOrEmpty(publicKey.getEncoded(), "Public key content");

		return PemUtils.publicKeyToPem(publicKey);
	}

	@Override
	public String exportPublicKeyForEncryptingToPem(final PublicKey publicKey) throws GeneralCryptoLibException {

		Validate.notNull(publicKey, "Public key");
		Validate.notNullOrEmpty(publicKey.getEncoded(), "Public key content");

		return PemUtils.publicKeyToPem(publicKey);
	}

	@Override
	public String exportPrivateKeyForSigningToPem(final PrivateKey privateKey) throws GeneralCryptoLibException {

		Validate.notNull(privateKey, "Private key");
		Validate.notNullOrEmpty(privateKey.getEncoded(), "Private key content");

		return PemUtils.privateKeyToPem(privateKey);
	}

	@Override
	public String exportPrivateKeyForEncryptingToPem(final PrivateKey privateKey) throws GeneralCryptoLibException {

		Validate.notNull(privateKey, "Private key");
		Validate.notNullOrEmpty(privateKey.getEncoded(), "Private key content");

		return PemUtils.privateKeyToPem(privateKey);
	}
}
