/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.signer.factory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.asymmetric.utils.KeyPairConverterAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.DigitalSignerPolicyFromProperties;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;

class DigitalSignerTest {

	private static final int ZERO = 0;
	private static final int ONE = 1;
	private static final int TWO = 2;
	private static final int DATA_BYTE_LENGTH = 100;
	private static final int NUMBER_OF_DATA_PARTS = 10;

	private static byte[] data;
	private static byte[][] dataParts;
	private static KeyPair keyPair;
	private static CryptoDigitalSigner cryptoDigitalSigner_sha256_rsa;
	private static CryptoDigitalSigner cryptoDigitalSigner_sha256_rsa_pss;
	private static PrivateKey privateKeyUsedInJavaScript;
	private static PublicKey publicKeyUsedInJavaScript;
	private static byte[] signedDataAsBytes;
	private static String rsaSignatureGeneratedInJs;
	private static String rsaPssSignatureGeneratedInJs1;
	private static String rsaPssSignatureGeneratedInJs2;

	@BeforeAll
	public static void setUp() throws GeneralCryptoLibException {

		// the service is only used for generating a key pair
		final AsymmetricService asymmetricServiceFromDefaultConstructor_sha256_rsa_pss = new AsymmetricService();

		// create key pairs for signing
		keyPair = asymmetricServiceFromDefaultConstructor_sha256_rsa_pss.getKeyPairForSigning();

		// create a signer using a properties file with SHA256withRSA set as the
		// signing algorithm
		final Properties propertiesRSA = new Properties();
		propertiesRSA.setProperty("asymmetric.signer", "SHA256_WITH_RSA_SHA256_BC");
		cryptoDigitalSigner_sha256_rsa = new DigitalSignerFactory(new DigitalSignerPolicyFromProperties(propertiesRSA)).create();

		// create a signer using a properties file with SHA256withRSA/PSS set as
		// the signing algorithm
		final Properties propertiesRSAPSS = new Properties();
		propertiesRSAPSS.setProperty("asymmetric.signer", "SHA256_WITH_RSA_AND_PSS_SHA256_MGF1_SHA256_32_1_BC");
		cryptoDigitalSigner_sha256_rsa_pss = new DigitalSignerFactory(new DigitalSignerPolicyFromProperties(propertiesRSAPSS)).create();

		final PrimitivesService primitivesService = new PrimitivesService();

		data = primitivesService.genRandomBytes(DATA_BYTE_LENGTH);

		dataParts = new byte[NUMBER_OF_DATA_PARTS][];
		for (int i = 0; i < NUMBER_OF_DATA_PARTS; i++) {
			dataParts[i] = primitivesService.genRandomBytes(DATA_BYTE_LENGTH);
		}

		final KeyPairConverterAPI keyPairConverter = asymmetricServiceFromDefaultConstructor_sha256_rsa_pss.getKeyPairConverter();

		final String privateKeyFromJsPemString =
				"-----BEGIN RSA PRIVATE KEY-----MIIEowIBAAKCAQEAglypZU45bnf2opnRI+Y51VqouBpvDT33xIB/OtbwKzwVpi" + "+JrjBFtfk33tE9t"
						+ "/dSRs79CK94HRhCWcOiLa2qWPrjeZ9SBiEyScrhIvRZVBF41zBgwQNuRvJCsKmAqlZaFNJDZxEP4repmlBn1CfVFmfrXmOKqwP5F7l9ZtucveRzsfmF1yVPFkW8TMuB3YqMiyymyqHlS8ujCsu5I8tpgPbwuxdMOY94fNhSXrYkY8IuX1g1zdq/Z1jluOaR/UqK4UpnbuJaH/F0VgDNiWh6cTD0DFGEk0b70i5wU4Q3L/S6XZQRvSuADoCbhwBKuFL5pW5n865oLVb5S3wuVdWaGwIDAQABAoIBAC/tn34Wf3kE9BGeGc1oFLVDaqqdVVz5/oEpeR2J7q0GnzMFYUpAhzC7WvY52cYsUPyll1Q9Jx0TUTmteo/uvKWQQFfz4nVMeS+2PoXabolBDzuWlsv/1eiRo0FOYHa/3siu8YcQN9X0DpAkpbfTmT1uoZOHZ3EuucMmOFu7vGn38Grw8bSxpR0uvTtnb8ygC+aB51y38RMyhzQQanrM8FMeAfDAy6IB0Yo7b0c50Cxa6Ax4nqn9LXyGakr5WeAMkgTIOA/GId9SZD4e5eRpq+628pOeR4O9datFltgl6r1+A4ii2VrJsDqeatGtODlX6KRKqwFHoGIa2TjgSZLuorECgYEAxeSZDOOgFsI5mB7RkRzZaQ9znJ15sgdyZiAFZAOUah4hSGdAXNAnZTlrdacduXEu3EfkpuPToX7xZSv5FRYwfBwMwCLeytlGLPjQzWejZGbo4+KqgzWb9fECDYVtDPlJ/+yLih9nt67BHweJKxYydl18rVigdVyy22X86NijSykCgYEAqKPUrXZAo+TJvmTw4tgsibJgvXBYBhmsej8mGNQw+Nyp2gV28sgm61ifIeXKS8teq+MFwGA6cHQedbsCqhMHokdhESZmlbWxhSFLihQcewBxwvrBwbaxI23yXRzwMewznZFL032PpcbqrmwFmcSSEZ3nmbvTH6ShqLW+pzDNp6MCgYBQLzdgxJ7qedqSa/JohTMG4e7rh9d2rpPJE7J7ewPZF8pOpx+qO+Gqn2COdJ+Ts2vUcAETKn9nEaPIZc/wnmQY9dioxbhWo0FPGaaphBPtq9Ez/XUv4zoFppk5V1X/isdUPsmvttf00oeIBiqrXbwmv+yz5JRn2Z7TTXjz9Ev+OQKBgQCUuoCMRzl1EgcXIqEL/0kwW6BUEqufHa9u1Ri9Vw6lvL8T6DPipMEmWK9nzuid9gtVns/ovTVtDgv7GuabplLaPQePf4WDzY11c0rSyS/hDyBFrK+LL5uEOqhAlJAGB2HyOj1clWVF+GvrTpuV5LZKUS/79pmZU7G7QCaX/0Ow7wKBgC/kDH7cmWQnWvvJ5izrx/7PogQVPOLELeUIGLu/hjsSdDKiFCxCUZ948+9NuG+DnpXDWzw//r8mPBRRGGsqFws5Aipp7yjQ3kRDCCzGelPCVhHyfmKqA+8ewXPulKS3/wIyHIvaXmsuAtTfurHtpRyzjKmCBK1Y6WQ3trIXvo7s-----END RSA PRIVATE KEY-----";
		final String publicKeyFromJsPemString = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAglypZU45bnf2opnRI+Y51VqouBpvDT33xIB"
				+ "/OtbwKzwVpi+JrjBFtfk33tE9t"
				+ "/dSRs79CK94HRhCWcOiLa2qWPrjeZ9SBiEyScrhIvRZVBF41zBgwQNuRvJCsKmAqlZaFNJDZxEP4repmlBn1CfVFmfrXmOKqwP5F7l9ZtucveRzsfmF1yVPFkW8TMuB3YqMiyymyqHlS8ujCsu5I8tpgPbwuxdMOY94fNhSXrYkY8IuX1g1zdq/Z1jluOaR/UqK4UpnbuJaH/F0VgDNiWh6cTD0DFGEk0b70i5wU4Q3L/S6XZQRvSuADoCbhwBKuFL5pW5n865oLVb5S3wuVdWaGwIDAQAB-----END PUBLIC KEY-----";

		privateKeyUsedInJavaScript = keyPairConverter.getPrivateKeyForSigningFromPem(privateKeyFromJsPemString);
		publicKeyUsedInJavaScript = keyPairConverter.getPublicKeyForSigningFromPem(publicKeyFromJsPemString);

		rsaSignatureGeneratedInJs = "UqEYX273eKEylP+nZDKHtCaPFQ1+mZvJhm0GIe8QOBjnglXBljfItkKKC1gMneIxT77m137"
				+ "+ikQsuECbnrZ3NOenGmx4J5yafDLbY1MeUiI35aYvmPk1RyNguX5SelTRAytGNV6aoZSuyBkbR0bdO7av5JzP35I+GURLMVeEI+veew9e6rG6oI7XZqRLV52LOYfoB"
				+ "/BWiORhmJDNh5Xj82G+D3OdU0DPd4dIR16tIxaYuhoQqR9dxFFN7UzzD6bbpzldcV9olfANHr135GDWxYmtwXxgv+pN0Cf5p5nJp9h+yLUnZurkzpWO69GmLYWqjHuK"
				+ "/vMK503WeBXco1QmsA==";

		rsaPssSignatureGeneratedInJs1 = "ZMnNlXZWDO7nrYtF2WNiazpD2"
				+ "+rTNvWe6WtoBbBQV7E1VwsrJSADEmDaqdTqLPKuGipVhLY8RMVuggTXe966kOO8wBkD6r23Mbz5mRPSjplgXBXDqhsMZzHdOF5f6rHm+fVal9eLO"
				+ "+cl3hR8IlrYDgffLpGdPJjuehyDs2wVedQhxrEDCePF9VFGqhHJ7UqcA5fPjkczVrePI3k1kM9GnTyy7aXN3mlJ7TUpNbueF0xAmi13KCg4YvS"
				+ "/Sz1re6cg79JxpyzyEIBunJPwqFzImjf4CeqwBVBtlb8MqrysCOsBnbQ4CPozUvPqYx4IclkMB3x/V/+Ir7byimX/8ZbBuQ==";

		rsaPssSignatureGeneratedInJs2 = "Jcf4sBBK0QDDEhrnLdf4uiYMi0homLhMON0T1xHBL3/408wC3SWcFUihHfjUIaCRKOxjjQcEfHXCQpgbbG"
				+ "/rsuhVMtrasExr3pV3mo5DHEsQNAyVQqscMEMGhx7xLZdkuEO5UpwtKEO3Zt4MFPOaiiQwNhKAZxmYhcjenZ19eyKXo60Tqa4JR"
				+ "/t7A6cHYTIdDBr1ixZvjnAeKxGXkFyzKUPFkP5teflVX4YwQFFEM5JryN6Yec2QupssouuXegy0Dd55wGXBKVkHiQ1eseW6WsxsFqg1jVaCdxClvMsrtZkna5r"
				+ "+Q0p9xJNa9GmgafJX0rQj9fDujcsVJLRelHohqQ==";

		final String signedData = "Ox2fUJq1gAbX";

		signedDataAsBytes = signedData.getBytes(StandardCharsets.UTF_8);
	}

