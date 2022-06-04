/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import ch.post.it.evoting.controlcomponents.keymanagement.NodeKeysActivator;

@Component
public class ControlComponentsApplicationBootstrap {

	public static final String MIXING_CONTAINER_PREFIX = "mixing";
	public static final String CHOICE_CODES_CONTAINER_PREFIX = "choicecodes";
	public static final String RABBITMQ_EXCHANGE = "evoting-exchange";

	private static final Logger LOGGER = LoggerFactory.getLogger(ControlComponentsApplicationBootstrap.class);
	private static final String KEY_ALIAS = "ccncakey";

	private final NodeKeysActivator nodeKeysActivator;
	private final RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;
	private final String keyStoreFile;
	private final String passwordFile;
	@Value("${mixing.rabbitmq.listeners.enabled:true}")
	boolean isMixingEnabled;

	public ControlComponentsApplicationBootstrap(final NodeKeysActivator nodeKeysActivator,
			final RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry,
			@Value("${keys.keystore.dir}/${keystore}")
			final String keyStoreFile,
			@Value("${keys.keystore.dir}/${keystore.password.file}")
			final String passwordFile) {
		this.nodeKeysActivator = nodeKeysActivator;
		this.rabbitListenerEndpointRegistry = rabbitListenerEndpointRegistry;
		this.keyStoreFile = keyStoreFile;
		this.passwordFile = passwordFile;
	}

	@Bean
	boolean isApplicationBootstrapEnabled(
			@Value("${application.bootstrap.enabled:true}")
			final String enabled) {
		LOGGER.info("Application bootstrapped enabled {}", enabled);
		return Boolean.parseBoolean(enabled);
	}

	@EventListener(value = ApplicationReadyEvent.class, condition = "@isApplicationBootstrapEnabled")
	public void bootstrap() throws IOException, KeyManagementException, InterruptedException, TimeoutException {
		activateNodeKeys();
		startRabbitMQListeningContainers();
	}

	private void startRabbitMQListeningContainers() {

		final List<String> mixingContainers = getContainerNames(MIXING_CONTAINER_PREFIX);
		final List<String> choiceCodeContainers = getContainerNames(CHOICE_CODES_CONTAINER_PREFIX);

		if (isMixingEnabled) {
			startRabbitMQContainers(mixingContainers);
		}
		startRabbitMQContainers(choiceCodeContainers);
	}

	private List<String> getContainerNames(final String containerPrefix) {
		return rabbitListenerEndpointRegistry.getListenerContainerIds().stream()
				.filter(c -> c.contains(containerPrefix))
				.collect(Collectors.toList());
	}

	private void activateNodeKeys() throws IOException, KeyManagementException, InterruptedException, TimeoutException {
		final Path keyStorePath = getFilePath(keyStoreFile);
		final Path passwordPath = getFilePath(passwordFile);
		nodeKeysActivator.activateNodeKeys(keyStorePath, KEY_ALIAS, passwordPath);
	}

	private void startRabbitMQContainers(final List<String> rabbitMQContainerNames) {
		rabbitMQContainerNames.stream()
				.map(rabbitListenerEndpointRegistry::getListenerContainer)
				.forEach(Lifecycle::start);
	}

	private Path getFilePath(final String file) throws IOException {
		final Resource resource = new DefaultResourceLoader().getResource(file);
		return Paths.get(resource.getFile().getPath());
	}
}
