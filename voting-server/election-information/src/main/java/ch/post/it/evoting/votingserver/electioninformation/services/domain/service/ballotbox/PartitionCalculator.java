/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.ballotbox;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper to fit a collection into partitions of a suggested size. The minimum size for a partition is the recommended size, if enough elements are
 * present; the maximum is (minimumSize * 2) - 1.
 */
class PartitionCalculator {

	private final int minimumRecommendedPartitionSize;

	PartitionCalculator(final int minimumRecommendedPartitionSize) {
		if (minimumRecommendedPartitionSize < 1) {
			throw new IllegalArgumentException("The minimum partition size must be a positive integer");
		}

		this.minimumRecommendedPartitionSize = minimumRecommendedPartitionSize;
	}

	/**
	 * Calculates how many partitions are required to fit all data.
	 *
	 * @param totalItems the total items to partition
	 * @return a map with each partition's size
	 */
	private int getPartitionCount(final int totalItems) {
		// Calculate the partition count.
		int count = totalItems / minimumRecommendedPartitionSize;
		if (count == 0) {
			count = 1;
		}
		return count;
	}

	Map<Integer, PartitionDetails> getPartitionDetails(final int totalItems) {
		// Calculate the partition count.
		final int partitionCount = getPartitionCount(totalItems);

		// Create a map to hold the partitions.
		final Map<Integer, PartitionDetails> partitionDetails = new ConcurrentHashMap<>(partitionCount);

		// Start filling up the partitions, if there are items.
		if (totalItems > 0) {
			// Fill up all partitions up to the recommended minimum size.
			for (int partitionIndex = 0; partitionIndex < partitionCount; partitionIndex++) {
				partitionDetails
						.put(partitionIndex, new PartitionDetails(partitionIndex * minimumRecommendedPartitionSize, minimumRecommendedPartitionSize));
			}
			// Find out any extra items that should be added to the last partition.
			final int remainingItems = totalItems - (partitionCount * minimumRecommendedPartitionSize);
			if (remainingItems > 0) {
				final PartitionDetails lastPartitionDetails = partitionDetails.get(partitionCount - 1);
				// Add the remainder of the collection to the last page.
				partitionDetails.put(partitionCount - 1,
						new PartitionDetails(lastPartitionDetails.getOffset(), lastPartitionDetails.getSize() + remainingItems));
			}
		} else {
			// No elements -- still, an empty partition is required for the empty payload to
			// travel through the nodes and generates the expected outputs.
			partitionDetails.put(0, new PartitionDetails(0, 0));
		}

		return partitionDetails;
	}

	static class PartitionDetails {
		private final int offset;

		private final int size;

		PartitionDetails(final int offset, final int size) {
			this.offset = offset;
			this.size = size;
		}

		public int getOffset() {
			return offset;
		}

		public int getSize() {
			return size;
		}
	}
}
