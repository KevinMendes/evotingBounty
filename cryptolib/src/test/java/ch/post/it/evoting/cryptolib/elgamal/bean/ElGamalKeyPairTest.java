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
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpGroupElement;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;

class ElGamalKeyPairTest {

	private static BigInteger p;
	private static BigInteger q;
	private static BigInteger g;
	private static ZpSubgroup group;
	private static List<Exponent> privKeys;
	private static List<ZpGroupElement> pubKeys;
	private static ElGamalPrivateKey elGamalPrivateKey;
	private static ElGamalPublicKey elGamalPublicKey;
	private static ElGamalKeyPair elGamalKeyPair;

	@BeforeAll
	static void setUp() throws GeneralCryptoLibException {

		p = new BigInteger("23");
		q = new BigInteger("11");
		g = new BigInteger("2");

		group = new ZpSubgroup(g, p, q);

		// create the private key
		privKeys = new ArrayList<>();
		privKeys.add(new Exponent(q, new BigInteger("4")));
		privKeys.add(new Exponent(q, new BigInteger("5")));
		elGamalPrivateKey = new ElGamalPrivateKey(privKeys, group);

		// create the public key
		pubKeys = new ArrayList<>();
		pubKeys.add(new ZpGroupElement(g, group));
		pubKeys.add(new ZpGroupElement(g, group));
		elGamalPublicKey = new ElGamalPublicKey(pubKeys, group);

		// create the key pair
		elGamalKeyPair = new ElGamalKeyPair(elGamalPrivateKey, elGamalPublicKey);
	}

	@Test
	void givenNullPrivateKeyWhenCreateKeyPairThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new ElGamalKeyPair(null, elGamalPublicKey));
	}

	@Test
	void givenNullPublicKeyWhenCreateKeyPairThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new ElGamalKeyPair(elGamalPrivateKey, null));
	}

	@Test
	void givenKeyListsWithDifferentNumbersOfElementsWhenCreateKeyPairThenException() throws GeneralCryptoLibException {
		// create public key with more elements than the private key
		final List<ZpGroupElement> pubKeys = new ArrayList<>();
		pubKeys.add(new ZpGroupElement(g, group));
		pubKeys.add(new ZpGroupElement(g, group));
		pubKeys.add(new ZpGroupElement(g, group));
		final ElGamalPublicKey elGamalPublicKeyLarger = new ElGamalPublicKey(pubKeys, group);

		assertThrows(GeneralCryptoLibException.class, () -> new ElGamalKeyPair(elGamalPrivateKey, elGamalPublicKeyLarger));
	}

	@Test
	void givenKeyListsFromDifferentGroupsWhenCreateKeyPairThenException() throws GeneralCryptoLibException {
		final BigInteger differentQ = new BigInteger("3");
		final ZpSubgroup smallGroup = new ZpSubgroup(g, p, differentQ);

		// create the public key
		final List<ZpGroupElement> pubKeys = new ArrayList<>();
		pubKeys.add(new ZpGroupElement(g, smallGroup));
		pubKeys.add(new ZpGroupElement(g, smallGroup));
		final ElGamalPublicKey elGamalPublicKeyDifferentGroup = new ElGamalPublicKey(pubKeys, smallGroup);

		assertThrows(GeneralCryptoLibException.class, () -> new ElGamalKeyPair(elGamalPrivateKey, elGamalPublicKeyDifferentGroup));
	}

	@Test
	void givenKeyPairWhenGetPublicKeyThenExpectedValues() {
		final ElGamalPublicKey returnedElGamalPublicKey = elGamalKeyPair.getPublicKeys();
		final String errorMsg = "The returned ElGamal public key does not have the expected values";

		assertEquals(elGamalPublicKey, returnedElGamalPublicKey, errorMsg);
	}

	@Test
	void givenKeyPairWhenGetPrivateKeyThenExpectedValues() {
		final ElGamalPrivateKey returnedElGamalPrivateKey = elGamalKeyPair.getPrivateKeys();
		final String errorMsg = "The returned ElGamal private key does not have the expected values";

		assertEquals(elGamalPrivateKey, returnedElGamalPrivateKey, errorMsg);
	}
}
