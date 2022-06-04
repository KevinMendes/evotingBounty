/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.keypair.factory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.crypto.kems.RSAKeyEncapsulation;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.ConfigEncryptionKeyPairAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.ConfigSigningKeyPairAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.KeyPairPolicy;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

class KeyPairFactoryTest {

	@Test
	void givenRsaCryptoPolicySunRsaSignWhenGenerateSigningKeyPairThenExpectedFieldsFromUnix() {
		assumeTrue(OperatingSystem.UNIX.isCurrent());

		createKeyPairThenAssertValues(getKeyPairPolicySunRSASignProviderUnixSecureRandomAlg());
	}

	@Test
	void givenRsaCryptoPolicyBCWhenGenerateSigningKeyPairThenExpectedFieldsFromUnix() {
		assumeTrue(OperatingSystem.UNIX.isCurrent());

		createKeyPairThenAssertValues(getKeyPairPolicyBCProviderUnixSecureRandomAlg());
	}

	@Test
	void givenRsaCryptoPolicySunRsaSignWhenGenerateSigningKeyPairThenExpectedFieldsFromWindows() {
		assumeTrue(OperatingSystem.WINDOWS.isCurrent());

		createKeyPairThenAssertValues(getKeyPairPolicySunRSASignProviderWindowsSecureRandomAlg());
	}

	@Test
	void givenRsaCryptoPolicyBCWhenGenerateSigningKeyPairThenExpectedFieldsFromWindows() {
		assumeTrue(OperatingSystem.WINDOWS.isCurrent());

		createKeyPairThenAssertValues(getKeyPairPolicyBCProviderWindowsSecureRandomAlg());
	}

	@Test
	void whenCreateKeyPairFactoryUsingSecureRandomAlgFromWrongOperatingSystem() {
		assumeTrue(OperatingSystem.UNIX.isCurrent());

		assertThrows(CryptoLibException.class, () -> createKeyPairThenAssertValues(getKeyPairPolicyBCProviderWindowsSecureRandomAlg()));
	}

	private void createKeyPairThenAssertValues(final KeyPairPolicy keyPairPolicy) {

		final KeyPairGeneratorFactory keyPairFactory = new KeyPairGeneratorFactory(keyPairPolicy);

		final CryptoKeyPairGenerator keyPairGenerator = keyPairFactory.createSigning();

		final KeyPair keyPair = keyPairGenerator.genKeyPair();
		final RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		final RSAKeyParameters bcPublicKey = new RSAKeyParameters(false, publicKey.getModulus(), publicKey.getPublicExponent());
		final RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		final RSAKeyParameters bcPrivateKey = new RSAKeyParameters(true, privateKey.getModulus(), privateKey.getPrivateExponent());

		assertPublicKeyHasExpectedAlgorithmAndFormat(keyPairPolicy.getSigningKeyPairAlgorithmAndSpec().getAlgorithm(), keyPair.getPublic());

		assertPrivateKeyHasExpectedAlgorithmAndFormat(keyPairPolicy.getSigningKeyPairAlgorithmAndSpec().getAlgorithm(), keyPair.getPrivate());

		// Generate RSA key pair.
		final AsymmetricCipherKeyPair keys = new AsymmetricCipherKeyPair(bcPublicKey, bcPrivateKey);
		// Set RSA-KEM parameters
		final RSAKeyEncapsulation kem;
		final KDF2BytesGenerator kdf = new KDF2BytesGenerator(new SHA256Digest());
		final SecureRandom rnd = new SecureRandom();
		final int keyByteSize = publicKey.getModulus().bitLength() / Byte.SIZE;
		final byte[] out = new byte[keyByteSize];
		final KeyParameter key1;
		final KeyParameter key2;
		// Test RSA-KEM
		kem = new RSAKeyEncapsulation(kdf, rnd);
		kem.init(keys.getPublic());
		key1 = (KeyParameter) kem.encrypt(out, 16);

		final SecretKey secretKey1 = new SecretKeySpec(key1.getKey(), "AES");

		final KDF2BytesGenerator kdf2 = new KDF2BytesGenerator(new SHA256Digest());
		final SecureRandom rnd2 = new SecureRandom();
		final RSAKeyEncapsulation kem2 = new RSAKeyEncapsulation(kdf2, rnd2);
		kem2.init(keys.getPrivate());
		key2 = (KeyParameter) kem2.decrypt(out, 16);

		final SecretKey secretKey2 = new SecretKeySpec(key2.getKey(), "AES");

		assertArrayEquals(key1.getKey(), key2.getKey(), "ARRAYS NOT EQUAL");
		assertArrayEquals(secretKey1.getEncoded(), secretKey2.getEncoded(), "ARRAYS NOT EQUAL");
	}

	private void assertPublicKeyHasExpectedAlgorithmAndFormat(final String expectedAlgorithm, final PublicKey publicKey) {

		assertEquals(expectedAlgorithm, publicKey.getAlgorithm());
		assertEquals("X.509", publicKey.getFormat());
	}

	private void assertPrivateKeyHasExpectedAlgorithmAndFormat(final String expectedAlgorithm, final PrivateKey privateKey) {

		assertEquals(expectedAlgorithm, privateKey.getAlgorithm());
		assertEquals("PKCS#8", privateKey.getFormat());
	}

	private KeyPairPolicy getKeyPairPolicyBCProviderUnixSecureRandomAlg() {
		// Requires BC provider.
		return new KeyPairPolicy() {

			@Override
			public ConfigSigningKeyPairAlgorithmAndSpec getSigningKeyPairAlgorithmAndSpec() {
				return ConfigSigningKeyPairAlgorithmAndSpec.RSA_2048_F4_BC;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
			}

			@Override
			public ConfigEncryptionKeyPairAlgorithmAndSpec getEncryptingKeyPairAlgorithmAndSpec() {
				return null;
			}
		};
	}

	private KeyPairPolicy getKeyPairPolicySunRSASignProviderUnixSecureRandomAlg() {
		return new KeyPairPolicy() {

			@Override
			public ConfigSigningKeyPairAlgorithmAndSpec getSigningKeyPairAlgorithmAndSpec() {
				return ConfigSigningKeyPairAlgorithmAndSpec.RSA_2048_F4_SUN_RSA_SIGN;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
			}

			@Override
			public ConfigEncryptionKeyPairAlgorithmAndSpec getEncryptingKeyPairAlgorithmAndSpec() {
				return null;
			}
		};
	}

	private KeyPairPolicy getKeyPairPolicySunRSASignProviderWindowsSecureRandomAlg() {
		return new KeyPairPolicy() {

			@Override
			public ConfigSigningKeyPairAlgorithmAndSpec getSigningKeyPairAlgorithmAndSpec() {
				return ConfigSigningKeyPairAlgorithmAndSpec.RSA_2048_F4_SUN_RSA_SIGN;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
			}

			@Override
			public ConfigEncryptionKeyPairAlgorithmAndSpec getEncryptingKeyPairAlgorithmAndSpec() {
				return null;
			}
		};
	}

	private KeyPairPolicy getKeyPairPolicyBCProviderWindowsSecureRandomAlg() {
		// Requires BC provider.
		return new KeyPairPolicy() {

			@Override
			public ConfigSigningKeyPairAlgorithmAndSpec getSigningKeyPairAlgorithmAndSpec() {
				return ConfigSigningKeyPairAlgorithmAndSpec.RSA_2048_F4_BC;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
			}

			@Override
			public ConfigEncryptionKeyPairAlgorithmAndSpec getEncryptingKeyPairAlgorithmAndSpec() {
				return null;
			}
		};
	}
}
