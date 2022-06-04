/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch;

import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ExtendedAuthInformation;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VerificationCardCodesDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VerificationCardCredentialDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VotingCardCredentialDataPack;

public class GeneratedVotingCardOutput {

	private String votingCardId;
	private String votingCardSetId;
	private String ballotId;
	private String ballotBoxId;
	private String credentialId;
	private String electionEventId;
	private String verificationCardId;
	private String verificationCardSetId;
	private String startVotingKey;

	private VotingCardCredentialDataPack voterCredentialDataPack;
	private VerificationCardCredentialDataPack verificationCardCredentialDataPack;
	private VerificationCardCodesDataPack verificationCardCodesDataPack;
	private ExtendedAuthInformation extendedAuthInformation;
	private boolean failed;
	private boolean poisonPill;
	private Exception error;

	private GeneratedVotingCardOutput(final Exception error) {
		this.failed = true;
		this.error = error;
	}

	private GeneratedVotingCardOutput() {
		this.poisonPill = true;
	}

	private GeneratedVotingCardOutput(final String votingCardId, final String votingCardSetId, final String ballotId, final String ballotBoxId,
			final String credentialId, final String electionEventId, final String verificationCardId, final String verificationCardSetId,
			final String startVotingKey, final VotingCardCredentialDataPack voterCredentialDataPack,
			final VerificationCardCredentialDataPack verificationCardCredentialDataPack,
			final VerificationCardCodesDataPack verificationCardCodesDataPack, final ExtendedAuthInformation extendedAuthInformation) {
		this.failed = false;

		this.votingCardId = votingCardId;
		this.votingCardSetId = votingCardSetId;
		this.ballotId = ballotId;
		this.ballotBoxId = ballotBoxId;
		this.credentialId = credentialId;
		this.electionEventId = electionEventId;
		this.verificationCardId = verificationCardId;
		this.verificationCardSetId = verificationCardSetId;
		this.startVotingKey = startVotingKey;
		this.voterCredentialDataPack = voterCredentialDataPack;
		this.verificationCardCredentialDataPack = verificationCardCredentialDataPack;
		this.verificationCardCodesDataPack = verificationCardCodesDataPack;
		this.extendedAuthInformation = extendedAuthInformation;
	}

	public static GeneratedVotingCardOutput error(final Exception error) {
		return new GeneratedVotingCardOutput(error);
	}

	public static GeneratedVotingCardOutput success(final String votingCardId, final String votingCardSetId, final String ballotId,
			final String ballotBoxId, final String credentialId, final String electionEventId, final String verificationCardId,
			final String verificationCardSetId, final String startVotingKey, final VotingCardCredentialDataPack voterCredentialDataPack,
			final VerificationCardCredentialDataPack verificationCardCredentialDataPack,
			final VerificationCardCodesDataPack verificationCardCodesDataPack, final ExtendedAuthInformation extendedAuthInformation) {
		return new GeneratedVotingCardOutput(votingCardId, votingCardSetId, ballotId, ballotBoxId, credentialId, electionEventId, verificationCardId,
				verificationCardSetId, startVotingKey, voterCredentialDataPack, verificationCardCredentialDataPack, verificationCardCodesDataPack,
				extendedAuthInformation);
	}

	public static GeneratedVotingCardOutput poisonPill() {
		return new GeneratedVotingCardOutput();
	}

	public String getVotingCardId() {
		return votingCardId;
	}

	public String getVotingCardSetId() {
		return votingCardSetId;
	}

	public String getBallotId() {
		return ballotId;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public String getCredentialId() {
		return credentialId;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getVerificationCardId() {
		return verificationCardId;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public String getStartVotingKey() {
		return startVotingKey;
	}

	public VotingCardCredentialDataPack getVoterCredentialDataPack() {
		return voterCredentialDataPack;
	}

	public VerificationCardCredentialDataPack getVerificationCardCredentialDataPack() {
		return verificationCardCredentialDataPack;
	}

	public VerificationCardCodesDataPack getVerificationCardCodesDataPack() {
		return verificationCardCodesDataPack;
	}

	public ExtendedAuthInformation getExtendedAuthInformation() {
		return extendedAuthInformation;
	}

	public boolean isError() {
		return failed;
	}

	public Exception getError() {
		return error;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("GeneratedVotingCardOutput{");
		sb.append("votingCardId='").append(votingCardId).append('\'');
		sb.append(", votingCardSetId='").append(votingCardSetId).append('\'');
		sb.append(", ballotId='").append(ballotId).append('\'');
		sb.append(", ballotBoxId='").append(ballotBoxId).append('\'');
		sb.append(", credentialId='").append(credentialId).append('\'');
		sb.append(", electionEventId='").append(electionEventId).append('\'');
		sb.append(", verificationCardId='").append(verificationCardId).append('\'');
		sb.append(", verificationCardSetId='").append(verificationCardSetId).append('\'');
		sb.append(", startVotingKey='").append(startVotingKey).append('\'');
		sb.append(", voterCredentialDataPack=").append(voterCredentialDataPack);
		sb.append(", verificationCardCredentialDataPack=").append(verificationCardCredentialDataPack);
		sb.append(", verificationCardCodesDataPack=").append(verificationCardCodesDataPack);
		sb.append(", extendedAuthInformation=").append(extendedAuthInformation);
		return sb.toString();
	}

	public boolean isPoisonPill() {
		return poisonPill;
	}
}
