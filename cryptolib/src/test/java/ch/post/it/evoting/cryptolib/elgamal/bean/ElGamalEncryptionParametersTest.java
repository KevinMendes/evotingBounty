/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.elgamal.bean;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

class ElGamalEncryptionParametersTest {

	@Test
	void testJsonRoundTrip() throws GeneralCryptoLibException {
		final BigInteger _p = new BigInteger("23");
		final BigInteger _q = new BigInteger("11");
		final BigInteger _g = new BigInteger("2");

		final ElGamalEncryptionParameters _elGamalEncryptionParameters = new ElGamalEncryptionParameters(_p, _q, _g);
		final String jsonStr = _elGamalEncryptionParameters.toJson();

		final ElGamalEncryptionParameters reconstructedElGamalEncryptionParameters = ElGamalEncryptionParameters.fromJson(jsonStr);

		final String errorMsg = "The reconstructed ElGamal encryption parameters are not equal to the expected parameters";

		assertEquals(_elGamalEncryptionParameters, reconstructedElGamalEncryptionParameters, errorMsg);
	}

	@Test
	void testVerifiable() throws GeneralCryptoLibException, IOException {
		final String jsonStr = loadJson("encryption_parameters");

		final ElGamalEncryptionParameters sut = ElGamalEncryptionParameters.fromJson(jsonStr);

		assertAll(() -> assertEquals(23, sut.getP().intValue()), () -> assertEquals(11, sut.getQ().intValue()),
				() -> assertEquals(2, sut.getG().intValue()));

	}

	@Test
	void testNoSeedNoCounter() throws GeneralCryptoLibException, IOException {
		final String jsonStr = loadJson("encryption_parameters-no_seed-no_counter");

		final ElGamalEncryptionParameters sut = ElGamalEncryptionParameters.fromJson(jsonStr);

		assertAll(() -> assertEquals(23, sut.getP().intValue()), () -> assertEquals(11, sut.getQ().intValue()),
				() -> assertEquals(2, sut.getG().intValue()));
	}

	@Test
	void testNoSeed() throws GeneralCryptoLibException, IOException {
		final String jsonStr = loadJson("encryption_parameters-no_seed");

		final ElGamalEncryptionParameters sut = ElGamalEncryptionParameters.fromJson(jsonStr);

		assertAll(() -> assertEquals(23, sut.getP().intValue()), () -> assertEquals(11, sut.getQ().intValue()),
				() -> assertEquals(2, sut.getG().intValue()));
	}

	@Test
	void testNoCounter() throws GeneralCryptoLibException, IOException {
		final String jsonStr = loadJson("encryption_parameters-no_counter");

		final ElGamalEncryptionParameters sut = ElGamalEncryptionParameters.fromJson(jsonStr);

		assertAll(() -> assertEquals(23, sut.getP().intValue()), () -> assertEquals(11, sut.getQ().intValue()),
				() -> assertEquals(2, sut.getG().intValue()));
	}

	@Test
	void failOnMissingRequiredData() throws IOException {
		final String jsonStr = loadJson("encryption_parameters-no_p");

		assertThrows(GeneralCryptoLibException.class, () -> ElGamalEncryptionParameters.fromJson(jsonStr));
	}

	@Test
	void failOnMalformedJson() throws IOException {
		final String jsonStr = loadJson("encryption_parameters-malformed");

		assertThrows(GeneralCryptoLibException.class, () -> ElGamalEncryptionParameters.fromJson(jsonStr));
	}

	private String loadJson(final String fileName) throws IOException {
		final String fullPath = fileName + ".json";
		try (final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fullPath); final Scanner scanner = new Scanner(is)) {
			scanner.useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : "";
		}
	}
}
