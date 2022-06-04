/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.secretsharing;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Interface that defines an API to serialize Objects that implement the Share interface into byte arrays
 */
public interface ShareSerializer {

	/**
	 * Build a {@link Share} from its serialized form.
	 *
	 * @param shareBytes The bytes the {@link Share} is read from.
	 * @return an instance of an Object {@link Share} created from its byte array representation.
	 * @throws GeneralCryptoLibException If there are too many or too little bytes.
	 */
	Share fromByteArray(byte[] shareBytes) throws GeneralCryptoLibException;

	/**
	 * Convert a given share to a byte array representation of this {@link Share} in a byte[].
	 *
	 * @param share The {@link Share} you want to serialize
	 * @return the byte[] representation of the {@link Share}
	 */
	byte[] toByteArray(Share share);
}
