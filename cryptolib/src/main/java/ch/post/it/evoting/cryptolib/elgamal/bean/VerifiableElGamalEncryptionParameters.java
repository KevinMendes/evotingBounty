/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.elgamal.bean;

import java.math.BigInteger;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Represents verifiable ElGamal Encryption parameters.
 */
@JsonRootName("encryptionParams")
@JsonIgnoreProperties({ "group" })
public final class VerifiableElGamalEncryptionParameters extends ElGamalEncryptionParameters {

	private final String seed;

	private final int pCounter;

	private final int qCounter;

	/**
	 * Constructs an instance of verifiable ElGamal encryption parameters.
	 *
	 * @param p        the group modulus.
	 * @param q        the group order.
	 * @param g        the group generator.
	 * @param seed     the seed used to produce p, q, g.
	 * @param pCounter the counter used to find P.
	 * @param qCounter the counter used to find Q.
	 */
	@JsonCreator
	VerifiableElGamalEncryptionParameters(
			@JsonProperty(value = "p", required = true)
			final
			BigInteger p,
			@JsonProperty(value = "q", required = true)
			final
			BigInteger q,
			@JsonProperty(value = "g", required = true)
			final
			BigInteger g,
			@JsonProperty(value = "seed", required = true)
			final
			String seed,
			@JsonProperty(value = "pCounter", required = true)
			final
			int pCounter,
			@JsonProperty(value = "qCounter", required = true)
			final
			int qCounter) throws GeneralCryptoLibException {
		super(p, q, g);

		Objects.requireNonNull(seed, "The seed is required");
		final BigInteger pMinusOne = p.subtract(BigInteger.ONE);
		if (q.compareTo(BigInteger.ONE) < 0) {
			throw new IllegalArgumentException("Q must be at least 1");
		}
		if (q.compareTo(pMinusOne) > 0) {
			throw new IllegalArgumentException("Q must be smaller than P");
		}
		if (g.compareTo(BigInteger.valueOf(2)) < 0) {
			throw new IllegalArgumentException("G must be at least 2");
		}
		if (g.compareTo(pMinusOne) > 0) {
			throw new IllegalArgumentException("G must be smaller than P");
		}

		this.seed = seed;
		this.pCounter = pCounter;
		this.qCounter = qCounter;
	}

	public VerifiableElGamalEncryptionParameters(final BigInteger p, final BigInteger q, final BigInteger g, final String seed)
			throws GeneralCryptoLibException {
		this(p, q, g, seed, 0, 0);
	}

	/**
	 * Deserializes the instance from a string in JSON format.
	 *
	 * @param json the JSON
	 * @return the instance
	 * @throws GeneralCryptoLibException failed to deserialize the instance.
	 */
	public static VerifiableElGamalEncryptionParameters fromJson(final String json) throws GeneralCryptoLibException {
		return fromJson(json, VerifiableElGamalEncryptionParameters.class);
	}

	@JsonProperty("seed")
	public String getSeed() {
		return seed;
	}

	@JsonProperty("pCounter")
	public int getPCounter() {
		return pCounter;
	}

	@JsonProperty("qCounter")
	public int getQCounter() {
		return qCounter;
	}

	@Override
	public int hashCode() {
		return Objects.hash(g, p, q, seed, pCounter, qCounter);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final VerifiableElGamalEncryptionParameters other = (VerifiableElGamalEncryptionParameters) obj;
		if (!g.equals(other.g)) {
			return false;
		}
		if (!p.equals(other.p)) {
			return false;
		}
		if (!q.equals(other.q)) {
			return false;
		}
		if (!seed.equals(other.seed)) {
			return false;
		}
		if (pCounter != other.pCounter) {
			return false;
		}
		return qCounter == other.qCounter;
	}
}
