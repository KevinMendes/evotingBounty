/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.elgamal.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.Exponent;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;

class ElGamalPrivateKeyTest {

	private static ZpSubgroup group;
	private static int numKeys;
	private static List<Exponent> privKeys;
	private static ElGamalPrivateKey elGamalPrivateKey;

	@BeforeAll
	static void setUp() throws GeneralCryptoLibException {

		BigInteger p = new BigInteger("23");
		BigInteger q = new BigInteger("11");
		BigInteger g = new BigInteger("2");

		group = new ZpSubgroup(g, p, q);

		numKeys = 2;

		privKeys = new ArrayList<>();
		privKeys.add(new Exponent(q, new BigInteger("4")));
		privKeys.add(new Exponent(q, new BigInteger("5")));

		elGamalPrivateKey = new ElGamalPrivateKey(privKeys, group);
	}

	@Test
	void givenNullKeysListWhenCreatePrivateKeyThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new ElGamalPrivateKey(null, group));
	}

	@Test
	void givenEmptyKeysListWhenCreatePrivateKeyThenException() {
		final List<Exponent> nullKeysList = new ArrayList<>();

		assertThrows(GeneralCryptoLibException.class, () -> new ElGamalPrivateKey(nullKeysList, group));
	}

	@Test
	void givenNullGroupWhenCreatePrivateKeyThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new ElGamalPrivateKey(privKeys, null));
	}

	@Test
	void givenPrivateKeyWhenGetKeysThenExpectedKeys() {
		final List<Exponent> returnedKeys = elGamalPrivateKey.getKeys();

		String errorMsg = "The created private key does not have the expected number of elements";
		assertEquals(numKeys, returnedKeys.size(), errorMsg);

		errorMsg = "The private does not have the expected list of keys";
		assertEquals(privKeys, returnedKeys, errorMsg);
	}

	@Test
	void givenPrivateKeyWhenGetGroupThenExpectedGroup() {
		final String errorMsg = "The created private key does not have the expected group";

		assertEquals(group, elGamalPrivateKey.getGroup(), errorMsg);
	}

	@Test
	void givenJsonStringWhenReconstructThenEqualToOriginalPrivateKey() throws GeneralCryptoLibException {
		final BigInteger p = new BigInteger("23");
		final BigInteger q = new BigInteger("11");
		final BigInteger g = new BigInteger("2");
		final ZpSubgroup smallGroup = new ZpSubgroup(g, p, q);

		final List<Exponent> privKeys = new ArrayList<>();
		privKeys.add(new Exponent(q, new BigInteger("4")));
		privKeys.add(new Exponent(q, new BigInteger("5")));
		final ElGamalPrivateKey expectedElGamalPrivateKey = new ElGamalPrivateKey(privKeys, smallGroup);

		final String jsonStr = elGamalPrivateKey.toJson();

		final ElGamalPrivateKey reconstructedPrivateKey = ElGamalPrivateKey.fromJson(jsonStr);

		final String errorMsg = "The reconstructed ElGamal private key is not equal to the expected key";

		assertEquals(expectedElGamalPrivateKey, reconstructedPrivateKey, errorMsg);
	}
}
