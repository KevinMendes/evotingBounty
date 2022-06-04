/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Singleton class to deal with module configuration properties.
 */
public class InfrastructureConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(InfrastructureConfig.class);

	private static final InfrastructureConfig INSTANCE = new InfrastructureConfig();

	private final String systemReadTimeOut;

	private final String systemWriteTimeOut;

	private final String systemConnectionTimeOut;

	private InfrastructureConfig() {
		systemReadTimeOut = System.getenv("READ_TIME_OUT");
		systemWriteTimeOut = System.getenv("WRITE_TIME_OUT");
		systemConnectionTimeOut = System.getenv("CONNECTION_TIME_OUT");
	}

	public static InfrastructureConfig getInstance() {
		return INSTANCE;
	}

	public static String getEnvWithDefaultOption(String environmentalVariableName, String defaultValue) {
		String environmentalVariableValue = System.getenv(environmentalVariableName);
		return environmentalVariableValue == null ? defaultValue : environmentalVariableValue;
	}

	public long getSystemReadTimeOut() {
		return systemReadTimeOut != null ? Long.parseLong(systemReadTimeOut) : 60L;
	}

	public long getSystemWriteTimeOut() {
		return systemWriteTimeOut != null ? Long.parseLong(systemWriteTimeOut) : 60L;
	}

	public long getSystemConnectionTimeOut() {
		return systemConnectionTimeOut != null ? Long.parseLong(systemConnectionTimeOut) : 60L;
	}
}
