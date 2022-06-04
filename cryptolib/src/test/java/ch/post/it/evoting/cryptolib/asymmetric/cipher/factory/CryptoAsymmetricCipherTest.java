/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.cipher.factory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyPair;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.cipher.configuration.AsymmetricCipherPolicy;
import ch.post.it.evoting.cryptolib.asymmetric.cipher.configuration.ConfigAsymmetricCipherAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;
import ch.post.it.evoting.cryptolib.symmetric.cipher.configuration.ConfigSymmetricCipherAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.ConfigSecretKeyAlgorithmAndSpec;

class CryptoAsymmetricCipherTest {

	private static final int DATA_BYTE_LENGTH = 100;

	private static KeyPair keyPair;
	private static byte[] data;
	private static CryptoAsymmetricCipher cryptoAsymmetricCipher;

	@BeforeAll
	static void setUp() {

		final AsymmetricService asymmetricServiceFromDefaultConstructor = new AsymmetricService();

		keyPair = asymmetricServiceFromDefaultConstructor.getKeyPairForEncryption();

		final PrimitivesService primitivesService = new PrimitivesService();

		data = primitivesService.genRandomBytes(DATA_BYTE_LENGTH);
	}

	private static AsymmetricCipherPolicy getAsymmetricCipherPolicyRsaKemWithKdf1AndSha256AndBc() {
		return new AsymmetricCipherPolicy() {

			@Override
			public ConfigAsymmetricCipherAlgorithmAndSpec getAsymmetricCipherAlgorithmAndSpec() {
				return ConfigAsymmetricCipherAlgorithmAndSpec.RSA_WITH_RSA_KEM_AND_KDF1_AND_SHA256_BC;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return getSecureRandomConfig();
			}

			@Override
			public ConfigSecretKeyAlgorithmAndSpec getSecretKeyAlgorithmAndSpec() {
				return ConfigSecretKeyAlgorithmAndSpec.AES_128_SUNJCE;
			}

			@Override
			public ConfigSymmetricCipherAlgorithmAndSpec getSymmetricCipherAlgorithmAndSpec() {
				return ConfigSymmetricCipherAlgorithmAndSpec.AES_WITH_GCM_AND_NOPADDING_96_128_BC;
			}
		};
	}

	private static AsymmetricCipherPolicy getAsymmetricCipherPolicyRsaKemWithKdf2AndSha256AndBc() {
		return new AsymmetricCipherPolicy() {

			@Override
			public ConfigAsymmetricCipherAlgorithmAndSpec getAsymmetricCipherAlgorithmAndSpec() {
				return ConfigAsymmetricCipherAlgorithmAndSpec.RSA_WITH_RSA_KEM_AND_KDF2_AND_SHA256_BC;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return getSecureRandomConfig();
			}

			@Override
			public ConfigSecretKeyAlgorithmAndSpec getSecretKeyAlgorithmAndSpec() {
				return ConfigSecretKeyAlgorithmAndSpec.AES_128_SUNJCE;
			}

			@Override
			public ConfigSymmetricCipherAlgorithmAndSpec getSymmetricCipherAlgorithmAndSpec() {
				return ConfigSymmetricCipherAlgorithmAndSpec.AES_WITH_GCM_AND_NOPADDING_96_128_BC;
			}
		};
	}

	private static ConfigSecureRandomAlgorithmAndProvider getSecureRandomConfig() {

		switch (OperatingSystem.current()) {
		case WINDOWS:
			return ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
		case UNIX:
			return ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
		default:
			throw new CryptoLibException("OS not supported");
		}
	}

	@Test
	void testEncryptAndDecryptDataWithRsaKemWithKdf1AndSha256AndBc() throws GeneralCryptoLibException {

		cryptoAsymmetricCipher = new AsymmetricCipherFactory(getAsymmetricCipherPolicyRsaKemWithKdf1AndSha256AndBc()).create();

		final byte[] encryptedData = cryptoAsymmetricCipher.encrypt(keyPair.getPublic(), data);

		final byte[] decryptedData = cryptoAsymmetricCipher.decrypt(keyPair.getPrivate(), encryptedData);

		assertArrayEquals(decryptedData, data);
	}

	@Test
	void testEncryptAndDecryptDataWithRsaKemWithKdf2AndSha256AndBc() throws GeneralCryptoLibException {

		cryptoAsymmetricCipher = new AsymmetricCipherFactory(getAsymmetricCipherPolicyRsaKemWithKdf2AndSha256AndBc()).create();

		final byte[] encryptedData = cryptoAsymmetricCipher.encrypt(keyPair.getPublic(), data);

		final byte[] decryptedData = cryptoAsymmetricCipher.decrypt(keyPair.getPrivate(), encryptedData);

		assertArrayEquals(decryptedData, data);
	}

	@Test
	void testDecryptShortWithRsaKem() {
		cryptoAsymmetricCipher = new AsymmetricCipherFactory(getAsymmetricCipherPolicyRsaKemWithKdf2AndSha256AndBc()).create();
		final byte[] data = { 1, 2, 3 };

		assertThrows(GeneralCryptoLibException.class, () -> cryptoAsymmetricCipher.decrypt(keyPair.getPrivate(), data));
	}

}
