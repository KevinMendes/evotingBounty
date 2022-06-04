/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.rule;

/**
 * Information used in validation of ids.
 */
public class ValidationIdItem {

	private String id1;

	private String id2;

	private String errorMessage;

	/**
	 * @param id1          the first ID string to be compared.
	 * @param id2          the second ID string to be compared.
	 * @param errorMessage the error message to be displayed in case of the rule fails.
	 */
	public ValidationIdItem(final String id1, final String id2, final String errorMessage) {
		super();
		this.id1 = id1;
		this.id2 = id2;
		this.errorMessage = errorMessage;
	}

	/**
	 * Returns the current value of the field id1.
	 *
	 * @return Returns the id1.
	 */
	public String getId1() {
		return id1;
	}

	/**
	 * Sets the value of the field id1.
	 *
	 * @param id1 The id1 to set.
	 */
	public void setId1(final String id1) {
		this.id1 = id1;
	}

	/**
	 * Returns the current value of the field id2.
	 *
	 * @return Returns the id2.
	 */
	public String getId2() {
		return id2;
	}

	/**
	 * Sets the value of the field id2.
	 *
	 * @param id2 The id2 to set.
	 */
	public void setId2(final String id2) {
		this.id2 = id2;
	}

	/**
	 * Returns the current value of the field errorMessage.
	 *
	 * @return Returns the errorMessage.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Sets the value of the field errorMessage.
	 *
	 * @param errorMessage The errorMessage to set.
	 */
	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
