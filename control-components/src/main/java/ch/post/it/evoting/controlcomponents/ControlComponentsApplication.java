/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableCaching
@EntityScan("ch.post.it.evoting")
@EnableJpaRepositories("ch.post.it.evoting")
@SpringBootApplication(scanBasePackages = { "ch.post.it.evoting" })
public class ControlComponentsApplication {

	public static void main(final String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		SpringApplication.run(ControlComponentsApplication.class);
	}
}

