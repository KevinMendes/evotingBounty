/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.tally.mixoffline;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.math.BigIntegerOperationsService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupElement;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.securedatamanager.services.domain.model.mixing.PrimeFactors;

@Service
class FactorizeService {

	/**
	 * Factorize a collection of elements.
	 * <p>
	 * {@link FactorizeService#factorize(GqElement, List, int)} for more details on inputs.
	 *
	 * @param voteOptions    the elements to factorize
	 * @param encodingPrimes the set of potential prime factors
	 * @param psi            the expected number of factors
	 * @return an immutable list of prime factors, one prime factors collection per input element.
	 */
	ImmutableList<PrimeFactors> factorize(final GroupVector<GqElement, GqGroup> voteOptions, final List<GqElement> encodingPrimes, final int psi) {
		checkNotNull(voteOptions);
		return voteOptions.stream().map(element -> factorize(element, encodingPrimes, psi)).collect(toImmutableList());
	}

	/**
	 * Factorizes a group element {@code x} into its prime factors. Factorizing the element is efficient since the primes are small and the set of
	 * primes is known (as {@code encodingPrimes}).
	 * <p>
	 * The element and encoding primes must be part of the same group.
	 *
	 * @param x              the element to factorize. Non-null.
	 * @param encodingPrimes the set of primes encoding the voting options. Non-empty, list of distinct primes from the same group without its
	 *                       generator.
	 * @param psi            the expected number of factors of {@code x}. In the range [1, 120].
	 * @return a list of size {@code psi} containing the prime factors, picked from {@code encodingPrimes}, of message {@code x}.
	 */
	@VisibleForTesting
	PrimeFactors factorize(final GqElement x, final List<GqElement> encodingPrimes, final int psi) {
		checkNotNull(x);
		checkNotNull(encodingPrimes);

		checkArgument(!encodingPrimes.isEmpty(), "The encoding primes must not be empty.");
		checkArgument(1 <= psi && psi <= 120, "Psi must be within the bounds [1, 120].");

		// Encoding primes validity checking.
		final boolean allPrimesSameGroup = encodingPrimes.stream().map(GroupElement::getGroup).distinct().count() <= 1;
		checkArgument(allPrimesSameGroup, "All encoding primes must be part of the same group.");
		final GqGroup gqGroup = encodingPrimes.get(0).getGroup();

		checkArgument(!encodingPrimes.contains(gqGroup.getGenerator()), "The encoding primes must not contain the generator.");

		final HashSet<GqElement> distinctEncodingPrimes = new HashSet<>(encodingPrimes);
		checkArgument(distinctEncodingPrimes.size() == encodingPrimes.size(), "The encoding primes must not contain duplicates.");

		final boolean areAllPrimes = encodingPrimes.stream().map(GqElement::getValue).allMatch(BigIntegerOperationsService::isSmallPrime);
		checkArgument(areAllPrimes, "The encoding primes must contain only prime numbers.");

		// Cross group checking.
		checkArgument(x.getGroup().equals(gqGroup), "The element x and the encoding primes must be part of the same group.");

		// Algorithm operations.
		final ImmutableList<GqElement> factors = encodingPrimes.stream().filter(pk -> x.getValue().remainder(pk.getValue()).equals(BigInteger.ZERO))
				.collect(toImmutableList());

		final GqElement product = factors.stream().reduce(gqGroup.getIdentity(), GqElement::multiply);
		if (!x.equals(product)) {
			throw new IllegalArgumentException("The message x could not be factorized using the provided encoding primes.");
		}

		if (factors.size() != psi) {
			throw new IllegalArgumentException(
					String.format("The actual number of prime factors does not match the expected number of factors psi. Expected: %d, found: %d",
							psi, factors.size()));
		}

		return new PrimeFactors(factors);
	}

}
