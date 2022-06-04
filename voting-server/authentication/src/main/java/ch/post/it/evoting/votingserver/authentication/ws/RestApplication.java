/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.ws;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * This class defines the application path for initializing the REST engine.
 */
@ApplicationPath("")
public class RestApplication extends Application {

	/**
	 * Constructor for initializing the REST engine and the security provider.
	 */
	public RestApplication() {
		// This constructor is intentionally left blank
	}
}
