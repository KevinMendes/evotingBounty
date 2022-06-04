/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.post.it.evoting.cryptolib.api.stores.StoresServiceAPI;
import ch.post.it.evoting.cryptolib.stores.service.PollingStoresServiceFactory;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;

@Configuration
public class ApplicationConfig {

	@Bean
	public ElGamalService elGamalService() {
		return new ElGamalService();
	}

	@Bean
	public StoresServiceAPI storesService() {
		return new PollingStoresServiceFactory().create();
	}

	@Bean
	public RandomService randomservice() {
		return new RandomService();
	}
}
