/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptoprimitives.math.RandomService;

class ExactlyOnceTaskTest {
	private static final RandomService randomService = new RandomService();
	private static final int RANDOM_SIZE = 32;

	private static Stream<Arguments> nullArgumentProvider() {
		final String correlationId = randomService.genRandomBase64String(RANDOM_SIZE);

		final String contextId = randomService.genRandomBase32String(RANDOM_SIZE);
		final String context = randomService.genRandomBase16String(RANDOM_SIZE);

		final Callable<byte[]> callable = () -> new byte[] { 0b0000001 };

		return Stream.of(
				Arguments.of(null, contextId, context, callable),
				Arguments.of(correlationId, null, context, callable),
				Arguments.of(correlationId, contextId, null, callable),
				Arguments.of(correlationId, contextId, context, null)
		);
	}

	@ParameterizedTest
	@MethodSource("nullArgumentProvider")
	@DisplayName("")
	void testBuildWithNullParametersThrowsNullPointerException(final String correlationId, final String contextId, final String context,
			final Callable<byte[]> callable) {
		final ExactlyOnceTask.Builder builder = new ExactlyOnceTask.Builder()
				.setCorrelationId(correlationId)
				.setContextId(contextId)
				.setContext(context)
				.setTask(callable);

		assertThrows(NullPointerException.class, builder::build);
	}
}