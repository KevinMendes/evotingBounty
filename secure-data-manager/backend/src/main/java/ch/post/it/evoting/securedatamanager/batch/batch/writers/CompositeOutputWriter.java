/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.writers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.support.CompositeItemWriter;

import ch.post.it.evoting.securedatamanager.batch.batch.GeneratedVotingCardOutput;

/**
 * The purpose of this class is to filter out the outputs that are marked as error, before passing the valid ones to the 'delegate' writers (that do
 * the real writing)
 */
public class CompositeOutputWriter extends CompositeItemWriter<GeneratedVotingCardOutput> {

	@Override
	public void write(final List<? extends GeneratedVotingCardOutput> items) throws Exception {

		// filter outputs that are "error'd". only allow correct outputs to be written by the 'real'
		// file writers
		final List<GeneratedVotingCardOutput> outputs = removeItemsInError(items);
		super.write(outputs);
	}

	private List<GeneratedVotingCardOutput> removeItemsInError(final List<? extends GeneratedVotingCardOutput> items) {
		final List<GeneratedVotingCardOutput> validItems = new ArrayList<>(items.size());
		for (final GeneratedVotingCardOutput item : items) {
			if (!item.isError()) {
				validItems.add(item);
			}
		}
		return validItems;
	}
}
