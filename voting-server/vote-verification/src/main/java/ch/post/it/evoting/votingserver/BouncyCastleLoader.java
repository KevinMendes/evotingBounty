/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver;

import java.security.Security;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class BouncyCastleLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger(BouncyCastleLoader.class);

	@PostConstruct
	public void addBouncyCastleProvider() {
		Security.addProvider(new BouncyCastleProvider());
		LOGGER.info("Added BouncyCastle as a security provider.");
	}
}
