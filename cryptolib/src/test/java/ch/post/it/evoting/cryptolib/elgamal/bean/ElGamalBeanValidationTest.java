/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.elgamal.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.Exponent;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpGroupElement;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;
import ch.post.it.evoting.cryptolib.test.tools.configuration.GroupLoader;

class ElGamalBeanValidationTest {

	private static ZpSubgroup zpSubgroup;
	private static List<Exponent> exponentList;
	private static List<ZpGroupElement> elementList;
	private static List<Exponent> emptyExponentList;
	private static List<ZpGroupElement> elementListWithNullValue;

	@BeforeAll
	static void setUp() throws GeneralCryptoLibException {

		final GroupLoader zpGroupLoader = new GroupLoader();
		zpSubgroup = new ZpSubgroup(zpGroupLoader.getG(), zpGroupLoader.getP(), zpGroupLoader.getQ());

		exponentList = new ArrayList<>();
		exponentList.add(new Exponent(zpSubgroup.getQ(), BigInteger.TEN));

		elementList = new ArrayList<>();
		elementList.add(new ZpGroupElement(BigInteger.ONE, zpSubgroup));

		emptyExponentList = new ArrayList<>();

		elementListWithNullValue = new ArrayList<>(elementList);
		elementListWithNullValue.add(null);
	}

	static Stream<Arguments> createElGamalEncryptionParameters() {

		final BigInteger p = zpSubgroup.getP();
		final BigInteger q = zpSubgroup.getQ();
		final BigInteger g = zpSubgroup.getG();

		return Stream.of(arguments(null, q, g, "Zp subgroup p parameter is null."), arguments(p, null, g, "Zp subgroup q parameter is null."),
				arguments(p, q, null, "Zp subgroup generator is null."),
				arguments(p, BigInteger.ZERO, g, "Zp subgroup q parameter must be greater than or equal to : 1; Found 0"), arguments(p, p, g,
						"Zp subgroup q parameter must be less than or equal to Zp subgroup p parameter minus 1: " + p.subtract(BigInteger.ONE)
								+ "; Found " + p),
				arguments(p, q, BigInteger.valueOf(1), "Zp subgroup generator must be greater than or equal to : 2; Found 1"), arguments(p, q, p,
						"Zp subgroup generator must be less than or equal to Zp subgroup p parameter minus 1: " + p.subtract(BigInteger.ONE)
								+ "; Found " + p));
	}

	static Stream<Arguments> createElGamalKeyPair() throws GeneralCryptoLibException {

		final ElGamalPrivateKey privateKey = new ElGamalPrivateKey(exponentList, zpSubgroup);
		final ElGamalPublicKey publicKey = new ElGamalPublicKey(elementList, zpSubgroup);

		final List<Exponent> exponentsLarger = new ArrayList<>(exponentList);
		exponentsLarger.add(new Exponent(zpSubgroup.getQ(), BigInteger.TEN));
		final ElGamalPrivateKey privateKeyWithMoreExponents = new ElGamalPrivateKey(exponentsLarger, zpSubgroup);

		final GroupLoader qrGroupLoader = new GroupLoader(2);
		final ZpSubgroup qrSubgroup = new ZpSubgroup(qrGroupLoader.getG(), qrGroupLoader.getP(), qrGroupLoader.getQ());
		final ElGamalPrivateKey privateKeyForOtherGroup = new ElGamalPrivateKey(exponentList, qrSubgroup);

		return Stream.of(arguments(null, publicKey, "ElGamal private key is null."), arguments(privateKey, null, "ElGamal public key is null."),
				arguments(privateKeyWithMoreExponents, publicKey,
						"ElGamal private key length must be equal to ElGamal public key length: " + publicKey.getKeys().size() + "; Found "
								+ privateKeyWithMoreExponents.getKeys().size()),
				arguments(privateKeyForOtherGroup, publicKey, "ElGamal public and private keys must belong to same mathematical group."));
	}

	static Stream<Arguments> createElGamalPrivateKey() {

		final List<Exponent> exponentsWithNullValue = new ArrayList<>(exponentList);
		exponentsWithNullValue.add(null);

		return Stream.of(arguments(null, zpSubgroup, "List of ElGamal private key exponents is null."),
				arguments(emptyExponentList, zpSubgroup, "List of ElGamal private key exponents is empty."),
				arguments(exponentsWithNullValue, zpSubgroup, "List of ElGamal private key exponents contains one or more null elements."),
				arguments(exponentList, null, "Zp subgroup is null."));
	}

	static Stream<Arguments> createElGamalPublicKey() {

		return Stream.of(arguments(null, zpSubgroup, "List of ElGamal public key elements is null."),
				arguments(emptyExponentList, zpSubgroup, "List of ElGamal public key elements is empty."),
				arguments(elementListWithNullValue, zpSubgroup, "List of ElGamal public key elements contains one or more null elements."),
				arguments(elementList, null, "Zp subgroup is null."));
	}

	static Stream<Arguments> createWitnessImpl() {
		return Stream.of(arguments(null, "Exponent is null."));
	}

	@ParameterizedTest
	@MethodSource("createElGamalEncryptionParameters")
	void testElGamalEncryptionParametersCreationValidation(final BigInteger p, final BigInteger q, final BigInteger g, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> new ElGamalEncryptionParameters(p, q, g));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("createElGamalKeyPair")
	void testElGamalKeyPairCreationValidation(final ElGamalPrivateKey privateKey, final ElGamalPublicKey publicKey, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> new ElGamalKeyPair(privateKey, publicKey));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("createElGamalPrivateKey")
	void testElGamalPrivateKeyCreationValidation(final List<Exponent> exponents, final ZpSubgroup zpSubgroup, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> new ElGamalPrivateKey(exponents, zpSubgroup));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("createElGamalPublicKey")
	void testElGamalPublicKeyCreationValidation(final List<ZpGroupElement> elements, final ZpSubgroup zpSubgroup, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> new ElGamalPublicKey(elements, zpSubgroup));
		assertEquals(errorMsg, exception.getMessage());
	}
}
