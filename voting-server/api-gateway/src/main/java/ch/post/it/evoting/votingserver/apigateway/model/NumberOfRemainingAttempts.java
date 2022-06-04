/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.model;

public class NumberOfRemainingAttempts {

	private Integer numberOfRemainingAttempts;

	public NumberOfRemainingAttempts(Integer numberOfRemainingAttempts) {
		super();
		this.numberOfRemainingAttempts = numberOfRemainingAttempts;
	}

	public NumberOfRemainingAttempts() {
		super();
	}

	public Integer getNumberOfRemainingAttempts() {
		return numberOfRemainingAttempts;
	}

	public void setNumberOfRemainingAttempts(Integer numberOfRemainingAttempts) {
		this.numberOfRemainingAttempts = numberOfRemainingAttempts;
	}

}
