/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.shares.keys;

import java.security.KeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface PrivateKeySerializer {

	byte[] serialize(PrivateKey privateKey);

	PrivateKey reconstruct(byte[] recovered, PublicKey publicKey) throws KeyException;
}
