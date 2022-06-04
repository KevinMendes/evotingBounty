/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.votingserver.voteverification.service;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.UncheckedIOException;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.domain.configuration.ReturnCodesMappingTablePayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.ReturnCodesMappingTableEntity;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.ReturnCodesMappingTableRepository;

/**
 * Saves and retrieves the return codes mapping table.
 */
@Stateless
public class ReturnCodesMappingTableService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReturnCodesMappingTableService.class);

	private final ObjectMapper objectMapper;
	private final ReturnCodesMappingTableRepository returnCodesMappingTableRepository;

	@Inject
	ReturnCodesMappingTableService(final ObjectMapper objectMapper,
			final ReturnCodesMappingTableRepository returnCodesMappingTableRepository) {
		this.objectMapper = objectMapper;
		this.returnCodesMappingTableRepository = returnCodesMappingTableRepository;
	}

	/**
	 * Saves the return codes mapping table.
	 *
	 * @param returnCodesMappingTablePayload the request payload. Must be non-null.
	 * @throws DuplicateEntryException if the return code mapping table is already saved in the database.
	 * @throws UncheckedIOException    if an error occurs while serializing the return code mapping table.
	 */
	public void saveReturnCodesMappingTable(final ReturnCodesMappingTablePayload returnCodesMappingTablePayload)
			throws DuplicateEntryException, UncheckedIOException {
		checkNotNull(returnCodesMappingTablePayload);

		final ReturnCodesMappingTableEntity returnCodesMappingTableEntity = new ReturnCodesMappingTableEntity();

		returnCodesMappingTableEntity.setVerificationCardSetId(returnCodesMappingTablePayload.getVerificationCardSetId());
		returnCodesMappingTableEntity.setElectionEventId(returnCodesMappingTablePayload.getElectionEventId());

		final byte[] serializedReturnCodesMappingTable;
		try {
			serializedReturnCodesMappingTable = objectMapper.writeValueAsBytes(returnCodesMappingTablePayload.getReturnCodesMappingTable());
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException("Failed to serialize return codes mapping table.", e);
		}
		returnCodesMappingTableEntity.setReturnCodesMappingTable(serializedReturnCodesMappingTable);

		// save return codes mapping table in vote verification
		returnCodesMappingTableRepository.save(returnCodesMappingTableEntity);
		LOGGER.info("Return codes mapping table successfully saved in vote verification. [electionEventId: {}, verificationCardSetId: {}]",
				returnCodesMappingTableEntity.getElectionEventId(), returnCodesMappingTableEntity.getVerificationCardSetId());
	}

	/**
	 * Retrieves the return codes mapping table for given a verification card set id.
	 *
	 * @param verificationCardSetId the verification card set id. Must be non-null and a valid UUID.
	 * @return the return codes mapping table.
	 * @throws FailedValidationException if {@code verificationCardSetId} is invalid.
	 * @throws ResourceNotFoundException if return codes mapping table is not found.
	 */
	public ReturnCodesMappingTableEntity retrieveReturnCodesMappingTable(final String verificationCardSetId) throws ResourceNotFoundException {
		validateUUID(verificationCardSetId);

		final ReturnCodesMappingTableEntity returnCodesMappingTableEntity = returnCodesMappingTableRepository.find(verificationCardSetId);
		if (returnCodesMappingTableEntity == null) {
			throw new ResourceNotFoundException(
					String.format("No return codes mapping table found for verificationCardSetId: %s", verificationCardSetId));
		}
		return returnCodesMappingTableEntity;
	}
}