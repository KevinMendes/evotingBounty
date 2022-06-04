/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.returncodes;

/**
 * The class representing the vote cast message.
 */
public class ShortVoteCastReturnCodeAndComputeResults extends ComputeResults {

	// the calculated vote cast code
	private String shortVoteCastReturnCode;

	public String getShortVoteCastReturnCode() {
		return shortVoteCastReturnCode;
	}

	public void setShortVoteCastReturnCode(String shortVoteCastReturnCode) {
		this.shortVoteCastReturnCode = shortVoteCastReturnCode;
	}
}
