/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.returncodes;

import ch.post.it.evoting.domain.election.model.vote.Vote;

public class VoteAndComputeResults {

	private Vote vote;

	private String credentialInfoCertificates;

	private ComputeResults computeResults;

	public Vote getVote() {
		return vote;
	}

	public void setVote(Vote vote) {
		this.vote = vote;
	}

	public ComputeResults getComputeResults() {
		return computeResults;
	}

	public void setComputeResults(ComputeResults computeResults) {
		this.computeResults = computeResults;
	}

	public String getCredentialInfoCertificates() {
		return credentialInfoCertificates;
	}

	public void setCredentialInfoCertificates(String credentialInfoCertificates) {
		this.credentialInfoCertificates = credentialInfoCertificates;
	}
}


