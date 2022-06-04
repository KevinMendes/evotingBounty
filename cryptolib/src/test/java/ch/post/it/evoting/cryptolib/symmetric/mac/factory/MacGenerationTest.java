/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.mac.factory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;
import ch.post.it.evoting.cryptolib.symmetric.mac.configuration.MacPolicyFromProperties;
import ch.post.it.evoting.cryptolib.symmetric.service.SymmetricService;

class MacGenerationTest {

	private static final int ZERO = 0;
	private static final int ONE = 1;
	private static final int TWO = 2;
	private static final int DATA_BYTE_LENGTH = 10;
	private static final int NUMBER_OF_DATA_PARTS = 10;
	private static SecretKey key;
	private static byte[] data1;
	private static byte[] data2;
	private static byte[][] dataParts1;
	private static byte[][] dataParts2;
	private static CryptoMac cryptoMac;
	private final int MAC_BYTE_LENGTH = 32;

	@BeforeAll
	static void setUp() throws GeneralCryptoLibException {

		final SymmetricService symmetricServiceFromDefaultConstructor = new SymmetricService();

		key = symmetricServiceFromDefaultConstructor.getSecretKeyForHmac();

		final PrimitivesService primitivesService = new PrimitivesService();

		data1 = primitivesService.genRandomBytes(DATA_BYTE_LENGTH);

		data2 = primitivesService.genRandomBytes(DATA_BYTE_LENGTH + ONE);

		dataParts1 = new byte[NUMBER_OF_DATA_PARTS][];
		for (int i = 0; i < NUMBER_OF_DATA_PARTS; i++) {
			dataParts1[i] = primitivesService.genRandomBytes(DATA_BYTE_LENGTH);
		}

		dataParts2 = new byte[NUMBER_OF_DATA_PARTS][];
		for (int i = 0; i < NUMBER_OF_DATA_PARTS; i++) {
			dataParts2[i] = primitivesService.genRandomBytes(DATA_BYTE_LENGTH + ONE);
		}
		cryptoMac = new MacFactory(new MacPolicyFromProperties()).create();
	}

	@Test
	void whenGenerateMacFromBytesThenHasExpectedLength() {

		final byte[] mac = cryptoMac.generate(key, data1);

		final int macByteLength = mac.length;

		assertEquals(macByteLength, MAC_BYTE_LENGTH);
	}

	@Test
	void whenGenerateMacFromStreamThenHasExpectedLength() throws GeneralCryptoLibException {

		final byte[] mac = cryptoMac.generate(key, constructStreamContainingBytes());

		final int macByteLength = mac.length;

		assertEquals(macByteLength, MAC_BYTE_LENGTH);
	}

	@Test
	void whenGenerateMacFromSameDataTwiceThenSameMac() {

		final byte[] mac1 = cryptoMac.generate(key, data1);

		final byte[] mac2 = cryptoMac.generate(key, data1);

		assertArrayEquals(mac1, mac2);
	}

	@Test
	void whenGenerateMacFromNoData() {
		assertDoesNotThrow(() -> cryptoMac.generate(key));
	}

	@Test
	void whenGenerateMacFromNullData() {
		assertDoesNotThrow(() -> cryptoMac.generate(key, (byte[]) null));
	}

	@Test
	void whenGenerateMacWithNullKeyThenException() {
		assertThrows(CryptoLibException.class, () -> cryptoMac.generate(null, data1));
	}

	@Test
	void whenGenerateMacWithOtherKey() {
		final SecretKey key = new SecretKey() {
			private static final long serialVersionUID = -1360480738312503962L;

			@Override
			public String getFormat() {
				return null;
			}

			@Override
			public byte[] getEncoded() {
				return "0123".getBytes(StandardCharsets.UTF_8);
			}

			@Override
			public String getAlgorithm() {
				return null;
			}
		};

		assertDoesNotThrow(() -> cryptoMac.generate(key, data1));
	}

