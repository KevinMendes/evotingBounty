/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.choicecodes.domain.services;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ChoiceCodeGenerationDTO;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationResponsePayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.orchestrator.choicecodes.domain.model.computedvalues.ComputedValues;
import ch.post.it.evoting.votingserver.orchestrator.choicecodes.domain.model.computedvalues.ComputedValuesRepository;

/**
 * Service for handling computed values.
 */
@Stateless
public class ComputedValuesService {

	@Inject
	private ComputedValuesRepository computedValuesRepository;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void create(String tenantId, String electionEventId, String verificationCardSetId, int chunkId) throws DuplicateEntryException {

		ComputedValues computedValues = new ComputedValues();
		computedValues.setTenantId(tenantId);
		computedValues.setElectionEventId(electionEventId);
		computedValues.setVerificationCardSetId(verificationCardSetId);
		computedValues.setChunkId(chunkId);
		computedValuesRepository.save(computedValues);

	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void update(String tenantId, String electionEventId, String verificationCardSetId, int chunkId,
			List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> computationResults) throws EntryPersistenceException {

		computedValuesRepository.update(tenantId, electionEventId, verificationCardSetId, chunkId, computationResults);
	}

}