	@Test
	void whenEncryptAndDecryptSingleDataByteArrayUsingRsaThenTrue() {

		final byte[] signatureBytes = cryptoDigitalSigner_sha256_rsa.sign(keyPair.getPrivate(), data);

		final boolean signatureVerified = cryptoDigitalSigner_sha256_rsa.verifySignature(signatureBytes, keyPair.getPublic(), data);

		assertTrue(signatureVerified);
	}

	@Test
	void whenEncryptAndDecryptArrayOfDataByteArraysUsingRsaThenTrue() {

		final byte[] signatureBytes = cryptoDigitalSigner_sha256_rsa.sign(keyPair.getPrivate(), dataParts);

		final boolean signatureVerified = cryptoDigitalSigner_sha256_rsa.verifySignature(signatureBytes, keyPair.getPublic(), dataParts);

		assertTrue(signatureVerified);
	}

	@Test
	void whenSingUsingStream() {

		final byte[] data = new byte[10000];
		final byte b = 101;
		Arrays.fill(data, b);
		final byte[] signatureBytes = cryptoDigitalSigner_sha256_rsa.sign(keyPair.getPrivate(), new ByteArrayInputStream(data));

		final boolean signatureVerified = cryptoDigitalSigner_sha256_rsa
				.verifySignature(signatureBytes, keyPair.getPublic(), new ByteArrayInputStream(data));

		assertTrue(signatureVerified);
	}

