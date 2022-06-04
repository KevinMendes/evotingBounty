/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox;

/**
 * Class for representing key-value pairs with additional information for vote.
 */
public class AdditionalData {
	// the key
	private String key;

	// the value
	private String value;

	/**
	 * Constructor.
	 *
	 * @param key   - the key.
	 * @param value - the value.
	 */
	public AdditionalData(final String key, final String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Returns the current value of the field key.
	 *
	 * @return Returns the key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Sets the value of the field key.
	 *
	 * @param key The key to set.
	 */
	public void setKey(final String key) {
		this.key = key;
	}

	/**
	 * Returns the current value of the field value.
	 *
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of the field value.
	 *
	 * @param value The value to set.
	 */
	public void setValue(final String value) {
		this.value = value;
	}
}
