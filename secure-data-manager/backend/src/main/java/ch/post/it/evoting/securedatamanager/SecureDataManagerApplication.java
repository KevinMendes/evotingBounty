/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SecureDataManagerApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecureDataManagerApplication.class);

	public static void main(final String[] args) {
		LOGGER.info("-------------------- Starting Secure Data Manager... --------------------");

		Security.addProvider(new BouncyCastleProvider());

		final ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(SecureDataManagerApplication.class).run(args);

		applicationContext.registerShutdownHook();

		LOGGER.info("-------------------- Secure Data Manager successfully started. -------------------- ");
	}

}
