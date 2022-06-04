/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model.generator;

/**
 * This bean holds all the information returned by a data generator.
 */
public class DataGeneratorResponse {

	/**
	 * If the data was generated successfully or not.
	 */
	private boolean successful = true;

	private String result;

	/**
	 * Returns the current value of the field successful.
	 *
	 * @return Returns the successful.
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * Sets the value of the field successful.
	 *
	 * @param successful The successful to set.
	 */
	public void setSuccessful(final boolean successful) {
		this.successful = successful;
	}

	public String getResult() {
		return result;
	}

	public void setResult(final String result) {
		this.result = result;
	}
}