	@Test
	void whenEncryptAndDecryptCommaSeperatedListOfDataByteArraysUsingRsaThenTrue() {

		final byte[] signatureBytes = cryptoDigitalSigner_sha256_rsa.sign(keyPair.getPrivate(), dataParts[ZERO], dataParts[ONE], dataParts[TWO]);

		final boolean signatureVerified = cryptoDigitalSigner_sha256_rsa
				.verifySignature(signatureBytes, keyPair.getPublic(), dataParts[ZERO], dataParts[ONE], dataParts[TWO]);

		assertTrue(signatureVerified);
	}

	@Test
	void whenEncryptAndDecryptSingleDataByteArrayUsingRsaPssThenTrue() {

		final byte[] signatureBytes = cryptoDigitalSigner_sha256_rsa_pss.sign(keyPair.getPrivate(), data);

		final boolean signatureVerified = cryptoDigitalSigner_sha256_rsa_pss.verifySignature(signatureBytes, keyPair.getPublic(), data);

		assertTrue(signatureVerified);
	}

	@Test
	void whenEncryptAndDecryptArrayOfDataByteArraysUsingRsaPssThenTrue() {

		final byte[] signatureBytes = cryptoDigitalSigner_sha256_rsa_pss.sign(keyPair.getPrivate(), dataParts);

		final boolean signatureVerified = cryptoDigitalSigner_sha256_rsa_pss.verifySignature(signatureBytes, keyPair.getPublic(), dataParts);

		assertTrue(signatureVerified);
	}

	@Test
	void whenEncryptAndDecryptCommaSeperatedListOfDataByteArraysUsingRsaPssThenTrue() {

		final byte[] signatureBytes = cryptoDigitalSigner_sha256_rsa_pss.sign(keyPair.getPrivate(), dataParts[ZERO], dataParts[ONE], dataParts[TWO]);

		final boolean signatureVerified = cryptoDigitalSigner_sha256_rsa_pss
				.verifySignature(signatureBytes, keyPair.getPublic(), dataParts[ZERO], dataParts[ONE], dataParts[TWO]);

		assertTrue(signatureVerified);
	}

