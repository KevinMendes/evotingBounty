/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.config;

import static java.text.MessageFormat.format;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import ch.post.it.evoting.votingserver.commons.messaging.Topic;

/**
 * Utility class which holds information about the topics used by the application.
 */
public final class TopicsConfig {
	/**
	 * Topic used for the results ready notification.
	 */
	public static final Topic HA_TOPIC;

	private static final String CC_TOPIC_NAMES_PROPERTY = "CC_TOPIC_NAMES";

	static {
		String json = System.getenv(CC_TOPIC_NAMES_PROPERTY);
		if (json == null) {
			throw new IllegalStateException(format("System property ''{0}'' is missing.", CC_TOPIC_NAMES_PROPERTY));
		}

		Parser parser = new Parser();
		parser.parse(json);

		HA_TOPIC = parser.ha;
	}

	private TopicsConfig() {
	}

	/**
	 * Topic names parser. For internal use only.
	 */
	static class Parser {
		private static final String HA_PROPERTY = "or-ha";

		/**
		 * Topic used for the results ready notification. For internal use only
		 */
		Topic ha;

		/**
		 * Parses a given JSON and populates the topic names. For internal use only.
		 *
		 * @param json the json.
		 */
		void parse(String json) {
			JsonObject object;
			try (JsonReader reader = Json.createReader(new StringReader(json))) {
				object = reader.readObject();
			}
			ha = new Topic(object.getString(HA_PROPERTY));
		}
	}
}
