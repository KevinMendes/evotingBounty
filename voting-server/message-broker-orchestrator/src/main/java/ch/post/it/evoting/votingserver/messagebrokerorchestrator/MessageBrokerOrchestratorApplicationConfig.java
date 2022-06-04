/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;

@Configuration
public class MessageBrokerOrchestratorApplicationConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageBrokerOrchestratorApplicationConfig.class);

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		objectMapper.registerModule(new JavaTimeModule());
		return objectMapper;
	}

	@Bean
	public Cache<String, CompletableFuture<String>> getInFlightRequestCache(@Value("${orchestrator.request.cache.timeout.seconds}") int timeOut) {
		return Caffeine.newBuilder()
				.expireAfterWrite(timeOut, TimeUnit.SECONDS)
				.removalListener((key, value, cause) -> {
					if(cause.wasEvicted()) {
						LOGGER.debug("In flight request evicted from cache. [correlationId : {}]", key);
					}
				}).build();
	}
}
