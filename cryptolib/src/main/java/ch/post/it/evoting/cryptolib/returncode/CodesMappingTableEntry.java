/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.returncode;

public final class CodesMappingTableEntry {

	private final byte[] key;

	private final byte[] data;

	public CodesMappingTableEntry(final byte[] key, final byte[] data) {

		this.key = key;
		this.data = data;
	}

	public byte[] getKey() {
		return key;
	}

	public byte[] getData() {
		return data;
	}
}
