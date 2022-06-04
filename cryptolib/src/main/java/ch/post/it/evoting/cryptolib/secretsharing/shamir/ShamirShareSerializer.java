/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.secretsharing.shamir;

import java.math.BigInteger;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.secretsharing.Share;
import ch.post.it.evoting.cryptolib.api.secretsharing.ShareSerializer;
import ch.post.it.evoting.cryptolib.mathematical.polynomials.Point;

public final class ShamirShareSerializer implements ShareSerializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShamirShareSerializer.class);

	private static final int INT_BYTE_LENGTH = 4;

	private static final int SHORT_BYTE_LENGTH = 2;

	private static short byteLength(final BigInteger bi) {
		return (short) ((bi.bitLength() / 8) + 1);
	}

	private static int calculateLength(final ShamirShare shamirShare) {
		final List<Point> points = shamirShare.getPoints();
		final BigInteger modulus = shamirShare.getModulus();

		final int headerLength = INT_BYTE_LENGTH + INT_BYTE_LENGTH + INT_BYTE_LENGTH + SHORT_BYTE_LENGTH + byteLength(modulus) + INT_BYTE_LENGTH;

		int sharesLength = 0;
		for (final Point point : points) {
			sharesLength += SHORT_BYTE_LENGTH + byteLength(point.getX()) + SHORT_BYTE_LENGTH + byteLength(point.getY());
		}

		return headerLength + sharesLength;
	}

	/**
	 * Convert a given share {@link Share} to a byte array representation. The format is the following:
	 *
	 * <p>numberOfSecrets|numberOfparts|threshold|byteLength(modulus)|modulus|secretLength
	 * |byteLength(pointX)|pointX|byteLength(pointY)|pointY
	 *
	 * <p>The length is codified as a short, so the byte length of pointX, pointY, and the modulus
	 * can be at most {@link Short#MAX_VALUE} (32k). This is more than enough for any sensible combination of parameters.
	 *
	 * @return the byte[] representation of the {@link Share}
	 */
	public byte[] toByteArray(final Share share) {
		final ShamirShare shamirShare;

		if (share instanceof ShamirShare) {
			shamirShare = (ShamirShare) share;
		} else {
			throw new IllegalArgumentException("Incompatible Share type: " + share.getClass().getName());
		}

		// In case the Share is of type MultipleSharesContainer, we have to use its serialization method. This is because the serialization
		// depends on the points, which the MultipleSharesContainer does not have.
		try {
			return share.serialize();
		} catch (final UnsupportedOperationException e) {
			LOGGER.info("{} does not have a serialize method, default shamir serialization will be used.", share.getClass().getName());
		}

		final BigInteger modulus = shamirShare.getModulus();
		final ByteBuffer bb = ByteBuffer.allocate(calculateLength(shamirShare)).putInt(shamirShare.getNumberOfSecrets())
				.putInt(shamirShare.getNumberOfParts()).putInt(shamirShare.getThreshold()).putShort(byteLength(modulus)).put(modulus.toByteArray())
				.putInt(shamirShare.getSecretLength());

		for (int i = 0; i < shamirShare.getNumberOfSecrets(); i++) {
			final Point point = shamirShare.getPoints().get(i);

			bb.putShort(byteLength(point.getX())).put(point.getX().toByteArray()).putShort(byteLength(point.getY())).put(point.getY().toByteArray());
		}

		return bb.array();
	}

	/**
	 * Build a {@link ShamirShare} from its serialized form.
	 *
	 * @param shamirShareBytes The bytes the {@link ShamirShare} is read from.
	 * @throws GeneralCryptoLibException If there are too many or too little bytes.
	 */
	public Share fromByteArray(final byte[] shamirShareBytes) throws GeneralCryptoLibException {
		final ByteBuffer bb = ByteBuffer.wrap(shamirShareBytes);
		try {
			// read integers
			final int numberOfSecrets = bb.getInt();
			final int numberOfParts = bb.getInt();
			final int threshold = bb.getInt();

			// read the modulus (length mod, mod)
			final byte[] modBytes = new byte[bb.getShort()];
			bb.get(modBytes);
			final BigInteger modulus = new BigInteger(modBytes);

			final int secretLength = bb.getInt();

			// read the points (length x, x, length y, y)
			final List<Point> points = new ArrayList<>(numberOfSecrets);
			for (int i = 0; i < numberOfSecrets; i++) {
				final byte[] xBytes = new byte[bb.getShort()];
				bb.get(xBytes);
				final byte[] yBytes = new byte[bb.getShort()];
				bb.get(yBytes);
				points.add(i, new Point(new BigInteger(xBytes), new BigInteger(yBytes)));
			}

			// assert there are no bytes left to read
			if (bb.hasRemaining()) {
				throw new GeneralCryptoLibException("There are bytes left in the buffer after decoding.");
			}

			return new ShamirShare(numberOfParts, threshold, modulus, secretLength, points);

		} catch (final BufferUnderflowException bue) {
			throw new GeneralCryptoLibException("The byte array is shorter than expected.", bue);
		} finally {
			// clear the entry parameter, and the used byte buffer.
			Arrays.fill(shamirShareBytes, (byte) 0x00);
			bb.clear();
			bb.put(shamirShareBytes);
		}
	}
}