	@Test
	void whenRegenerateSignatureAsFromJsUsingRsaThenEquals() {

		final byte[] signature = cryptoDigitalSigner_sha256_rsa.sign(privateKeyUsedInJavaScript, signedDataAsBytes);

		final String signatureBase64 = Base64.getEncoder().encodeToString(signature);

		final String errorMsg = "The generated signature does not match the expected one";
		assertEquals(rsaSignatureGeneratedInJs, signatureBase64, errorMsg);
	}

	@Test
	void whenVerifyRealRsaSignatureFromJsThenOk() {

		final byte[] signatureBytes = Base64.getDecoder().decode(rsaSignatureGeneratedInJs);

		assertTrue(cryptoDigitalSigner_sha256_rsa.verifySignature(signatureBytes, publicKeyUsedInJavaScript, signedDataAsBytes));
	}

	@Test
	void whenVerifyBadRsaSignatureFromJsThenFalse() {

		final byte[] signatureBytes = Base64.getDecoder().decode("XXXXXX");

		assertFalse(cryptoDigitalSigner_sha256_rsa.verifySignature(signatureBytes, publicKeyUsedInJavaScript, signedDataAsBytes));
	}

	@Test
	void whenVerifyRealRsaPssSignature1FromJsThenOk() {

		final byte[] signatureBytes = Base64.getDecoder().decode(rsaPssSignatureGeneratedInJs1);

		assertTrue(cryptoDigitalSigner_sha256_rsa_pss.verifySignature(signatureBytes, publicKeyUsedInJavaScript, signedDataAsBytes));
	}

	@Test
	void whenVerifyRealRsaPssSignature2FromJsThenOk() {

		final byte[] signatureBytes = Base64.getDecoder().decode(rsaPssSignatureGeneratedInJs2);

		assertTrue(cryptoDigitalSigner_sha256_rsa_pss.verifySignature(signatureBytes, publicKeyUsedInJavaScript, signedDataAsBytes));
	}

	@Test
	void whenVerifyBadRsaPssSignatureFromJsThenFalse() {

		final byte[] signatureBytes = Base64.getDecoder().decode("XXXXXX");

		assertFalse(cryptoDigitalSigner_sha256_rsa_pss.verifySignature(signatureBytes, publicKeyUsedInJavaScript, signedDataAsBytes));
	}

	@Test
	void whenVerifyRsaSignatureFromJsAsRsaPssThenFalse() {

		final byte[] signatureBytes = Base64.getDecoder().decode(rsaSignatureGeneratedInJs);

		assertFalse(cryptoDigitalSigner_sha256_rsa_pss.verifySignature(signatureBytes, publicKeyUsedInJavaScript, signedDataAsBytes));
	}

	@Test
	void whenVerifyRsaPssSignatureFromJsAsRsaThenFalse() {

		final byte[] signatureBytes = Base64.getDecoder().decode(rsaPssSignatureGeneratedInJs1);

		assertFalse(cryptoDigitalSigner_sha256_rsa.verifySignature(signatureBytes, publicKeyUsedInJavaScript, signedDataAsBytes));
	}

	@Test
	void testEncryptAndDecryptCommaSeparatedListOfDataByteArrays() {

		final byte[] signatureBytes = cryptoDigitalSigner_sha256_rsa.sign(keyPair.getPrivate(), dataParts[ZERO], dataParts[ONE], dataParts[TWO]);

		final boolean signatureVerified = cryptoDigitalSigner_sha256_rsa
				.verifySignature(signatureBytes, keyPair.getPublic(), dataParts[ZERO], dataParts[ONE], dataParts[TWO]);

		assertTrue(signatureVerified);
	}

	@Test
	void signNullKeyTest() {

		final byte[] message = "data".getBytes(StandardCharsets.UTF_8);

		assertThrows(CryptoLibException.class, () -> cryptoDigitalSigner_sha256_rsa.sign(null, message));
	}

	@Test
	void signEmptyKeyTest() throws CryptoLibException {

		final byte[] message = "data".getBytes(StandardCharsets.UTF_8);

		final PrivateKey key = new PrivateKey() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getFormat() {
				return null;
			}

			@Override
			public byte[] getEncoded() {
				return null;
			}

			@Override
			public String getAlgorithm() {
				return null;
			}
		};

		assertThrows(CryptoLibException.class, () -> cryptoDigitalSigner_sha256_rsa.sign(key, message));
	}

	@Test
	void testSignNoData() {
		assertDoesNotThrow(() -> cryptoDigitalSigner_sha256_rsa.sign(keyPair.getPrivate()));
	}

	@Test
	void testSignEmptyData() {

		final byte[] message = new byte[0];

		assertDoesNotThrow(() -> cryptoDigitalSigner_sha256_rsa.sign(keyPair.getPrivate(), message));
	}
}
