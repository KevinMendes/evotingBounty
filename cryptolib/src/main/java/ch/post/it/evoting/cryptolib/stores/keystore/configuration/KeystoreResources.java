/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import java.io.InputStream;
import java.nio.file.Path;

interface KeystoreResources {

	Path getResourcePath(String resource);

	InputStream getResourceAsStream(String resourcePath);

	KeystoreProperties getKeystoreProperties();

}
