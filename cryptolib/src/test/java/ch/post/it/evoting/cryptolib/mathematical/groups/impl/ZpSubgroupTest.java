/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.groups.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Tests for {@link ZpSubgroup}.
 */
class ZpSubgroupTest {

	private static BigInteger _p;

	private static BigInteger _q;

	private static BigInteger _g;

	private static ZpSubgroup _groupSmall;

	@BeforeAll
	public static void setUp() throws GeneralCryptoLibException {

		_p = new BigInteger("23");

		_q = new BigInteger("11");

		_g = new BigInteger("2");

		_groupSmall = new ZpSubgroup(_g, _p, _q);
	}

	@Test
	void givenAGroupMemberThenCheckMembership() throws GeneralCryptoLibException {

		final String errorMessage = "This element should be a group element";
		final boolean expectedResult = true;

		// Create a group element member of the group
		final BigInteger value = new BigInteger("1");
		final ZpGroupElement groupMember = new ZpGroupElement(value, _groupSmall);

		createElementAndAssertMembership(groupMember, expectedResult, _groupSmall, errorMessage);
	}

	@Test
	void givenAnElementWithDifferentPAndQThenCheckMembership() throws GeneralCryptoLibException {

		final String errorMessage = "This element should be a group element";
		final boolean expectedResult = false;

		final BigInteger differentQ = new BigInteger("3");
		final BigInteger differentP = new BigInteger("7");

		final ZpSubgroup _group_g2_q3 = new ZpSubgroup(_g, differentP, differentQ);
		final BigInteger value = new BigInteger("1");
		final ZpGroupElement notGroupMember = new ZpGroupElement(value, _group_g2_q3);

		createElementAndAssertMembership(notGroupMember, expectedResult, _groupSmall, errorMessage);
	}

	@Test
	void givenANonGroupMemberWithPositiveValueThenCheckMembership() throws GeneralCryptoLibException {

		final String errorMessage = "This element should not be a group element";
		final boolean expectedResult = false;

		// Create a group element that is not a member of the group
		final BigInteger value = new BigInteger("5");
		final ZpGroupElement groupMember = new ZpGroupElement(value, _groupSmall);

		createElementAndAssertMembership(groupMember, expectedResult, _groupSmall, errorMessage);
	}

	@Test
	void testGetIdentityElementOnce() throws GeneralCryptoLibException {

		final ZpGroupElement identity = new ZpGroupElement(BigInteger.ONE, _groupSmall);

		assertEquals(_groupSmall.getIdentity(), identity, "The element returned is not the expected identity element");
	}

	@Test
	void testGetIdentityElementTwice() throws GeneralCryptoLibException {

		final String errorMessage = "The %s element returned is not the expected identity element";

		final ZpGroupElement identityElement = new ZpGroupElement(BigInteger.ONE, _groupSmall);

		final ZpGroupElement firstIdentity = _groupSmall.getIdentity();
		final ZpGroupElement secondIdentity = _groupSmall.getIdentity();

		assertEquals(identityElement, firstIdentity, String.format(errorMessage, "first"));
		assertEquals(identityElement, secondIdentity, String.format(errorMessage, "second"));
	}

	@Test
	void testGetQ() {
		assertEquals(_q, _groupSmall.getQ(), "The Q element is not the expected one");
	}

	@Test
	void testGetG() {
		assertEquals(_g, _groupSmall.getGenerator().getValue(), "The generator element is not the expected one");
	}

	@Test
	void givenANullGeneratorWhenCreatingGroupThenError() {
		assertThrows(GeneralCryptoLibException.class, () -> new ZpSubgroup(null, _p, _q));
	}

	@Test
	void givenANullOrderWhenCreatingGroupThenError() {
		assertThrows(GeneralCryptoLibException.class, () -> new ZpSubgroup(_g, _p, null));
	}

	@Test
	void testEqualsDifferentObjectType() throws GeneralCryptoLibException {

		final String notAGroup = "I am not a group";
		final String errorMessage = "Expected that objects would not be equals";
		assertNotEquals(new ZpSubgroup(_g, _p, _q), notAGroup, errorMessage);
	}

	@Test
	void testEqualsTrue() throws GeneralCryptoLibException {

		final String errorMessage = "Expected that objects would be equals";
		assertEquals(_groupSmall, new ZpSubgroup(_g, _p, _q), errorMessage);
	}

	/**
	 * Create an element with value {@code elementValue}, and assert the group membership of the element is the expected value.
	 *
	 * @param groupElement   The value of the group element to create.
	 * @param isGroupElement The expected result of calling the isGroupMember method.
	 * @param group          The group that should be used to check the membership of the received element.
	 * @param errorMessage   Error message
	 */
	private void createElementAndAssertMembership(final ZpGroupElement groupElement, final boolean isGroupElement, final ZpSubgroup group,
			final String errorMessage) {

		assertEquals(isGroupElement, group.isGroupMember(groupElement), errorMessage);
	}
}
