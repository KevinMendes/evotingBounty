/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.mac.factory;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.binary.ByteArrays;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.symmetric.mac.configuration.MacPolicy;

/**
 * A generator of MAC's.
 */
public class CryptoMac {

	private static final String INITIALIZATION_FAILURE_ERROR_MESSAGE = "Failed to initialize MAC generator.";

	private final Mac macGenerator;

	/**
	 * Creates an instance of MAC generator with the specified {@code macPolicy} .
	 *
	 * @param macPolicy policy for generating MAC's.
	 */
	CryptoMac(final MacPolicy macPolicy) {

		try {
			if (Provider.DEFAULT == macPolicy.getMacAlgorithmAndProvider().getProvider()) {
				macGenerator = Mac.getInstance(macPolicy.getMacAlgorithmAndProvider().getAlgorithm());
			} else {
				macGenerator = Mac.getInstance(macPolicy.getMacAlgorithmAndProvider().getAlgorithm(),
						macPolicy.getMacAlgorithmAndProvider().getProvider().getProviderName());
			}
		} catch (final GeneralSecurityException e) {
			throw new CryptoLibException(
					"Failed to create MAC generator in this environment. Attempted to use the provider: " + macPolicy.getMacAlgorithmAndProvider()
							.getProvider().getProviderName() + ", and the algorithm: " + macPolicy.getMacAlgorithmAndProvider().getAlgorithm()
							+ ". Error message was " + e.getMessage(), e);
		}
	}

	/**
	 * Generates a MAC for the given data, using the given SecretKey.
	 *
	 * @param key  the {@link javax.crypto.SecretKey} to use.
	 * @param data the input data for the MAC.
	 * @return one or more byte[] representing the generated MAC.
	 * @throws CryptoLibException if given key is null or is empty.
	 */
	public byte[] generate(final SecretKey key, final byte[]... data) {

		initMac(key);

		for (final byte[] bs : data) {
			macGenerator.update(bs);
		}
		return macGenerator.doFinal();
	}

	/**
	 * Generates a MAC for the data readable from {@code inStream}, using {@code key}.
	 *
	 * @param key the {@link javax.crypto.SecretKey} to use.
	 * @param in  the {@link InputStream} from which to read data.
	 * @return byte[] representing the generated MAC.
	 * @throws GeneralCryptoLibException if the given secret key is invalid, the data cannot be read from the input stream or the MAC generation
	 *                                   process fails.
	 */
	public byte[] generate(final SecretKey key, final InputStream in) throws GeneralCryptoLibException {

		initMac(key);

		final byte[] buf = new byte[4096];
		int len;
		try {
			while ((len = in.read(buf)) >= 0) {
				macGenerator.update(buf, 0, len);
			}
		} catch (final IllegalStateException | IOException e) {
			throw new GeneralCryptoLibException("Exception while updating MAC from input stream", e);
		}

		return macGenerator.doFinal();
	}

	/**
	 * Verifies that a given MAC is indeed the MAC for the given data, using the given {@link javax.crypto.SecretKey}.
	 *
	 * @param key  the {@link javax.crypto.SecretKey} to use.
	 * @param mac  the MAC to be verified.
	 * @param data one or more input data for the MAC
	 * @return true if the MAC is the MAC of the given data and SecretKey, false otherwise.
	 * @throws CryptoLibException if given key is null or contains no data.
	 */
	public boolean verify(final SecretKey key, final byte[] mac, final byte[]... data) {

		return ByteArrays.constantTimeEquals(generate(key, data), mac);
	}

	/**
	 * Verifies that a given MAC is indeed the MAC for the data readable from {@code inStream}, using {@code key}.
	 *
	 * @param key the {@link javax.crypto.SecretKey} to use.
	 * @param mac the MAC to be verified.
	 * @param in  the {@link InputStream} from which data should be read.
	 * @return true if {@code mac} is indeed the MAC of the data readable from {@code in} using {@code key}, false otherwise.
	 * @throws GeneralCryptoLibException if the given secret key or MAC is invalid, the data cannot be read from the input stream or the MAC
	 *                                   verification process fails.
	 */
	public boolean verify(final SecretKey key, final byte[] mac, final InputStream in) throws GeneralCryptoLibException {

		return ByteArrays.constantTimeEquals(generate(key, in), mac);
	}

	private void initMac(final SecretKey key) {

		try {
			macGenerator.init(key);
		} catch (final InvalidKeyException e) {
			throw new CryptoLibException(INITIALIZATION_FAILURE_ERROR_MESSAGE, e);
		}
	}
}