	@Test
	void whenGenerateMacWithNullKeyByteArray() {
		final SecretKey key = new SecretKey() {
			private static final long serialVersionUID = 4144119085977660815L;

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

		assertThrows(CryptoLibException.class, () -> cryptoMac.generate(key, data1));
	}

	@Test
	void whenGenerateMacsForDifferentDataThenDifferentMacs() {

		final byte[] mac1 = cryptoMac.generate(key, data1);

		final byte[] mac2 = cryptoMac.generate(key, data2);

		assertFalse(Arrays.equals(mac1, mac2));
	}

	@Test
	void whenGenerateMacsForSameDataThenSameMacs() {

		final byte[] mac1 = cryptoMac.generate(key, dataParts1);

		final byte[] mac2 = cryptoMac.generate(key, dataParts1);

		assertArrayEquals(mac1, mac2);
	}

	@Test
	void whenGenerateMacsForDifferentArraysOfDataByteArraysThenDifferentMacs() {

		final byte[] mac1 = cryptoMac.generate(key, dataParts1);

		final byte[] mac2 = cryptoMac.generate(key, dataParts2);

		assertFalse(Arrays.equals(mac1, mac2));
	}

	@Test
	void whenGenerateMacsForSameArraysOfDataByteArraysThenSameMacs() {

		final byte[] mac1 = cryptoMac.generate(key, dataParts1);

		final byte[] mac2 = cryptoMac.generate(key, dataParts1);

		assertArrayEquals(mac1, mac2);
	}

	@Test
	void whenGenerateMacsForDifferentCommaSeparatedListsOfDataByteArraysThenDifferentMacs() {

		final byte[] mac1 = cryptoMac.generate(key, dataParts1[ZERO], dataParts1[ONE], dataParts1[TWO]);

		final byte[] mac2 = cryptoMac.generate(key, dataParts1[ZERO], dataParts1[ONE], dataParts2[TWO]);

		assertFalse(Arrays.equals(mac1, mac2));
	}

	@Test
	void whenGenerateMacsForSameCommaSeparatedListsOfDataByteArraysThenSameMacs() {

		final byte[] mac1 = cryptoMac.generate(key, dataParts1[ZERO], dataParts1[ONE], dataParts1[TWO]);

		final byte[] mac2 = cryptoMac.generate(key, dataParts1[ZERO], dataParts1[ONE], dataParts1[TWO]);

		assertArrayEquals(mac1, mac2);
	}

	@Test
	void whenGenerateAndVerifyMacFromStreamThenTrue() throws GeneralCryptoLibException {

		final byte[] mac = cryptoMac.generate(key, constructStreamContainingBytes());

		assertTrue(cryptoMac.verify(key, mac, constructStreamContainingBytes()));
	}

	@Test
	void whenVerifyBadMacThenFalse() throws GeneralCryptoLibException {

		final byte[] badMac = new byte[MAC_BYTE_LENGTH];

		assertFalse(cryptoMac.verify(key, badMac, constructStreamContainingBytes()));
	}

	@Test
	void whenGenerateMacFromStreamTwiceThenSameMacs() throws GeneralCryptoLibException {

		final byte[] mac1 = cryptoMac.generate(key, constructStreamContainingBytes());

		final byte[] mac2 = cryptoMac.generate(key, constructStreamContainingBytes());

		assertArrayEquals(mac1, mac2);
	}

	@Test
	void whenGenerateMacFromStreamAndFromBytesThenSameMacs() throws GeneralCryptoLibException {

		// generate a MAC from stream
		final byte[] macFromStream = cryptoMac.generate(key, constructStreamContainingBytes());

		// generate a MAC from all the bytes at once
		final byte[] allBytesFromFile = constructBytes();
		final byte[] macFromBytes = cryptoMac.generate(key, allBytesFromFile);

		// confirm that both MACs are equal
		assertArrayEquals(macFromStream, macFromBytes);
	}

	@Test
	void whenGenerateMacFromBytesAndVerifyUsingStreamThenTrue() throws GeneralCryptoLibException {

		// generate a MAC from all the bytes at once
		final byte[] allBytesFromFile = constructBytes();
		final byte[] macFromBytes = cryptoMac.generate(key, allBytesFromFile);

		// confirm that both MACs are equal
		assertTrue(cryptoMac.verify(key, macFromBytes, constructStreamContainingBytes()));
	}

	@Test
	void whenGenerateMacFromStreamAndVerifyUsingBytesThenTrue() throws GeneralCryptoLibException {

		// generate a MAC from stream
		final byte[] macFromStream = cryptoMac.generate(key, constructStreamContainingBytes());

		// read all of the bytes that will be used to verify the mac
		final byte[] allBytesFromFile = constructBytes();

		// confirm that both MACs are equal
		assertTrue(cryptoMac.verify(key, macFromStream, allBytesFromFile));
	}

	private byte[] constructBytes() {
		final byte[] bytes = new byte[1024];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) i;
		}
		return bytes;
	}

	private InputStream constructStreamContainingBytes() {
		return new ByteArrayInputStream(constructBytes());
	}
}
