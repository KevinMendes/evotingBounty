/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ch.post.it.evoting.securedatamanager.commons.Constants;

@Component
public class PropertiesBasedJobSelectionStrategy {

	private static final String PREFIX = Constants.VOTING_CARD_SET_GENERATION;
	private final String qualifier;

	public PropertiesBasedJobSelectionStrategy(
			@Value("${spring.batch.jobs.qualifier:}")
			final String qualifier) {
		this.qualifier = qualifier;
	}

	public String select() {
		return String.format("%s-%s", PREFIX, qualifier);
	}
}