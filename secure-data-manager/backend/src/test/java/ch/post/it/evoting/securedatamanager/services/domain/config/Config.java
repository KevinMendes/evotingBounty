/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.config;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.commons.PrefixPathResolver;
import ch.post.it.evoting.securedatamanager.services.application.service.HashService;
import ch.post.it.evoting.securedatamanager.services.application.service.KeyStoreService;
import ch.post.it.evoting.securedatamanager.services.application.service.KeyStoreServiceForTesting;
import ch.post.it.evoting.securedatamanager.services.domain.service.utils.PublicKeyLoader;
import ch.post.it.evoting.securedatamanager.services.domain.service.utils.SystemTenantPublicKeyLoader;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseManager;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseManagerFactory;

/**
 * MVC Configuration
 */
@Configuration
@ComponentScan(basePackages = { "ch.post.it.evoting.securedatamanager.services.infrastructure" })
@PropertySource("classpath:config/application.properties")
@Profile("test")
public class Config {
	@Value("${user.home}")
	private String prefix;

	@Value("${database.type}")
	private String databaseType;

	@Value("${database.path}")
	private String databasePath;

	@Value("${database.password.location}")
	private String passwordFile;

	@Autowired
	private DatabaseManagerFactory databaseManagerFactory;

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertiesResolver() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public DatabaseManagerFactory createDatabaseManagerFactory() {
		return new DatabaseManagerFactory(databaseType, databasePath, passwordFile);
	}

	@Bean
	public PathResolver getPrefixPathResolver() {
		return new PrefixPathResolver(prefix);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public SystemTenantPublicKeyLoader getSystemTenantPublicKeyLoader() {
		return new SystemTenantPublicKeyLoader();
	}

	@Bean
	public PublicKeyLoader getPublicKeyLoader() {
		return new PublicKeyLoader();
	}

	@Bean
	AsymmetricServiceAPI asymmetricServiceAPI() {
		return new AsymmetricService();
	}

	@Bean(initMethod = "createDatabase")
	DatabaseManager databaseManager() {
		final String name = UUID.randomUUID().toString();
		return databaseManagerFactory.newDatabaseManager(name);
	}

	@Bean
	KeyStoreService getKeyStoreService() {
		return new KeyStoreServiceForTesting();
	}

	@Bean
	HashService getHashService() {
		return new HashService();
	}

	@Bean
	ObjectReader jsonReader() {
		return new ObjectMapper().reader();
	}
}
