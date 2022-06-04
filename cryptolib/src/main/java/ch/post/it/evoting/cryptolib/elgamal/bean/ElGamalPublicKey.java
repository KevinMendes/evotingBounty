/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.elgamal.bean;

import static java.lang.Math.min;
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
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpGroupElement;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;

/**
 * Encapsulates an ElGamal public key.
 *
 * <p>Instances of this class contain a list of the Zp group elements corresponding to the key and
 * the Zp subgroup to which these elements belong.
 */
@JsonRootName("publicKey")
public final class ElGamalPublicKey extends AbstractJsonSerializable {

	private final ZpGroupElement[] elements;

	private final ZpSubgroup zpSubgroup;

	/**
	 * Creates an {@link ElGamalPublicKey} object, using the specified list of Zp group elements and the specified Zp subgroup.
	 *
	 * <p>Note: For performance reasons, a group membership check is not performed for any Zp group
	 * element in the specified list. Therefore, this membership should be ensured prior to specifying the list as input to this constructor.
	 *
	 * @param elements   the list of public key Zp group elements.
	 * @param zpSubgroup the Zp subgroup to which the Zp group elements of this public key belong.
	 * @throws GeneralCryptoLibException if the list of public key Zp group elements is null or empty or if the Zp subgroup is null.
	 */
	public ElGamalPublicKey(final List<ZpGroupElement> elements, final ZpSubgroup zpSubgroup) throws GeneralCryptoLibException {
		Validate.notNullOrEmptyAndNoNulls(elements, "List of ElGamal public key elements");
		Validate.notNull(zpSubgroup, "Zp subgroup");
		this.elements = elements.toArray(new ZpGroupElement[0]);
		this.zpSubgroup = zpSubgroup;
	}

	private ElGamalPublicKey(final ZpGroupElement[] elements, final ZpSubgroup zpSubgroup) {
		this.elements = elements;
		this.zpSubgroup = zpSubgroup;
	}

	/**
	 * Deserializes the instance from a string in JSON format.
	 *
	 * @param json the JSON
	 * @return the instance
	 * @throws GeneralCryptoLibException failed to deserialize the instance.
	 */
	public static ElGamalPublicKey fromJson(final String json) throws GeneralCryptoLibException {
		return AbstractJsonSerializable.fromJson(json, ElGamalPublicKey.class);
	}

	/**
	 * Creates an instance from a given memento during JSON deserialization.
	 *
	 * @param memento the memento
	 * @return
	 * @throws GeneralCryptoLibException failed to create the instance.
	 */
	@JsonCreator
	static ElGamalPublicKey fromMemento(final Memento memento) throws GeneralCryptoLibException {
		Validate.notNullOrEmptyAndNoNulls(memento.elements, "List of ElGamal public key elements");
		Validate.notNull(memento.zpSubgroup, "Zp subgroup");
		final ZpGroupElement[] elements = new ZpGroupElement[memento.elements.length];
		for (int i = 0; i < elements.length; i++) {
			elements[i] = new ZpGroupElement(memento.elements[i], memento.zpSubgroup);
		}
		return new ElGamalPublicKey(elements, memento.zpSubgroup);
	}

	/**
	 * Retrieves the list of public key Zp group elements. The returned list is read-only.
	 *
	 * @return the list of Zp group elements.
	 */
	public List<ZpGroupElement> getKeys() {
		return unmodifiableList(asList(elements));
	}

	/**
	 * Retrieves the Zp subgroup to which the public key Zp group elements belong.
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
		result = prime * result + Arrays.hashCode(elements);
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
		final ElGamalPublicKey other = (ElGamalPublicKey) obj;
		if (!ObjectArrays.constantTimeEquals(elements, other.elements)) {
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
		memento.elements = new BigInteger[elements.length];
		for (int i = 0; i < elements.length; i++) {
			memento.elements[i] = elements[i].getValue();
		}
		return memento;
	}

	/**
	 * Memento for JSON serialization.
	 */
	static class Memento {
		@JsonProperty("zpSubgroup")
		public ZpSubgroup zpSubgroup;

		@JsonProperty("elements")
		public BigInteger[] elements;
	}
}
