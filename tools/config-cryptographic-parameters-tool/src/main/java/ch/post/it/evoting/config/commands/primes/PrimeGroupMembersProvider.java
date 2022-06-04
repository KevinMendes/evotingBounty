/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.primes;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupElement;

/**
 * Allows the generation of ElGamal data parameters.
 */
@Service
public final class PrimeGroupMembersProvider {

	// Maximum supported number of voting options
	public static final int OMEGA = 1200;

	// Maximum supported number of selections
	public static final int PHI = 120;

	/**
	 * Generates an ElGamalDataParameters using the received encryption parameters.
	 *
	 * @param encryptionParameterP The ElGamal encryption parameter P.
	 * @param encryptionParameterQ The ElGamal encryption parameter Q.
	 * @return a list of prime group numbers.
	 */
	public List<BigInteger> generateVotingOptionRepresentations(final BigInteger encryptionParameterP, final BigInteger encryptionParameterQ,
			final BigInteger encryptionParameterG) {
		final GqGroup gqGroup = new GqGroup(encryptionParameterP, encryptionParameterQ, encryptionParameterG);

		final List<BigInteger> primesList = GqElement.GqElementFactory.getSmallPrimeGroupMembers(gqGroup, OMEGA).stream().map(GroupElement::getValue)
				.collect(Collectors.toList());

		if (primesList.stream().skip((long) OMEGA - PHI).reduce(BigInteger.ONE, BigInteger::multiply).compareTo(encryptionParameterP) >= 0) {
			throw new IllegalStateException("The product of the last phi primes is not smaller than p");
		}
		return primesList;
	}
}
