/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.post.it.evoting.votingserver.commons.messaging.Topic;

/**
 * Tests of {@link TopicsConfig}.
 */
public class TopicsConfigTest {

	@Test
	public void test() {
		final String json = "{\"or-ha\":\"or-ha-fanout\"}";
		final TopicsConfig.Parser parser = new TopicsConfig.Parser();
		parser.parse(json);
		assertEquals(new Topic("or-ha-fanout"), parser.ha);
	}
}
