/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.cipher.configuration;

/**
 * Enum which defines the encryption operating modes.
 */
public enum EncryptionOperatingMode {
	GCM("GCM");

	private final String mode;

	EncryptionOperatingMode(final String mode) {
		this.mode = mode;
	}

	@Override
	public String toString() {
		return mode;
	}
}
