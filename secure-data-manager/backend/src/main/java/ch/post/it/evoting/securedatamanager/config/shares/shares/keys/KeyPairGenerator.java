/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.shares.keys;

import java.security.KeyException;
import java.security.KeyPair;

public interface KeyPairGenerator {

	KeyPair generate() throws KeyException;
}
