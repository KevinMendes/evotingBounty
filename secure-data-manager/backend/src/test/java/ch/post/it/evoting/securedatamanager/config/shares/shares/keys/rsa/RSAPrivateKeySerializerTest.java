/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.securedatamanager.config.shares.shares.keys.rsa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;

class RSAPrivateKeySerializerTest {

	private static RSAKeyPairGenerator rsaKeyPairGenerator;

	@BeforeAll
	static void beforeClass() {
		final AsymmetricServiceAPI asymmetricService = new AsymmetricService();
		rsaKeyPairGenerator = new RSAKeyPairGenerator(asymmetricService);
	}

	@RepeatedTest(10)
	void serializeAndReconstruct() throws Exception {

		final KeyPair keyPair = rsaKeyPairGenerator.generate();
		final PrivateKey privateKey = keyPair.getPrivate();
		final PublicKey publicKey = keyPair.getPublic();

		final RSAPrivateKeySerializer serializer = new RSAPrivateKeySerializer();
		final byte[] serializedKey = serializer.serialize(privateKey);

		final PrivateKey reconstructedKey = serializer.reconstruct(serializedKey, publicKey);

		assertEquals(((RSAPrivateKey) privateKey).getPrivateExponent(), ((RSAPrivateKey) reconstructedKey).getPrivateExponent());
	}
}
