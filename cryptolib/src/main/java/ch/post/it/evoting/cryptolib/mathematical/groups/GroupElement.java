/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.groups;

import java.math.BigInteger;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.Exponent;

/**
 * Representation of a mathematical group element.
 *
 * <p>GroupElements are immutable.
 */
public interface GroupElement {

	/**
	 * Returns a {@code GroupElement} whose value is {@code (this * element)}.
	 *
	 * @param element The element to be multiplied by this (cannot be null).
	 * @return (this * element).
	 * @throws GeneralCryptoLibException if {@code element} is invalid.
	 */
	GroupElement multiply(GroupElement element) throws GeneralCryptoLibException;

	/**
	 * Returns a {@code GroupElement} whose value is (this<sup>exponent</sup>).
	 *
	 * @param exponent the exponent to which this {@code GroupElement} is to be raised.
	 * @return (this < sup > exponent < / sup >).
	 * @throws GeneralCryptoLibException if {@code exponent} is invalid.
	 */
	GroupElement exponentiate(Exponent exponent) throws GeneralCryptoLibException;

	/**
	 * Returns the element value.
	 *
	 * @return element value.
	 */
	BigInteger getValue();
}
