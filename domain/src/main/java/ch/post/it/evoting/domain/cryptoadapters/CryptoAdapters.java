/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.cryptoadapters;

import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPrivateKey;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPublicKey;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.Exponent;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpGroupElement;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;

/**
 * Adapters to convert between the cryptolib and crypto-primitives objects
 */
public final class CryptoAdapters {

	private CryptoAdapters() {
		//Intentionally left blank
	}

	/*
		Converts a cryptolib exponent to a ZqElement
	 */
	public static ZqElement convert(Exponent cryptolibExponent) {
		checkNotNull(cryptolibExponent);
		BigInteger value = cryptolibExponent.getValue();
		BigInteger q = cryptolibExponent.getQ();
		ZqGroup zqGroup = new ZqGroup(q);
		return ZqElement.create(value, zqGroup);
	}

	/*
		Converts an ElGamalPrivateKey to an ElGamalMultiRecipientPrivateKey
	 */
	public static ElGamalMultiRecipientPrivateKey convert(ElGamalPrivateKey cryptolibPrivateKey) {
		checkNotNull(cryptolibPrivateKey);
		List<ZqElement> privateKeyElements = cryptolibPrivateKey.getKeys().stream().map(CryptoAdapters::convert).collect(Collectors.toList());
		return new ElGamalMultiRecipientPrivateKey(privateKeyElements);
	}

	/*
		Converts an ElGamalPublicKey to an ElGamalMultiRecipientPublicKey.
	 */
	public static ElGamalMultiRecipientPublicKey convert(final ElGamalPublicKey elGamalPublicKey) {
		checkNotNull(elGamalPublicKey);

		final ZpGroupElement generator = elGamalPublicKey.getGroup().getGenerator();
		final GqGroup gqGroup = new GqGroup(generator.getP(), generator.getQ(), generator.getValue());

		return elGamalPublicKey.getKeys().stream()
				.map(key -> CryptoAdapters.convert(key, gqGroup))
				.collect(Collectors.collectingAndThen(Collectors.toList(), ElGamalMultiRecipientPublicKey::new));
	}

	/**
	 * Converts a cryptolib ZpGroupElement to a GqElement.
	 *
	 * @param cryptolibElement the element to convert
	 * @param gqGroup          the gq group
	 * @return a GqElement with value the same as the cryptolibElement and the group defined by the generator element.
	 */
	public static GqElement convert(ZpGroupElement cryptolibElement, GqGroup gqGroup) {
		checkNotNull(cryptolibElement);
		checkNotNull(gqGroup);

		return GqElementFactory.fromValue(cryptolibElement.getValue(), gqGroup);
	}

	/*
		Converts a cryptolib ElGamalPrivateKey to an ElGamalMultiRecipientKeyPair
	*/
	public static ElGamalMultiRecipientKeyPair toElGamalMultiRecipientKeyPair(final ElGamalPrivateKey cryptolibPrivateKey) {
		checkNotNull(cryptolibPrivateKey);

		final ZpGroupElement generatorCryptolib = cryptolibPrivateKey.getGroup().getGenerator();
		final GqGroup gqGroup = new GqGroup(generatorCryptolib.getP(), generatorCryptolib.getQ(), generatorCryptolib.getValue());
		final GqElement generator = convert(generatorCryptolib, gqGroup);

		final ElGamalMultiRecipientPrivateKey privateKey = convert(cryptolibPrivateKey);

		return ElGamalMultiRecipientKeyPair.from(privateKey, generator);
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------
	// Crypto-primitives to cryptolib.
	// -----------------------------------------------------------------------------------------------------------------------------------------------

	public static ElGamalPublicKey convert(final ElGamalMultiRecipientPublicKey elGamalMultiRecipientPublicKey) {
		final BigInteger p = elGamalMultiRecipientPublicKey.getGroup().getP();
		final BigInteger q = elGamalMultiRecipientPublicKey.getGroup().getQ();
		final BigInteger g = elGamalMultiRecipientPublicKey.getGroup().getGenerator().getValue();

		try {
			final ZpSubgroup zpSubgroup = new ZpSubgroup(g, p, q);

			// Convert crypto-primitives public key to cryptolib public key.
			final List<ZpGroupElement> elements = new ArrayList<>();
			for (GqElement publicKeyElement : elGamalMultiRecipientPublicKey.getKeyElements()) {
				elements.add(new ZpGroupElement(publicKeyElement.getValue(), zpSubgroup));
			}
			return new ElGamalPublicKey(elements, zpSubgroup);

		} catch (GeneralCryptoLibException e) {
			throw new IllegalArgumentException("Failed to convert to ElGamalPublicKey");
		}
	}

	public static ElGamalPrivateKey convert(final ElGamalMultiRecipientPrivateKey elGamalMultiRecipientPrivateKey, final GqGroup gqGroup) {
		checkNotNull(elGamalMultiRecipientPrivateKey);
		checkNotNull(gqGroup);

		checkArgument(elGamalMultiRecipientPrivateKey.getGroup().hasSameOrderAs(gqGroup));
		final BigInteger p = gqGroup.getP();
		final BigInteger q = gqGroup.getQ();
		final BigInteger g = gqGroup.getGenerator().getValue();

		try {
			final ZpSubgroup zpSubgroup = new ZpSubgroup(g, p, q);

			// Convert crypto-primitives private key to cryptolib private key.
			final List<Exponent> exponents = new ArrayList<>();
			for (int i = 0; i < elGamalMultiRecipientPrivateKey.size(); i++) {
				final ZqElement privateKeyElement = elGamalMultiRecipientPrivateKey.get(i);
				exponents.add(new Exponent(q, privateKeyElement.getValue()));
			}
			return new ElGamalPrivateKey(exponents, zpSubgroup);
		} catch (GeneralCryptoLibException e) {
			throw new IllegalArgumentException("Failed to convert to ElGamalPublicKey");
		}
	}

	public static ZpGroupElement convert(final GqElement gqElement) {
		final BigInteger value = gqElement.getValue();
		final BigInteger p = gqElement.getGroup().getP();
		final BigInteger q = gqElement.getGroup().getQ();
		try {
			return new ZpGroupElement(value, p, q);
		} catch (GeneralCryptoLibException e) {
			throw new IllegalArgumentException("Failed to convert GqElement.", e);
		}
	}

	public static Exponent convert(final ZqElement zqElement) {
		checkNotNull(zqElement);

		final BigInteger q = zqElement.getGroup().getQ();
		final BigInteger value = zqElement.getValue();

		try {
			return new Exponent(q, value);
		} catch (GeneralCryptoLibException e) {
			throw new IllegalArgumentException("Fauled to convert to ZqElement.", e);
		}
	}
}
