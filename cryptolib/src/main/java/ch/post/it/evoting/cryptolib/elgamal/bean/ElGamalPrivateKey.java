/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.elgamal.bean;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonValue;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.binary.ObjectArrays;
import ch.post.it.evoting.cryptolib.commons.serialization.AbstractJsonSerializable;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.Exponent;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;

/**
 * Encapsulates an ElGamal private key.
 *
 * <p>Instances of this class contain a list of the exponents corresponding to the key and the Zp
 * subgroup to which these exponents belong.
 */
@JsonRootName("privateKey")
public final class ElGamalPrivateKey extends AbstractJsonSerializable {

	private final Exponent[] exponents;

	private final ZpSubgroup zpSubgroup;

	/**
	 * Constructs an {@link ElGamalPrivateKey} object, using the specified list of exponents and the specified Zp subgroup.
	 *
	 * <p>Note: For performance reasons, a group membership check is not performed for any exponent in
	 * the specified list. Therefore, this membership should be ensured prior to specifying the list as input to this constructor.
	 *
	 * @param exponents  the list of private key exponents.
	 * @param zpSubgroup the Zp subgroup to which the exponents of this private key belong.
	 * @throws GeneralCryptoLibException if the list of private key exponents is null, empty or contains one more null elements, or if the Zp subgroup
	 *                                   is null.
	 */
	public ElGamalPrivateKey(final List<Exponent> exponents, final ZpSubgroup zpSubgroup) throws GeneralCryptoLibException {
		Validate.notNullOrEmptyAndNoNulls(exponents, "List of ElGamal private key exponents");
		Validate.notNull(zpSubgroup, "Zp subgroup");
		this.exponents = exponents.toArray(new Exponent[0]);
		this.zpSubgroup = zpSubgroup;
	}

	private ElGamalPrivateKey(final Exponent[] exponents, final ZpSubgroup zpSubgroup) {
		this.exponents = exponents;
		this.zpSubgroup = zpSubgroup;
	}

	/**
	 * Deserializes the instance from a string in JSON format.
	 *
	 * @param json the JSON
	 * @return the instance
	 * @throws GeneralCryptoLibException failed to deserialize the instance.
	 */
	public static ElGamalPrivateKey fromJson(final String json) throws GeneralCryptoLibException {
		return AbstractJsonSerializable.fromJson(json, ElGamalPrivateKey.class);
	}

	/**
	 * Creates an instance from a given memento during JSON deserialization.
	 *
	 * @param memento the memento
	 * @return
	 * @throws GeneralCryptoLibException failed to create the instance.
	 */
	@JsonCreator
	static ElGamalPrivateKey fromMemento(final Memento memento) throws GeneralCryptoLibException {
		Validate.notNullOrEmptyAndNoNulls(memento.exponents, "List of ElGamal private key exponents");
		Validate.notNull(memento.zpSubgroup, "Zp subgroup");
		final BigInteger q = memento.zpSubgroup.getQ();
		final Exponent[] exponents = new Exponent[memento.exponents.length];
		for (int i = 0; i < exponents.length; i++) {
			exponents[i] = new Exponent(q, memento.exponents[i]);
		}
		return new ElGamalPrivateKey(exponents, memento.zpSubgroup);
	}

	/**
	 * Retrieves the list of private key exponents. The returned list is read-only.
	 *
	 * @return the list of exponents.
	 */
	public List<Exponent> getKeys() {

		return unmodifiableList(asList(exponents));
	}

	/**
	 * Retrieves the Zp subgroup to which the private key exponents belong.
	 *
	 * @return the Zp subgroup.
	 */
	public ZpSubgroup getGroup() {
		return zpSubgroup;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(exponents);
		result = prime * result + ((zpSubgroup == null) ? 0 : zpSubgroup.hashCode());
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
		final ElGamalPrivateKey other = (ElGamalPrivateKey) obj;
		if (!ObjectArrays.constantTimeEquals(exponents, other.exponents)) {
			return false;
		}
		if (zpSubgroup == null) {
			return other.zpSubgroup == null;
		} else {
			return zpSubgroup.equals(other.zpSubgroup);
		}
	}

	/**
	 * Returns a memento used during JSON serialization.
	 *
	 * @return a memento.
	 */
	@JsonValue
	Memento toMemento() {
		final Memento memento = new Memento();
		memento.zpSubgroup = zpSubgroup;
		memento.exponents = new BigInteger[exponents.length];
		for (int i = 0; i < exponents.length; i++) {
			memento.exponents[i] = exponents[i].getValue();
		}
		return memento;
	}

	/**
	 * Memento for JSON serialization.
	 */
	static class Memento {
		@JsonProperty("zpSubgroup")
		public ZpSubgroup zpSubgroup;

		@JsonProperty("exponents")
		public BigInteger[] exponents;
	}
}
