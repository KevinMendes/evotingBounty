/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.writers;

import java.nio.file.Path;

import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.core.io.FileSystemResource;

import com.fasterxml.jackson.core.JsonProcessingException;

import ch.post.it.evoting.securedatamanager.batch.batch.GeneratedVotingCardOutput;
import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.CreateVotingCardSetException;
import ch.post.it.evoting.securedatamanager.config.commons.utils.ConfigObjectMapper;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VerificationCardCodesDataPack;

public class StandardPrintingDataWriter extends FlatFileItemWriter<GeneratedVotingCardOutput> {

	private static final ConfigObjectMapper mapper = new ConfigObjectMapper();

	public StandardPrintingDataWriter(final Path path) {

		setLineAggregator(lineAggregator());
		setTransactional(false);
		setAppendAllowed(false);
		setShouldDeleteIfExists(true);
		setResource(new FileSystemResource(path.toString()));
	}

	private LineAggregator<GeneratedVotingCardOutput> lineAggregator() {

		return item -> {
			final String votingCardId = item.getVotingCardId();
			final String verificationCardId = item.getVerificationCardId();
			final String electionEventId = item.getElectionEventId();
			final String ballotId = item.getBallotId();
			final VerificationCardCodesDataPack verificationCardCodesDataPack = item.getVerificationCardCodesDataPack();
			final String ballotCastingKey = verificationCardCodesDataPack.getBallotCastingKey();
			final String voteCastingCode = verificationCardCodesDataPack.getVoteCastingCode();
			final String startVotingKey = item.getStartVotingKey();

			final String mapChoicesCodesToVotingOptionsAsJSON;
			try {
				mapChoicesCodesToVotingOptionsAsJSON = mapper.fromJavaToJSON(verificationCardCodesDataPack.getMapChoiceCodesToVotingOption());
			} catch (final JsonProcessingException e) {
				throw new CreateVotingCardSetException("Exception while trying to encode choice codes to voting options map to Json", e);
			}
			return String.format("%s;%s;%s;%s;%s;%s;%s;%s", votingCardId, verificationCardId, electionEventId, mapChoicesCodesToVotingOptionsAsJSON,
					ballotCastingKey, voteCastingCode, ballotId, startVotingKey);
		};
	}
}
