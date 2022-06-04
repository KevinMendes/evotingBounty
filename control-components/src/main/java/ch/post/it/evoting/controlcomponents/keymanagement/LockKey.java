/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

public enum LockKey {

	NODE_KEY("NODE_KEY");

	private final String key;

	LockKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
