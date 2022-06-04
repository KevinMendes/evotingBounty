/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model.mixing;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.math.GqElement;

/**
 * Represents a collection of prime factors. This class does not do any checks, it is only a container of prime factors for readability.
 */
public class PrimeFactors {

	ImmutableList<GqElement> factors;

	public PrimeFactors(final ImmutableList<GqElement> factors) {
		checkNotNull(factors);
		this.factors = factors;
	}

	public int size() {
		return factors.size();
	}

	public ImmutableList<GqElement> getFactors() {
		return factors;
	}

	@Override
	public String toString() {
		return "PrimeFactors{" + "factors=" + factors + '}';
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PrimeFactors that = (PrimeFactors) o;
		return factors.equals(that.factors);
	}

	@Override
	public int hashCode() {
		return Objects.hash(factors);
	}
}
