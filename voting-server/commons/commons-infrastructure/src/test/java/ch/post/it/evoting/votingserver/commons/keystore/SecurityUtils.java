/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.keystore;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

abstract class SecurityUtils {

	private SecurityUtils() {
	}

	static String privateKeyToString(final PrivateKey privateKey) {
		final RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
		return ("Modulus: " + rsaPrivateKey.getModulus() + "\nExponent: " + rsaPrivateKey.getPrivateExponent() + "\n");
	}

	static String certificateToString(final Certificate certificate) {
		final RSAPublicKey rsaPublicKey = (RSAPublicKey) certificate.getPublicKey();
		return ("Modulus: " + rsaPublicKey.getModulus() + "\nExponent: " + rsaPublicKey.getPublicExponent() + "\n");
	}

}
