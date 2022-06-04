/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.elgamal.bean;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.serialization.AbstractJsonSerializable;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;
import ch.post.it.evoting.cryptolib.mathematical.groups.MathematicalGroup;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpGroupElement;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;

/**
 * Represents ElGamal Encryption parameters.
 */
@JsonRootName("encryptionParams")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElGamalEncryptionParameters extends AbstractJsonSerializable {
	protected final BigInteger p;

	protected final BigInteger q;

	protected final BigInteger g;

	@JsonIgnore
	protected ZpSubgroup group;

	/**
	 * Represents ElGamal encryption parameters.
	 *
	 * @param p the modulus of the group.
	 * @param q the order of the group.
	 * @param g the generator of the group.
	 * @throws GeneralCryptoLibException if any of the arguments are null.
	 */
	@JsonCreator
	public ElGamalEncryptionParameters(
			@JsonProperty(value = "p", required = true)
			final
			BigInteger p,
			@JsonProperty(value = "q", required = true)
			final
			BigInteger q,
			@JsonProperty(value = "g", required = true)
			final
			BigInteger g) throws GeneralCryptoLibException {
		Validate.notNull(p, "Zp subgroup p parameter");
		Validate.notNull(q, "Zp subgroup q parameter");
		Validate.notNull(g, "Zp subgroup generator");
		final BigInteger pMinusOne = p.subtract(BigInteger.ONE);
		Validate.inRange(q, BigInteger.ONE, pMinusOne, "Zp subgroup q parameter", "", "Zp subgroup p parameter minus 1");
		Validate.inRange(g, BigInteger.valueOf(2), pMinusOne, "Zp subgroup generator", "", "Zp subgroup p parameter minus 1");
		this.p = p;
		this.q = q;
		this.g = g;

		group = new ZpSubgroup(this.g, this.p, this.q);
	}

	/**
	 * Deserializes the instance from a string in JSON format.
	 *
	 * @param json the JSON
	 * @return the instance
	 * @throws GeneralCryptoLibException failed to deserialize the instance.
	 */
	public static ElGamalEncryptionParameters fromJson(final String json) throws GeneralCryptoLibException {
		return fromJson(json, ElGamalEncryptionParameters.class);
	}

	/**
	 * Extracts {@link java.math.BigInteger} value of modulus (parameter p).
	 *
	 * @return modulus (parameter p).
	 */
	@JsonProperty("p")
	public BigInteger getP() {
		return p;
	}

	/**
	 * Extracts {@link java.math.BigInteger} value of order (parameter q).
	 *
	 * @return order (parameter q).
	 */
	@JsonProperty("q")
	public BigInteger getQ() {
		return q;
	}

	/**
	 * Extracts {@link java.math.BigInteger} value of {@link ZpGroupElement} generator (parameter g).
	 *
	 * @return generator (parameter g).
	 */
	@JsonProperty("g")
	public BigInteger getG() {
		return g;
	}

	public MathematicalGroup<?> getGroup() {
		return group;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((g == null) ? 0 : g.hashCode());
		result = prime * result + ((p == null) ? 0 : p.hashCode());
		result = prime * result + ((q == null) ? 0 : q.hashCode());
		return result;
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
		final ElGamalEncryptionParameters other = (ElGamalEncryptionParameters) obj;
		if (g == null) {
			if (other.g != null) {
				return false;
			}
		} else if (!g.equals(other.g)) {
			return false;
		}
		if (p == null) {
			if (other.p != null) {
				return false;
			}
		} else if (!p.equals(other.p)) {
			return false;
		}
		if (q == null) {
			return other.q == null;
		} else {
			return q.equals(other.q);
		}
	}
}
