/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.keystore;

import ch.post.it.evoting.cryptolib.stores.keystore.configuration.AbstractKeystoreReader;

public class TestKeystoreReader extends AbstractKeystoreReader {

	private TestKeystoreReader() {
		super(TestKeystoreResources.getInstance());
	}

	public static TestKeystoreReader getInstance() {
		return new TestKeystoreReader();
	}

}
