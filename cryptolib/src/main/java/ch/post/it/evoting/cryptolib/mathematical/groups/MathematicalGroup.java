/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.groups;

import java.math.BigInteger;

/**
 * Representation of a mathematical group.
 *
 * <p>MathematicalGroups are immutable.
 *
 * @param <E> the type of the group elements of this group, it must be a type which extends {@link GroupElement}.
 */
public interface MathematicalGroup<E extends GroupElement> {

	/**
	 * Checks whether a given element is a member of this {@code MathematicalGroup}.
	 *
	 * @param element group element to check.
	 * @return true if the {@code element} is a member of the group and {@code false} otherwise.
	 */
	boolean isGroupMember(E element);

	/**
	 * Returns the identity element of the group.
	 *
	 * @return the identity element.
	 */
	E getIdentity();

	/**
	 * Returns the q parameter, which is the order of the group.
	 *
	 * @return the q (order) parameter.
	 */
	BigInteger getQ();

	/**
	 * Returns the p parameter, which is the modulus of the group.
	 *
	 * @return the p (modulus) parameter.
	 */
	BigInteger getP();

	/**
	 * Returns the value of the generator of the group.
	 *
	 * @return the generator value.
	 */
	BigInteger getG();

	/**
	 * Returns the generator element of the group.
	 *
	 * @return the generator.
	 */
	E getGenerator();
}
