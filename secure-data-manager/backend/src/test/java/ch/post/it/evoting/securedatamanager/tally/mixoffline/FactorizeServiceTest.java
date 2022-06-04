/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.securedatamanager.tally.mixoffline;

import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevel;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.securedatamanager.services.domain.model.mixing.PrimeFactors;

@DisplayName("Factorize with")
class FactorizeServiceTest {

	private static FactorizeService factorizeService;
	private static GqGroup gqGroup;
	private static List<GqElement> encodingPrimes;
	private static GqElement x;
	private static int psi;

	@BeforeAll
	public static void setUpAll() {
		factorizeService = new FactorizeService();

		try (MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(SecurityLevel.TESTING_ONLY);
			// Set up a GqGroup and valid primes encoding the voting options.
			gqGroup = new GqGroup(BigInteger.valueOf(47), BigInteger.valueOf(23), BigInteger.valueOf(2));
			encodingPrimes = Stream.of(BigInteger.valueOf(3), BigInteger.valueOf(7), BigInteger.valueOf(17), BigInteger.valueOf(37))
					.map(value -> GqElementFactory.fromValue(value, gqGroup)).collect(Collectors.toList());

			// Create a message x to be factorized.
			x = GqElementFactory.fromValue(BigInteger.valueOf(21), gqGroup);

			// Expected number of factors for message x (3 * 7 = 21).
			psi = 2;
		}
	}

	@Test
	@DisplayName("valid parameters works as expected")
	void factorizeTest() {
		final PrimeFactors factors = factorizeService.factorize(x, encodingPrimes, psi);
		assertEquals(psi, factors.size());

		final PrimeFactors expectedFactors = Stream.of(BigInteger.valueOf(3), BigInteger.valueOf(7))
				.map(value -> GqElementFactory.fromValue(value, gqGroup))
				.collect(Collectors.collectingAndThen(toImmutableList(), PrimeFactors::new));
		assertEquals(expectedFactors, factors);
	}

	@Test
	@DisplayName("any null parameters throws NullPointerException")
	void factorizeNullParams() {
		assertThrows(NullPointerException.class, () -> factorizeService.factorize((GqElement) null, encodingPrimes, psi));
		assertThrows(NullPointerException.class, () -> factorizeService.factorize(x, null, psi));
	}

	@Test
	@DisplayName("empty encoding primes throws IllegalArgumentException")
	void factorizeEmptyEncodingPrimes() {
		final List<GqElement> emptyList = Collections.emptyList();
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> factorizeService.factorize(x, emptyList, psi));
		assertEquals("The encoding primes must not be empty.", exception.getMessage());
	}

	@Test
	@DisplayName("invalid psi throws IllegalArgumentException")
	void factorizeInvalidPsi() {
		final IllegalArgumentException illegalArgumentException1 = assertThrows(IllegalArgumentException.class,
				() -> factorizeService.factorize(x, encodingPrimes, 0));
		assertEquals("Psi must be within the bounds [1, 120].", illegalArgumentException1.getMessage());

		final IllegalArgumentException illegalArgumentException2 = assertThrows(IllegalArgumentException.class,
				() -> factorizeService.factorize(x, encodingPrimes, 121));
		assertEquals("Psi must be within the bounds [1, 120].", illegalArgumentException2.getMessage());
	}

	@Test
	@DisplayName("encoding primes not all from same group throws IllegalArgumentException")
	void factorizeEncodingPrimesNotAllSameGroup() {
		try (MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(SecurityLevel.TESTING_ONLY);
			final GqGroup otherGqGroup = new GqGroup(BigInteger.valueOf(23), BigInteger.valueOf(11), BigInteger.valueOf(2));
			final ArrayList<GqElement> invalidEncodingPrimes = new ArrayList<>(encodingPrimes);
			invalidEncodingPrimes.add(GqElementFactory.fromValue(BigInteger.valueOf(4), otherGqGroup));

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> factorizeService.factorize(x, invalidEncodingPrimes, psi));
			assertEquals("All encoding primes must be part of the same group.", exception.getMessage());
		}
	}

	@Test
	@DisplayName("encoding primes containing generator throws IllegalArgumentException")
	void factorizeEncodingPrimesWithGenerator() {
		final List<GqElement> invalidEncodingPrimes = new ArrayList<>(encodingPrimes);
		invalidEncodingPrimes.add(gqGroup.getGenerator());

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> factorizeService.factorize(x, invalidEncodingPrimes, psi));
		assertEquals("The encoding primes must not contain the generator.", exception.getMessage());
	}

	@Test
	@DisplayName("encoding primes containing duplicates throws IllegalArgumentException")
	void factorizeEncodingPrimesWithDuplicate() {
		final List<GqElement> invalidEncodingPrimes = new ArrayList<>(encodingPrimes);
		invalidEncodingPrimes.add(encodingPrimes.get(0));

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> factorizeService.factorize(x, invalidEncodingPrimes, psi));
		assertEquals("The encoding primes must not contain duplicates.", exception.getMessage());
	}

	@Test
	@DisplayName("encoding primes containing non prime throws IllegalArgumentException")
	void factorizeEncodingPrimesWithNonPrime() {
		final List<GqElement> invalidEncodingPrimes = new ArrayList<>(encodingPrimes);
		final GqElement nonPrime = GqElementFactory.fromValue(BigInteger.valueOf(4), gqGroup);
		invalidEncodingPrimes.add(nonPrime);

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> factorizeService.factorize(x, invalidEncodingPrimes, psi));
		assertEquals("The encoding primes must contain only prime numbers.", exception.getMessage());
	}

	@Test
	@DisplayName("x and encoding primes from different groups throws IllegalArgumentException")
	void factorizeXEncodingPrimesDiffGroup() {
		try (MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(SecurityLevel.TESTING_ONLY);
			final GqGroup otherGqGroup = new GqGroup(BigInteger.valueOf(23), BigInteger.valueOf(11), BigInteger.valueOf(2));
			final GqElement otherX = GqElementFactory.fromValue(BigInteger.valueOf(4), otherGqGroup);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> factorizeService.factorize(otherX, encodingPrimes, psi));
			assertEquals("The element x and the encoding primes must be part of the same group.", exception.getMessage());
		}
	}

	@Test
	@DisplayName("factorization not possible with given encoding primes throws IllegalArgumentException")
	void factorizeImpossibleFactorization() {
		final GqElement invalidX = GqElementFactory.fromValue(BigInteger.valueOf(9), gqGroup);

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> factorizeService.factorize(invalidX, encodingPrimes, psi));
		assertEquals("The message x could not be factorized using the provided encoding primes.", exception.getMessage());
	}

	@Test
	@DisplayName("factorization not matching psi throws IllegalArgumentException")
	void factorizeInvalidFactorization() {
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> factorizeService.factorize(x, encodingPrimes, 3));
		String errorMessage = String
				.format("The actual number of prime factors does not match the expected number of factors psi. Expected: %d, found: %d", 3, psi);
		assertEquals(errorMessage, exception.getMessage());
	}
}
