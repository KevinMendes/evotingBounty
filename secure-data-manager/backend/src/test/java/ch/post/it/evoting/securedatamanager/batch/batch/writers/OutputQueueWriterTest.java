/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.writers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.securedatamanager.batch.batch.GeneratedVotingCardOutput;

class OutputQueueWriterTest {

	@Test
	void writeIntoQueueAllItems() throws Exception {

		// given
		BlockingQueue<GeneratedVotingCardOutput> queue = new LinkedBlockingQueue<>();
		List<GeneratedVotingCardOutput> items = new ArrayList<>();
		items.add(createOutput());
		items.add(createOutput());
		items.add(createOutput());
		GeneratedVotingCardOutputWriter generatedVotingCardOutputWriter = new GeneratedVotingCardOutputWriter(queue);

		// when
		generatedVotingCardOutputWriter.write(items);

		// then
		assertEquals(items.size(), queue.size());
	}

	private GeneratedVotingCardOutput createOutput() {
		return GeneratedVotingCardOutput.success(null, null, null, null, null, null, null, null, null, null, null, null, null);
	}
}
