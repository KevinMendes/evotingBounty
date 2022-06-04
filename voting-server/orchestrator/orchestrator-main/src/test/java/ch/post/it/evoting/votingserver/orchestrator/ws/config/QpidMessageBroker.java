/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.ws.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.server.SystemLauncher;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QpidMessageBroker extends ExternalResource {

	public static final String CONFIG_FILE_PATH = "qpid-config.json";

	private static final Logger logger = LoggerFactory.getLogger(QpidMessageBroker.class);

	private final SystemLauncher systemLauncher;

	private final Map<String, Object> systemConfigAttributes = new HashMap<>();

	public QpidMessageBroker(String configFilePath) {
		systemConfigAttributes.put("type", "Memory");
		systemConfigAttributes.put("initialConfigurationLocation", configFilePath);
		systemConfigAttributes.put("startupLoggedToSystemOut", true);

		systemLauncher = new SystemLauncher();
	}

	@Override
	protected void before() throws Throwable {
		logger.info("Embedded AMQP message broker starting...");
		systemLauncher.startup(systemConfigAttributes);
		logger.info("Embedded AMQP message broker started");
	}

	@Override
	protected void after() {
		logger.info("Embedded AMQP message broker stopping...");
		systemLauncher.shutdown();
		logger.info("Embedded AMQP message broker running has been stopped");
	}
}
