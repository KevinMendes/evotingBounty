/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.tally;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

@Repository
public class MixnetInitialPayloadFileRepository extends MixnetPayloadFileRepository {

	@Autowired
	public MixnetInitialPayloadFileRepository(
			@Value("${shufflePayload.filePrefix:mixnetInitialPayload_}")
			final String filePrefix, final ObjectMapper objectMapper, final PathResolver payloadResolver) {
		super(filePrefix, objectMapper, payloadResolver);
	}

	/**
	 * Saves the mix net initial payload to the filesystem for the given election event, ballot and ballot box.
	 *
	 * @return the path of the saved file.
	 * @throws NullPointerException     if any of the inputs is null.
	 * @throws IllegalArgumentException if any of the inputs is not valid.
	 * @see PathResolver to get the resolved file Path.
	 */
	public Path savePayload(final String electionEventId, final String ballotId, final String ballotBoxId, final MixnetInitialPayload payload) {
		return super.savePayload(electionEventId, ballotId, ballotBoxId, payload, 0);
	}
}
