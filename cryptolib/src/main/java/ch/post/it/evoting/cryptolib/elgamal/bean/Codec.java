/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.elgamal.bean;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.Exponent;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpGroupElement;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;

/**
 * Utility class for binary serialization of {@link ElGamalPrivateKey} and {@link ElGamalPublicKey}.
 *
 * <p>This class is thread-safe.
 */
class Codec {
	private Codec() {
	}

	/**
	 * Decodes {@link ElGamalPrivateKey} from given bytes.
	 *
	 * @param bytes the bytes
	 * @return the key
	 * @throws GeneralCryptoLibException failed to decode the key.
	 */
	public static ElGamalPrivateKey decodePrivateKey(final byte[] bytes) throws GeneralCryptoLibException {
		final ByteBuffer buffer = ByteBuffer.wrap(bytes);
		final ZpSubgroup group = decodeGroup(buffer);
		final int size = decodeInt(buffer);
		final List<Exponent> exponents = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			final BigInteger value = decodeBigInteger(buffer);
			exponents.add(new Exponent(group.getQ(), value));
		}
		return new ElGamalPrivateKey(exponents, group);
	}

	/**
	 * Decodes {@link ElGamalPublicKey} from given bytes.
	 *
	 * @param bytes the bytes
	 * @return the key
	 * @throws GeneralCryptoLibException failed to decode the key.
	 */
	public static ElGamalPublicKey decodePublicKey(final byte[] bytes) throws GeneralCryptoLibException {
		final ByteBuffer buffer = ByteBuffer.wrap(bytes);
		final ZpSubgroup group = decodeGroup(buffer);
		final int size = decodeInt(buffer);
		final List<ZpGroupElement> elements = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			final BigInteger value = decodeBigInteger(buffer);
			elements.add(new ZpGroupElement(value, group));
		}
		return new ElGamalPublicKey(elements, group);
	}

	/**
	 * Encodes a given {@link ElGamalPrivateKey}.
	 *
	 * @param key the key
	 * @return the bytes.
	 */
	public static byte[] encode(final ElGamalPrivateKey key) {
		final List<ByteBuffer> buffers = new LinkedList<>();
		encode(buffers, key);
		return concatenate(buffers).array();
	}

	/**
	 * Encodes a given {@link ElGamalPublicKey}.
	 *
	 * @param key the key
	 * @return the bytes.
	 */
	public static byte[] encode(final ElGamalPublicKey key) {
		final List<ByteBuffer> buffers = new LinkedList<>();
		encode(buffers, key);
		return concatenate(buffers).array();
	}

	private static void checkRemaining(final ByteBuffer buffer, final int length) throws GeneralCryptoLibException {
		if (buffer.remaining() < length) {
			throw new GeneralCryptoLibException("Not enough data.");
		}
	}

	private static ByteBuffer concatenate(final List<ByteBuffer> buffers) {
		int length = 0;
		for (final ByteBuffer buffer : buffers) {
			buffer.rewind();
			length += buffer.remaining();
		}
		final ByteBuffer bytes = ByteBuffer.allocate(length);
		buffers.forEach(bytes::put);
		return bytes;
	}

	private static BigInteger decodeBigInteger(final ByteBuffer buffer) throws GeneralCryptoLibException {
		final int length = decodeInt(buffer);
		checkRemaining(buffer, length);
		final byte[] bytes = new byte[length];
		buffer.get(bytes);
		return new BigInteger(bytes);
	}

	private static ZpSubgroup decodeGroup(final ByteBuffer buffer) throws GeneralCryptoLibException {
		final BigInteger p = decodeBigInteger(buffer);
		final BigInteger q = decodeBigInteger(buffer);
		final BigInteger g = decodeBigInteger(buffer);
		return new ZpSubgroup(g, p, q);
	}

	private static int decodeInt(final ByteBuffer buffer) throws GeneralCryptoLibException {
		checkRemaining(buffer, Integer.BYTES);
		return buffer.getInt();
	}

	private static void encode(final List<ByteBuffer> buffers, final BigInteger value) {
		final ByteBuffer buffer = ByteBuffer.wrap(value.toByteArray());
		encode(buffers, buffer.limit());
		buffers.add(buffer);
	}

	private static void encode(final List<ByteBuffer> buffers, final ElGamalPrivateKey key) {
		encode(buffers, key.getGroup());
		encode(buffers, key.getKeys().size());
		for (final Exponent exponent : key.getKeys()) {
			encode(buffers, exponent.getValue());
		}
	}

	private static void encode(final List<ByteBuffer> buffers, final ElGamalPublicKey key) {
		encode(buffers, key.getGroup());
		encode(buffers, key.getKeys().size());
		for (final ZpGroupElement element : key.getKeys()) {
			encode(buffers, element.getValue());
		}
	}

	private static void encode(final List<ByteBuffer> buffers, final int value) {
		final ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.putInt(value);
		buffers.add(buffer);
	}

	private static void encode(final List<ByteBuffer> buffers, final ZpSubgroup group) {
		encode(buffers, group.getP());
		encode(buffers, group.getQ());
		encode(buffers, group.getG());
	}
}
