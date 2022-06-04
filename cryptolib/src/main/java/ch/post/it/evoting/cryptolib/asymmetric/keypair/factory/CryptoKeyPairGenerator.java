/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.keypair.factory;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

public final class CryptoKeyPairGenerator {

	private final KeyPairGenerator keyPairGenerator;

	/**
	 * Constructor which stores a {@link KeyPairGenerator}.
	 *
	 * <p>Note: depending on how the {@link KeyPairGenerator} has been configured, this class will
	 * generate the corresponding types of {@link KeyPair}.
	 *
	 * @param keyPairGenerator the {@link KeyPairGenerator} that should be already initialized.
	 */
	CryptoKeyPairGenerator(final KeyPairGenerator keyPairGenerator) {
		this.keyPairGenerator = keyPairGenerator;
	}

	/**
	 * Generates a key pair.
	 *
	 * @return the generated key pair.
	 * @see KeyPairGenerator#genKeyPair()
	 */
	public KeyPair genKeyPair() {
		return keyPairGenerator.genKeyPair();
	}
}
