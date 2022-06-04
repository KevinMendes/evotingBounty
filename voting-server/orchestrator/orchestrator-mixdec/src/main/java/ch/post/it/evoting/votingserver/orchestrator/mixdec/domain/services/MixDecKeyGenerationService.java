/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalEncryptionParameters;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingException;

/**
 * Permits the generation of MixDec keys.
 */
public interface MixDecKeyGenerationService {

	/**
	 * Request the generation of MixDec keys.
	 *
	 * @param trackingId            the tracking ID.
	 * @param tenantId              the tenant ID.
	 * @param electionEventId       the election event ID.
	 * @param electoralAuthorityIds a list of Electoral Authority IDs.
	 * @return a map where each key in the map is an Electoral Authority and the corresponding value is a serialized ElGamal publickey.
	 * @throws IOException
	 * @throws ResourceNotFoundException
	 * @throws GeneralCryptoLibException
	 */
	Map<String, List<String>> requestMixDecKeyGenerationSync(String trackingId, String tenantId, String electionEventId,
			final List<String> electoralAuthorityIds, ZonedDateTime keysDateFrom, ZonedDateTime keysDateTo,
			ElGamalEncryptionParameters elGamalEncryptionParameters) throws IOException, ResourceNotFoundException, GeneralCryptoLibException;

	/**
	 * Starts the service up.
	 *
	 * @throws MessagingException
	 */
	void startup() throws MessagingException;

	/**
	 * Shuts the service down.
	 *
	 * @throws MessagingException
	 */
	void shutdown() throws MessagingException;
}
