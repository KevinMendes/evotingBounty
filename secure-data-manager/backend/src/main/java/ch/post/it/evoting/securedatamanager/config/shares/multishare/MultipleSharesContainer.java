/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.multishare;

import java.math.BigInteger;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.secretsharing.ThresholdSecretSharingServiceAPI;
import ch.post.it.evoting.cryptolib.mathematical.polynomials.Point;
import ch.post.it.evoting.cryptolib.secretsharing.service.ThresholdSecretSharingService;
import ch.post.it.evoting.cryptolib.secretsharing.shamir.ShamirShare;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.ConfigSharesException;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SharesException;

/**
 * This a container class that allows a list of shares to be group together. This container also acts as a share itself (it can be written to, and
 * read from a smartcard using the infrastructure that reads and writes Share objects).
 * <p>
 * Note: this class is based on the Share class from the Shares library.
 * <p>
 * This container was created to handle the situation where multiple secrets must be split and stored (typically on smartcards). The approach that is
 * followed is to perform a split on each of the secrets, and then group one share from each of the sets of outputs (of the split operation) into
 * containers (these containers could be considered 'super-shares', as they contain within them multiple shares).
 */
public final class MultipleSharesContainer extends ShamirShare {

	private static final int MSC_INT_BYTE_LENGTH = 4;

	// note: this field has the same meaning as the field with the same name in its parent class.
	// Therefore, it does not
	// refer to the number of shares in this particular container, which can be obtained from the size
	// of the list of
	// shares field in this class.
	private final int numberOfParts;

	private final int threshold;

	private final List<byte[]> shares;

	private ThresholdSecretSharingServiceAPI thresholdSecretSharingServiceAPI;

	/**
	 * Initialize from a serialized object.
	 *
	 * @param serialized the serialised object.
	 * @param modulus    the modulus for each secret of the set of shares this one belongs to.
	 */
	public MultipleSharesContainer(byte[] serialized, final BigInteger modulus) throws SharesException {

		super(0, 0, modulus, 0, Collections.emptyList());

		initSecretSharingService();

		basicInputValidations(serialized);

		shares = new ArrayList<>();

		ByteBuffer bb = ByteBuffer.wrap(serialized);
		try {

			bb.getInt(); // read the modulus length.
			numberOfParts = bb.getInt();
			threshold = bb.getInt();

			int numShares = bb.getInt();
			for (int i = 0; i < numShares; i++) {
				byte[] shareBytes = new byte[bb.getInt()];
				bb.get(shareBytes);
				shares.add(shareBytes);
			}

		} catch (BufferUnderflowException bue) {
			throw new SharesException("The byte array is shorter than expected.", bue);
		} finally {
			// clear the entry parameter, and the used byte buffer.
			Arrays.fill(serialized, (byte) 0x00);
			bb.clear();
			bb.put(serialized);
		}
	}

	/**
	 * Get the modulus info from the serialized data
	 *
	 * @param serialized the serialized data.
	 * @return the modulus of the data as a BigInteger.
	 */
	public static BigInteger getModulusFromSerializedData(byte[] serialized) {
		ByteBuffer bb = ByteBuffer.wrap(serialized);
		int modulusLength = bb.getInt();

		byte[] modulusBytes = Arrays.copyOfRange(serialized, serialized.length - modulusLength, serialized.length);

		return new BigInteger(modulusBytes);
	}

	/**
	 * @return the list of shares.
	 */
	public List<byte[]> getShares() {
		return shares;
	}

	/**
	 * Obtain a serialized representation of the data. The format is of the form:
	 * <p>
	 * numOfSharesThatTheSecretsWereSplitInto|threshold|numOfSharesInThisContainer|numBytesInShare1|bytesOfShare1|.. .|numBytesInShareN|bytesOfShareN
	 */
	@Override
	public byte[] serialize() {

		byte[] mudulusBytes = getModulus().toByteArray();

		ByteBuffer bb = ByteBuffer.allocate(calculateLength());
		bb.putInt(mudulusBytes.length);
		bb.putInt(numberOfParts);
		bb.putInt(threshold);
		bb.putInt(shares.size());

		for (byte[] share : shares) {
			bb.putInt(share.length);
			bb.put(share);
		}
		bb.put(mudulusBytes);

		return bb.array();
	}

	/**
	 * Check if the given {@link MultipleSharesContainer} is compatible with this.
	 * <p>
	 * Note: the check that is performed here is very rudimentary, all that is checked is that the number of shares in this container is equal to the
	 * number of shares in the other container.
	 *
	 * @param other the MultipleSharesContainer to check compatibility with.
	 * @return true if the MultipleSharesContainers are compatible, false otherwise.
	 */
	public boolean isCompatible(final ShamirShare other) {

		if (!(other instanceof MultipleSharesContainer)) {
			return false;
		}
		MultipleSharesContainer o = (MultipleSharesContainer) other;

		if (shares.size() != o.getShares().size()) {
			return false;
		} else {
			return (numberOfParts == o.getNumberOfParts()) && (this.threshold == o.getThreshold());
		}
	}

	/**
	 * Perform basic health checks on the share. Checks if this container contains at least 1 share.
	 *
	 * @throws SharesException if the check does not pass.
	 */
	public void check() throws SharesException {

		if (shares.isEmpty()) {
			throw new SharesException("Container does not contain any shares.");
		}
	}

	/**
	 * Get the number of parts of this set of shares.
	 *
	 * @return Returns the numberOfParts.
	 */
	@Override
	public int getNumberOfParts() {
		return numberOfParts;
	}

	/**
	 * Get the threshold of this set of shares.
	 *
	 * @return Returns the threshold.
	 */
	@Override
	public int getThreshold() {
		return threshold;
	}

	/**
	 * Get the modulus of the shares in this container (all the shares must have the same modulus).
	 *
	 * @return the modulus.
	 */
	@Override
	public BigInteger getModulus() {

		byte[] share1AsByteArray = this.shares.get(0).clone();

		try {
			return thresholdSecretSharingServiceAPI.deserialize(share1AsByteArray).getModulus();
		} catch (GeneralCryptoLibException e) {
			throw new ConfigSharesException("Error trying to obtain the modulus of the shares in this container.", e);
		}
	}

	/**
	 * Override of method in parent class, however this method will throw an exception if called.
	 */
	@Override
	public List<Point> getPoints() {

		throw new UnsupportedOperationException(
				"There is no single point associated with a MultipleSharesContainer (because it can include multiple shares, which all "
						+ "have their own points).");
	}

	@Override
	public boolean equals(final Object other) {
		return EqualsBuilder.reflectionEquals(this, other, true);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, true);
	}

	private void basicInputValidations(byte[] serialized) throws SharesException {

		if (serialized == null || serialized.length == 0) {
			throw new SharesException("The receive serialized object was not an initialized non-empty array.");
		}
	}

	private int calculateLength() {

		int total = MSC_INT_BYTE_LENGTH + MSC_INT_BYTE_LENGTH + MSC_INT_BYTE_LENGTH + MSC_INT_BYTE_LENGTH;

		for (byte[] share : shares) {
			total += MSC_INT_BYTE_LENGTH;
			total += share.length;
		}
		total += getModulus().toByteArray().length;
		return total;
	}

	private void initSecretSharingService() {
		this.thresholdSecretSharingServiceAPI = new ThresholdSecretSharingService();
	}

}
