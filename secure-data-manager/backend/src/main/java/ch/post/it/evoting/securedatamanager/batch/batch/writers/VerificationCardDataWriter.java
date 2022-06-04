/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.writers;

import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENVCD_ERROR_GENERATING_VERIFICATION_KEYSTORE;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENVCD_SUCCESS_GENERATING_VERIFICATION_CARD_KEYSTORE;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.securedatamanager.batch.batch.GeneratedVotingCardOutput;
import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.specific.GenerateVerificationCardDataException;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VerificationCardCredentialDataPack;

public class VerificationCardDataWriter extends MultiFileDataWriter<GeneratedVotingCardOutput> {

	private static final Logger LOGGER = LoggerFactory.getLogger(VerificationCardDataWriter.class);

	public VerificationCardDataWriter(final Path path, final int maxNumCredentialsPerFile) {
		super(path, maxNumCredentialsPerFile);
	}

	@Override
	protected String getLine(final GeneratedVotingCardOutput item) {
		final String verificationCardId = item.getVerificationCardId();
		final String verificationCardSetId = item.getVerificationCardSetId();
		final String electionEventId = item.getElectionEventId();
		final VerificationCardCredentialDataPack verificationCardCredentialDataPack = item.getVerificationCardCredentialDataPack();
		final String verificationCardSerializedKeyStoreB64 = verificationCardCredentialDataPack.getSerializedKeyStore();

		confirmAndLogKeystore(verificationCardSerializedKeyStoreB64, verificationCardId, verificationCardSetId, electionEventId);

		return String.format("%s,%s,%s,%s", verificationCardId, verificationCardSerializedKeyStoreB64, electionEventId, verificationCardSetId);
	}

	private void confirmAndLogKeystore(final String verificationCardSerializedKeyStoreB64, final String verificationCardId,
			final String verificationCardSetId, final String electionEventId) {

		if (verificationCardSerializedKeyStoreB64.length() > 0) {
			LOGGER.info("{}. [electionEventId: {}, verificationCardSetId: {}, verificationCardId: {}]",
					GENVCD_SUCCESS_GENERATING_VERIFICATION_CARD_KEYSTORE.getInfo(), electionEventId, verificationCardSetId, verificationCardId);

		} else {
			LOGGER.error("{}. [electionEventId: {}, verificationCardSetId: {}, verificationCardId: {}]",
					GENVCD_ERROR_GENERATING_VERIFICATION_KEYSTORE.getInfo(), electionEventId, verificationCardSetId, verificationCardId);
			throw new GenerateVerificationCardDataException("Error - the keyStore that is being written to file is invalid");
		}
	}
}
