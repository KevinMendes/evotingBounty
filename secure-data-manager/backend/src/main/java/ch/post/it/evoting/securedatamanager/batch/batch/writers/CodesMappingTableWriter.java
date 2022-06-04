/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.writers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonProcessingException;

import ch.post.it.evoting.securedatamanager.batch.batch.GeneratedVotingCardOutput;
import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.CreateVotingCardSetException;
import ch.post.it.evoting.securedatamanager.config.commons.utils.ConfigObjectMapper;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VerificationCardCodesDataPack;

public class CodesMappingTableWriter extends MultiFileDataWriter<GeneratedVotingCardOutput> {

	private static final ConfigObjectMapper mapper = new ConfigObjectMapper();

	public CodesMappingTableWriter(final Path basePath, final int maxNumCredentialsPerFile) {
		super(basePath, maxNumCredentialsPerFile);
	}

	@Override
	protected String getLine(final GeneratedVotingCardOutput item) {
		final VerificationCardCodesDataPack verificationCardCodesDataPack = item.getVerificationCardCodesDataPack();
		final String mappingAsJSONB64;
		try {
			// The TreeMap reorders the entries by their key to ensure that the original order of insertion is completely lost.
			mappingAsJSONB64 = Base64.getEncoder().encodeToString(
					mapper.fromJavaToJSON(new TreeMap<>(verificationCardCodesDataPack.getCodesMappingTable())).getBytes(StandardCharsets.UTF_8));
		} catch (final JsonProcessingException e) {
			throw new CreateVotingCardSetException("Exception while trying to encode codes mapping table to Json", e);
		}
		final String verificationCardId = item.getVerificationCardId();
		return String.format("%s,%s", verificationCardId, mappingAsJSONB64);
	}
}
