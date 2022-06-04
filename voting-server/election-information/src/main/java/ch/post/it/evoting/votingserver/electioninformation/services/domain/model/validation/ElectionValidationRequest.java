/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation;

/**
 * Class used for validation purposes
 */
public class ElectionValidationRequest {

	private final String tenantId;

	private final String electionEventId;

	private final String ballotBoxId;

	private final boolean validatedWithGracePeriod;

	private ElectionValidationRequest(final String tenantId, final String electionEventId, final String ballotBoxId,
			final boolean validatedWithGracePeriod) {
		this.tenantId = tenantId;
		this.electionEventId = electionEventId;
		this.ballotBoxId = ballotBoxId;
		this.validatedWithGracePeriod = validatedWithGracePeriod;
	}

	public static ElectionValidationRequest create(final String tenantId, final String electionEventId, final String ballotBoxId,
			final boolean validateWithGracePeriod) {
		return new ElectionValidationRequest(tenantId, electionEventId, ballotBoxId, validateWithGracePeriod);
	}

	public String getTenantId() {
		return tenantId;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public boolean isValidatedWithGracePeriod() {
		return validatedWithGracePeriod;
	}
}
