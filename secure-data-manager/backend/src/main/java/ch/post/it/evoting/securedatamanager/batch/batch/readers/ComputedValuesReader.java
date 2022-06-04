/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.readers;

import java.util.concurrent.BlockingQueue;

import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import ch.post.it.evoting.securedatamanager.commons.domain.VcIdCombinedReturnCodesGenerationValues;

@Component
@JobScope
public class ComputedValuesReader implements ItemReader<VcIdCombinedReturnCodesGenerationValues> {

	private final BlockingQueue<VcIdCombinedReturnCodesGenerationValues> computedValuesQueue;

	public ComputedValuesReader(final BlockingQueue<VcIdCombinedReturnCodesGenerationValues> computedValuesQueue) {
		this.computedValuesQueue = computedValuesQueue;
	}

	@Override
	public VcIdCombinedReturnCodesGenerationValues read() throws InterruptedException {
		final VcIdCombinedReturnCodesGenerationValues computedValues = computedValuesQueue.take();
		if (!computedValues.isPoisonPill()) {
			return computedValues;
		} else {
			// Add again the poison pill to the queue to ensure all remaining
			// threads will receive it
			computedValuesQueue.add(VcIdCombinedReturnCodesGenerationValues.poisonPill());
			return null;
		}
	}

}
