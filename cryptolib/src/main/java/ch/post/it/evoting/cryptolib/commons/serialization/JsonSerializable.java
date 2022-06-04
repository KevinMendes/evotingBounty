/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.serialization;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Object which can be serialized to JSON format.
 *
 * <p>Classes implementing this interface
 *
 * <ul>
 *   <li>should provide a complimentary static method {@code fromJson} for creating instances from
 *       given JSON strings.
 *   <li>must guarantee that instances returned by {@code fromJson} are valid and fully initialized.
 *   <li>can be used directly with Jackson FasterXML library for both serialization and
 *       deserialization.
 *   <li>must guarantee that instances deserialized by Jackson FasterXML are valid and fully
 *       initialized.
 * </ul>
 */
public interface JsonSerializable {
	/**
	 * Serializes the instance into JSON format.
	 *
	 * @return the JSON string
	 * @throws GeneralCryptoLibException failed to serialize the instance.
	 */
	String toJson() throws GeneralCryptoLibException;
}
