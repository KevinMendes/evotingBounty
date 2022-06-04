/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.writers;

import java.nio.file.Path;
import java.util.Base64;

import ch.post.it.evoting.securedatamanager.batch.batch.GeneratedVotingCardOutput;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ExtendedAuthInformation;

public class ExtendedAuthenticationWriter extends MultiFileDataWriter<GeneratedVotingCardOutput> {

	public ExtendedAuthenticationWriter(final Path basePath, final int maxNumCredentialsPerFile) {
		super(basePath, maxNumCredentialsPerFile);
	}

	@Override
	protected String getLine(GeneratedVotingCardOutput item) {
		final String electionEventId = item.getElectionEventId();
		final ExtendedAuthInformation extendedAuthInformation = item.getExtendedAuthInformation();
		final String credentialId = item.getCredentialId();

		final String authId = extendedAuthInformation.getAuthenticationId().getDerivedKeyInEx();
		// get encoded or empty if not present
		final String extraParam = extendedAuthInformation.getExtendedAuthChallenge().map(extendedAuthChallenge -> Base64.getEncoder()
				.encodeToString(extendedAuthChallenge.getDerivedChallenges().getDerivedKey().getEncoded())).orElse(Constants.EMPTY);

		// get salt or empty if not present
		String salt = extendedAuthInformation.getExtendedAuthChallenge()
				.map(extendedAuthChallenge -> Base64.getEncoder().encodeToString(extendedAuthChallenge.getSalt())).orElse(Constants.EMPTY);
		final String encryptedSVK = extendedAuthInformation.getEncryptedSVK();
		return String.format("%s,%s,%s,%s,%s,%s", authId, extraParam, encryptedSVK, electionEventId, salt, credentialId);
	}
}
