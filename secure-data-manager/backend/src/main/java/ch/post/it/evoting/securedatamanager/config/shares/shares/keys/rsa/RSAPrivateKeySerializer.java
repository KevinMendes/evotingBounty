/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.shares.keys.rsa;

import java.math.BigInteger;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;

import ch.post.it.evoting.securedatamanager.config.shares.shares.keys.PrivateKeySerializer;

public final class RSAPrivateKeySerializer implements PrivateKeySerializer {

	@Override
	public byte[] serialize(final PrivateKey privateKey) {
		if (!(privateKey instanceof RSAPrivateKey)) {
			throw new IllegalArgumentException("The private key must be an RSA private key");
		}

		return ((RSAPrivateKey) privateKey).getPrivateExponent().toByteArray();
	}

	@Override
	public PrivateKey reconstruct(final byte[] recovered, final PublicKey publicKey) throws KeyException {
		if (!(publicKey instanceof RSAPublicKey)) {
			throw new IllegalArgumentException("The public key must be an RSA private key");
		}

		final RSAPrivateKeySpec spec = new RSAPrivateKeySpec(((RSAPublicKey) publicKey).getModulus(), new BigInteger(recovered));
		final KeyFactory factory;
		try {
			factory = KeyFactory.getInstance("RSA");
			return factory.generatePrivate(spec);

		} catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new KeyException("An error occured while generating the private key from the byte[]", e);
		}
	}
}
