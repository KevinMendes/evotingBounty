/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.ws;

import java.security.Security;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * This class define the application path for initializing the rest engine.
 */
@ApplicationPath("")
public class RestApplication extends Application {

	/**
	 * Constructor for initialization of rest engine and security provider.
	 */
	public RestApplication() {
		Security.addProvider(new BouncyCastleProvider());
	}
}
