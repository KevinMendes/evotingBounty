/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.signer.factory;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.DigitalSignerPolicy;
import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.PaddingInfo;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.SecureRandomFactory;

/**
 * A digital signer.
 *
 * <p>Instances of this class are immutable.
 */
public class CryptoDigitalSigner {

	private static final String SET_DIGITAL_SIGNER_PADDING_ERROR_MESSAGE = "Could not set digital signer padding.";

	private static final String DIGITALLY_SIGN_ERROR_MESSAGE = "Could not digitally sign data.";

	private static final String VERIFY_DIGITAL_SIGNATURE_ERROR_MESSAGE = "Could not verify digital signature.";

	private final Signature signer;
	private final SecureRandom prng;

	/**
	 * Creates an instance of signer with the provided policy.
	 *
	 * @param digitalSignerPolicy Policy for digital signers.
	 */
	CryptoDigitalSigner(final DigitalSignerPolicy digitalSignerPolicy) {

		// Create signer instance.
		final String algorithmAndPadding = digitalSignerPolicy.getDigitalSignerAlgorithmAndSpec().getAlgorithmAndPadding();
		final String provider = digitalSignerPolicy.getDigitalSignerAlgorithmAndSpec().getProvider().getProviderName();
		try {
			if (Provider.DEFAULT == digitalSignerPolicy.getDigitalSignerAlgorithmAndSpec().getProvider()) {
				signer = Signature.getInstance(algorithmAndPadding);
			} else {
				signer = Signature.getInstance(algorithmAndPadding, provider);
			}

			// Get the appropriate PRNG instance.
			prng = new SecureRandomFactory(digitalSignerPolicy::getSecureRandomAlgorithmAndProvider).createSecureRandom();

		} catch (final GeneralSecurityException e) {
			throw new CryptoLibException("Failed to create digital signer in this environment. Attempted to use the provider: " + provider
					+ ", and the algorithm/padding: " + algorithmAndPadding + ". Error message was " + e.getMessage(), e);
		}

		if (digitalSignerPolicy.getDigitalSignerAlgorithmAndSpec().getPaddingInfo().equals(PaddingInfo.PSS_PADDING_INFO)) {
			setSignerPadding(digitalSignerPolicy);
		}
	}

	/**
	 * Signs the given data, using the given {@link java.security.PrivateKey}.
	 *
	 * @param key  the {@link java.security.PrivateKey} used to sign the data.
	 * @param data the collection of data to be signed.
	 * @return the signature.
	 * @throws CryptoLibException if the given private key or data to sign is invalid, or if the signature generation process fails.
	 */
	public byte[] sign(final PrivateKey key, final byte[]... data) {

		try {
			signer.initSign(key, prng);

			for (final byte[] dataPart : data) {
				signer.update(dataPart);
			}

			return signer.sign();
		} catch (final GeneralSecurityException e) {
			throw new CryptoLibException(DIGITALLY_SIGN_ERROR_MESSAGE, e);
		}
	}

	/**
	 * Signs the data in the given input stream, using the given {@link java.security.PrivateKey}.
	 *
	 * @param key the {@link java.security.PrivateKey} used to sign the data.
	 * @param in  the {@link InputStream} containing the data to be signed.
	 * @return the signature.
	 * @throws CryptoLibException if the given private key is invalid, or if the signature generation process fails.
	 */
	public byte[] sign(final PrivateKey key, final InputStream in) {

		try {
			signer.initSign(key, prng);

			final byte[] buf = new byte[4096];
			int len;
			while ((len = in.read(buf)) >= 0) {
				signer.update(buf, 0, len);
			}

			return signer.sign();
		} catch (final IOException | GeneralSecurityException e) {
			throw new CryptoLibException(DIGITALLY_SIGN_ERROR_MESSAGE, e);
		}
	}

	/**
	 * Verifies the given signature of the given data, using the given {@link java.security.PublicKey}.
	 *
	 * @param signature the signature.
	 * @param key       the {@link java.security.PublicKey} used to verify the signature.
	 * @param data      the collection of data that was signed.
	 * @return true if the signature is valid, false otherwise.
	 * @throws CryptoLibException if the given signature, public key or data that was signed is invalid, or if the signature verification process
	 *                            fails.
	 */
	public boolean verifySignature(final byte[] signature, final PublicKey key, final byte[]... data) {

		try {
			signer.initVerify(key);

			for (final byte[] dataPart : data) {
				signer.update(dataPart);
			}

			return signer.verify(signature);
		} catch (final GeneralSecurityException e) {
			throw new CryptoLibException(VERIFY_DIGITAL_SIGNATURE_ERROR_MESSAGE, e);
		}
	}

	/**
	 * Verifies the given signature of the data from the given {@link InputStream}, using the given {@link java.security.PublicKey}.
	 *
	 * @param signature the signature.
	 * @param key       the {@link java.security.PublicKey} used to verify the signature.
	 * @param in        the {@link InputStream} containing the data that was signed.
	 * @return true if the signature is valid, false otherwise.
	 * @throws CryptoLibException if the given signature or public key is invalid, or if the signature verification process fails.
	 */
	public boolean verifySignature(final byte[] signature, final PublicKey key, final InputStream in) {

		try {
			signer.initVerify(key);

			final byte[] buf = new byte[4096];
			int len;
			while ((len = in.read(buf)) >= 0) {
				signer.update(buf, 0, len);
			}

			return signer.verify(signature);
		} catch (final IOException | GeneralSecurityException e) {
			throw new CryptoLibException(VERIFY_DIGITAL_SIGNATURE_ERROR_MESSAGE, e);
		}
	}

	/**
	 * Sets the padding of the digital signer.
	 *
	 * @param digitalSignerPolicy Policy for digital signers.
	 */
	private void setSignerPadding(final DigitalSignerPolicy digitalSignerPolicy) {

		if (digitalSignerPolicy.getDigitalSignerAlgorithmAndSpec().getPaddingInfo().equals(PaddingInfo.PSS_PADDING_INFO)) {

			final String messageDigestAlgorithm = digitalSignerPolicy.getDigitalSignerAlgorithmAndSpec().getPaddingMessageDigestAlgorithm();

			final PSSParameterSpec pssParameterSpec = new PSSParameterSpec(messageDigestAlgorithm,
					PaddingInfo.PSS_PADDING_INFO.getPaddingMaskingGenerationFunctionAlgorithm(),
					new MGF1ParameterSpec(PaddingInfo.PSS_PADDING_INFO.getPaddingMaskingGenerationFunctionMessageDigestAlgorithm()),
					PaddingInfo.PSS_PADDING_INFO.getPaddingSaltBitLength(), PaddingInfo.PSS_PADDING_INFO.getPaddingTrailerField());

			try {
				signer.setParameter(pssParameterSpec);
			} catch (final InvalidAlgorithmParameterException e) {
				throw new CryptoLibException(SET_DIGITAL_SIGNER_PADDING_ERROR_MESSAGE, e);
			}
		}
	}
}
