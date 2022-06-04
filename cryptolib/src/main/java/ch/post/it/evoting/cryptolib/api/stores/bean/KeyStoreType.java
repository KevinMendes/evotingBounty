/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.stores.bean;

/**
 * Enum which defines the key store types.
 */
public enum KeyStoreType {
	PKCS12("PKCS12"),
	JCEKS("JCEKS");

	private final String keyStoreTypeName;

	KeyStoreType(final String keyStoreTypeName) {
		this.keyStoreTypeName = keyStoreTypeName;
	}

	public String getKeyStoreTypeName() {
		return keyStoreTypeName;
	}
}
