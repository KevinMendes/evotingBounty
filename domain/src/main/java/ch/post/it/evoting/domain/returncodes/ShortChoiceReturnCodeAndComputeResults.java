/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.returncodes;

public class ShortChoiceReturnCodeAndComputeResults extends ComputeResults {

	private String shortChoiceReturnCodes;

	public String getShortChoiceReturnCodes() {
		return shortChoiceReturnCodes;
	}

	public void setShortChoiceReturnCodes(final String shortChoiceReturnCodes) {
		this.shortChoiceReturnCodes = shortChoiceReturnCodes;
	}
}


