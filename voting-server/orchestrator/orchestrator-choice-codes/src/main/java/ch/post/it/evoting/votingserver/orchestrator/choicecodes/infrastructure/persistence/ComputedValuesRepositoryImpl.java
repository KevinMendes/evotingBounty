/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.choicecodes.infrastructure.persistence;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ChoiceCodeGenerationDTO;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationResponsePayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.orchestrator.choicecodes.domain.model.computedvalues.ComputedValues;
import ch.post.it.evoting.votingserver.orchestrator.choicecodes.domain.model.computedvalues.ComputedValuesRepository;

/**
 * The implementation of the operations on the computed values repository. The implementation uses JPA as data layer to access database.
 */
@Stateless
public class ComputedValuesRepositoryImpl extends BaseRepositoryImpl<ComputedValues, Integer> implements ComputedValuesRepository {

	private static final ExecutorService executor = Executors.newCachedThreadPool();

	/* The name of parameter tenant id */
	private static final String PARAMETER_TENANT_ID = "tenantId";

	/* The name of parameter election event id */
	private static final String PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	/* The name of parameter verification card set id */
	private static final String PARAMETER_VERIFICATION_CARD_SET_ID = "verificationCardSetId";

	/* The name of parameter chunk id */
	private static final String PARAMETER_CHUNK_ID = "chunkId";

	private static final int BUFFER_SIZE = 8192;

	@Override
	public ComputedValues findByTenantIdElectionEventIdVerificationCardSetId(String tenantId, String electionEventId, String verificationCardSetId,
			int chunkId) throws ResourceNotFoundException {
		TypedQuery<ComputedValues> query = entityManager.createQuery(
				"SELECT c FROM ComputedValues c WHERE c.tenantId = :tenantId AND c.electionEventId = :electionEventId AND c.verificationCardSetId = :verificationCardSetId AND c.chunkId = :chunkId",
				ComputedValues.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VERIFICATION_CARD_SET_ID, verificationCardSetId);
		query.setParameter(PARAMETER_CHUNK_ID, chunkId);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		}
	}

	@Override
	public void writeJsonToStreamForTenantIdElectionEventIdVerificationCardSetIdChunkId(OutputStream stream, String tenantId, String electionEventId,
			String verificationCardSetId, int chunkId) throws ResourceNotFoundException, IOException {
		String computedValuesJsonQuery =
				"SELECT c.json FROM computed_values c WHERE c.tenant_id = :tenantId AND c.election_event_id = :electionEventId "
						+ "AND c.verification_card_set_id = :verificationCardSetId AND c.chunk_id = :chunkId";
		final Session session = entityManager.unwrap(Session.class);
		final SQLQuery query = session.createSQLQuery(computedValuesJsonQuery);

		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VERIFICATION_CARD_SET_ID, verificationCardSetId);
		query.setParameter(PARAMETER_CHUNK_ID, chunkId);

		try {
			Clob result = (Clob) query.uniqueResult();
			try {
				if (result == null) {
					throw new ResourceNotFoundException(
							"Verification Card Set " + verificationCardSetId + " contributions generation is not completed");
				}
				final char[] buffer = new char[BUFFER_SIZE];
				try (Reader resultReader = result.getCharacterStream(1, (int) result.length());
						Writer resultWriter = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
					int read;
					while ((read = resultReader.read(buffer)) != -1) {
						resultWriter.write(buffer, 0, read);
					}
				}
			} finally {
				if (result != null) {
					result.free();
				}
			}
		} catch (NoResultException | SQLException e) {
			throw new ResourceNotFoundException("", e);
		}
	}

	@Override
	public boolean existsByTenantIdElectionEventIdVerificationCardSetId(String tenantId, String electionEventId, String verificationCardSetId)
			throws ResourceNotFoundException {
		Query query = entityManager.createQuery(
				"SELECT COUNT(1) FROM ComputedValues c WHERE c.tenantId = :tenantId AND c.electionEventId = :electionEventId AND c.verificationCardSetId = :verificationCardSetId");
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VERIFICATION_CARD_SET_ID, verificationCardSetId);
		try {
			return 0L < (Long) query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		}
	}

	@Override
	public boolean existsByTenantIdElectionEventIdVerificationCardSetIdChunkId(String tenantId, String electionEventId, String verificationCardSetId,
			int chunkId) throws ResourceNotFoundException {
		Query query = entityManager.createQuery(
				"SELECT COUNT(1) FROM ComputedValues c WHERE c.tenantId = :tenantId AND c.electionEventId = :electionEventId AND c.verificationCardSetId = :verificationCardSetId AND c.chunkId = :chunkId");
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VERIFICATION_CARD_SET_ID, verificationCardSetId);
		query.setParameter(PARAMETER_CHUNK_ID, chunkId);
		try {
			return 0L < (Long) query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		}
	}

	@Override
	public boolean isComputedByTenantIdElectionEventIdVerificationCardSetIdChunkId(String tenantId, String electionEventId,
			String verificationCardSetId, int chunkId) throws ResourceNotFoundException {
		Query query = entityManager.createQuery(
				"SELECT COUNT(1) FROM ComputedValues c WHERE c.tenantId = :tenantId AND c.electionEventId = :electionEventId AND c.verificationCardSetId = :verificationCardSetId AND c.chunkId = :chunkId AND c.json IS NOT NULL");
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VERIFICATION_CARD_SET_ID, verificationCardSetId);
		query.setParameter(PARAMETER_CHUNK_ID, chunkId);
		try {
			return 1 == ((Long) query.getSingleResult()).intValue();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		}
	}

	@Override
	public boolean areComputedByTenantIdElectionEventIdVerificationCardSetId(String tenantId, String electionEventId, String verificationCardSetId,
			int chunkCount) {
		Query query = entityManager.createQuery(
				"SELECT COUNT(1) FROM ComputedValues c WHERE c.tenantId = :tenantId AND c.electionEventId = :electionEventId AND c.verificationCardSetId = :verificationCardSetId AND c.json IS NOT NULL");
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VERIFICATION_CARD_SET_ID, verificationCardSetId);
		return chunkCount == ((Long) query.getSingleResult()).intValue();
	}

	@Override
	public void update(String tenantId, String electionEventId, String verificationCardSetId, int chunkId,
			List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>> computationResults) throws EntryPersistenceException {

		try (PipedInputStream in = new PipedInputStream(); PipedOutputStream out = new PipedOutputStream(in)) {

			final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
			final Runnable writeJsonToPipeCommand = () -> {
				try {
					mapper.writeValue(out, computationResults);
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			};

			executor.execute(writeJsonToPipeCommand);

			Session session = entityManager.unwrap(Session.class);

			Integer count = session.doReturningWork(work -> {

				final String queryString = "UPDATE computed_values SET json = ? WHERE tenant_id = ? AND verification_card_set_id = ? AND election_event_id = ? AND chunk_id = ?";

				PreparedStatement pstmt = work.prepareStatement(queryString);
				int recordCount;
				{
					pstmt.setAsciiStream(1, in);
					pstmt.setString(2, tenantId);
					pstmt.setString(3, verificationCardSetId);
					pstmt.setString(4, electionEventId);
					pstmt.setInt(5, chunkId);

					recordCount = pstmt.executeUpdate();
				}
				return recordCount;
			});

			if (count == 0) {
				throw new EntryPersistenceException(
						String.format("No computed values record was updated by tenantId %s verificationCardId %s electionEventId %s chunkId %d",
								tenantId, verificationCardSetId, electionEventId, chunkId));
			}

		} catch (IOException e) {
			throw new EntryPersistenceException("Error updating computed values", e);
		}
	}

}
