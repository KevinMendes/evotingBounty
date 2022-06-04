/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.choicecodes.domain.model.computedvalues;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.ejb.Local;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ChoiceCodeGenerationDTO;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationResponsePayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Provides operations on the computed values repository.
 */
@Local
public interface ComputedValuesRepository extends BaseRepository<ComputedValues, Integer> {

	/**
	 * Returns a computed values entry for a given tenant, election event and verification card set identifier.
	 *
	 * @param tenantId              - the tenant identifier.
	 * @param electionEventId       - the election event identifier.
	 * @param verificationCardSetId - the verification card set identifier.
	 * @param chunkId               - the chunk identifier
	 * @return A list of computed values.
	 * @throws ResourceNotFoundException
	 */
	ComputedValues findByTenantIdElectionEventIdVerificationCardSetId(String tenantId, String electionEventId, String verificationCardSetId,
			int chunkId) throws ResourceNotFoundException;

	/**
	 * Checks if a computed values entry for a given tenant, election event verification card set and chunk identifier exists.
	 *
	 * @param tenantId
	 * @param electionEventId
	 * @param verificationCardSetId
	 * @param chunkId
	 * @return true if exists, false otherwise
	 * @throws ResourceNotFoundException
	 */
	boolean existsByTenantIdElectionEventIdVerificationCardSetIdChunkId(String tenantId, String electionEventId, String verificationCardSetId,
			int chunkId) throws ResourceNotFoundException;

	/**
	 * Checks if a computed values entry for a given tenant, election event and verification card set identifier exists.
	 *
	 * @param tenantId
	 * @param electionEventId
	 * @param verificationCardSetId
	 * @return true if exists, false otherwise
	 * @throws ResourceNotFoundException
	 */
	boolean existsByTenantIdElectionEventIdVerificationCardSetId(String tenantId, String electionEventId, String verificationCardSetId)
			throws ResourceNotFoundException;

	/**
	 * Checks if a computed values entry for a given tenant, election event, verification card set and chunk identifier has already been computed.
	 *
	 * @param tenantId
	 * @param electionEventId
	 * @param verificationCardSetId
	 * @param chunkId
	 * @return
	 * @throws ResourceNotFoundException
	 */
	boolean isComputedByTenantIdElectionEventIdVerificationCardSetIdChunkId(String tenantId, String electionEventId, String verificationCardSetId,
			int chunkId) throws ResourceNotFoundException;

	/**
	 * Get an stream from which the content of the json field of a computed values entry for a given tenant, election event and verification card set
	 * identifier can be read.
	 *
	 * @param stream
	 * @param tenantId
	 * @param electionEventId
	 * @param verificationCardSetId
	 * @param chunkId
	 * @return the stream from which the json field content can be read
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 */
	void writeJsonToStreamForTenantIdElectionEventIdVerificationCardSetIdChunkId(OutputStream stream, String tenantId, String electionEventId,
			String verificationCardSetId, int chunkId) throws ResourceNotFoundException, IOException;

	/**
	 * Checks if all computed values entries for a given tenant, election event and verification card set identifier have already been computed.
	 *
	 * @param tenantId
	 * @param electionEventId
	 * @param verificationCardSetId
	 * @param chunkCount
	 * @return
	 */
	boolean areComputedByTenantIdElectionEventIdVerificationCardSetId(String tenantId, String electionEventId, String verificationCardSetId,
			int chunkCount);

	/**
	 * Saves computation results into an existing computed values record
	 *
	 * @param tenantId
	 * @param electionEventId
	 * @param verificationCardSetId
	 * @param chunkId
	 * @param computationResults
	 * @throws EntryPersistenceException if failed to save computed values
	 */
	void update(String tenantId, String electionEventId, String verificationCardSetId, int chunkId,
			List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> computationResults) throws EntryPersistenceException;

}
