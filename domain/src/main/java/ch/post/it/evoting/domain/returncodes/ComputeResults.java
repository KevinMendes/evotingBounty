/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.returncodes;

/**
 * Class that contains the computation and the decryption results for generating the Choice Return Codes and the Vote Cast Return Code. For the Vote
 * Cast Return Code, the decryption results are null.
 */
public class ComputeResults {

	private String computationResults;

	private String decryptionResults;

	public String getComputationResults() {
		return computationResults;
	}

	public void setComputationResults(String computationResults) {
		this.computationResults = computationResults;
	}

	public String getDecryptionResults() {
		return decryptionResults;
	}

	public void setDecryptionResults(String decryptionResults) {
		this.decryptionResults = decryptionResults;
	}

}


