/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.writers;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import ch.post.it.evoting.securedatamanager.commons.domain.VcIdCombinedReturnCodesGenerationValues;

@Component
public class ComputedValuesOutputWriter implements ItemWriter<List<VcIdCombinedReturnCodesGenerationValues>> {

	private final BlockingQueue<VcIdCombinedReturnCodesGenerationValues> queue;

	public ComputedValuesOutputWriter(final BlockingQueue<VcIdCombinedReturnCodesGenerationValues> queue) {
		this.queue = queue;
	}

	@Override
	public void write(final List<? extends List<VcIdCombinedReturnCodesGenerationValues>> itemsList) throws Exception {
		for (final List<VcIdCombinedReturnCodesGenerationValues> items : itemsList) {
			for (final VcIdCombinedReturnCodesGenerationValues item : items) {
				queue.put(item);
			}
		}
	}

}
