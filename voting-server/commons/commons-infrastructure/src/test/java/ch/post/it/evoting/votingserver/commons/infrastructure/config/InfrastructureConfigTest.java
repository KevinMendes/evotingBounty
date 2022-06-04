/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.commons.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
class InfrastructureConfigTest {

	@SystemStub
	private static EnvironmentVariables environmentVariables;

	@BeforeAll
	static void setup() {
		environmentVariables.set("VAR_ONE", "123");
	}

	@Test
	@DisplayName("Given the correct setting of an environment variable then its value should be returned")
	void shouldFindEnvironmentVariable() {
		final String var_one = InfrastructureConfig.getEnvWithDefaultOption("VAR_ONE", "123");
		assertEquals(Integer.valueOf(123), Integer.valueOf(var_one));
	}

	@Test
	@DisplayName("Given no environment variable then the default value should be returned")
	void shouldReturnDefault() {
		final String var_one = InfrastructureConfig.getEnvWithDefaultOption("VAR_TWO", "456");
		assertEquals(Integer.valueOf(456), Integer.valueOf(var_one));
	}
}
