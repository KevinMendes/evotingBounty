/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.writers;

import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENCREDAT_ERROR_GENERATING_KEYSTORE;
import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENCREDAT_SUCCESS_KEYSTORE_GENERATED;

import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.securedatamanager.batch.batch.GeneratedVotingCardOutput;
import ch.post.it.evoting.securedatamanager.batch.batch.exceptions.GenerateVerificationCardCodesException;
import ch.post.it.evoting.securedatamanager.config.commons.datapacks.beans.SerializedCredentialDataPack;

public class CredentialDataWriter extends MultiFileDataWriter<GeneratedVotingCardOutput> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CredentialDataWriter.class);

	public CredentialDataWriter(final Path basePath, final int maxNumCredentialsPerFile) {
		super(basePath, maxNumCredentialsPerFile);
	}

	@Override
	protected String getLine(final GeneratedVotingCardOutput item) {
		final String credentialId = item.getCredentialId();
		final String votingCardSetId = item.getVotingCardSetId();
		final String electionEventId = item.getElectionEventId();
		final SerializedCredentialDataPack voterCredentialDataPack = item.getVoterCredentialDataPack();

		final String credentialSerializedKeyStoreB64 = voterCredentialDataPack.getSerializedKeyStore();
		if (StringUtils.isNotBlank(credentialSerializedKeyStoreB64)) {
			LOGGER.debug("{}. [electionEventId: {}, votingCardSetId: {}, credentialId: {}]", GENCREDAT_SUCCESS_KEYSTORE_GENERATED.getInfo(),
					electionEventId, votingCardSetId, credentialId);
		} else {
			LOGGER.error("{}. [electionEventId: {}, votingCardSetId: {}, credentialId: {}]", GENCREDAT_ERROR_GENERATING_KEYSTORE.getInfo(),
					electionEventId, votingCardSetId, credentialId);
			throw new GenerateVerificationCardCodesException("Error - the keyStore that is being written to file is invalid");
		}

		return String.format("%s,%s", credentialId, credentialSerializedKeyStoreB64);
	}
}
