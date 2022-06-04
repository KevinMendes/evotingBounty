/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.ballotbox;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.ballotbox.PartitionCalculator.PartitionDetails;

public class PartitionCalculatorTest {

	@Test
	public void test0Items() {
		testCalculator(0, 1, 1, 0);
	}

	@Test
	public void testAOneItemPartition() {
		// Test a 1-item partition.
		testCalculator(1, 1, 1, 1);
	}

	@Test
	public void testMRPSCollection() {
		// Test the minimum recommended partition size, should end up in one partition.
		testCalculator(10, 10, 1, 10);
	}

	@Test
	public void testOneMoreThanMRPS() {
		// Test 1 more than the minimum recommended size, should end up in one partition.
		testCalculator(11, 10, 1, 11);
	}

	@Test
	public void testOneLessThanTwiceTheMRPS() {
		// Test 1 less than double the minimum recommended size, should still end up in one partition.
		testCalculator(19, 10, 1, 19);
	}

	@Test
	public void testTwiceTheMRPS() {
		// Test twice the partition size, should end up in two partitions.
		testCalculator(2, 1, 2, 1);
		testCalculator(20, 10, 2, 10);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidData() {
		testCalculator(0, 0, 0, 0);
	}

	private void testCalculator(final int totalItems, final int minimumRecommendedPartitionSize, final int expectedPartitions, final int expectedLastPartitionSize) {
		final PartitionCalculator sut = new PartitionCalculator(minimumRecommendedPartitionSize);
		final Map<Integer, PartitionDetails> partitionDetails = sut.getPartitionDetails(totalItems);
		assertEquals(expectedPartitions, partitionDetails.size());
		if (!partitionDetails.isEmpty()) {
			assertEquals(expectedLastPartitionSize, partitionDetails.get(partitionDetails.size() - 1).getSize());
		}
	}
}
