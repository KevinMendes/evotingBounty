/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.writers;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.springframework.batch.item.ItemWriter;

import ch.post.it.evoting.securedatamanager.batch.batch.GeneratedVotingCardOutput;

public class GeneratedVotingCardOutputWriter implements ItemWriter<GeneratedVotingCardOutput> {

	private final BlockingQueue<GeneratedVotingCardOutput> queue;

	public GeneratedVotingCardOutputWriter(final BlockingQueue<GeneratedVotingCardOutput> queue) {
		this.queue = queue;
	}

	@Override
	public void write(final List<? extends GeneratedVotingCardOutput> items) throws Exception {
		for (final GeneratedVotingCardOutput item : items) {
			queue.put(item);
		}
	}
}
