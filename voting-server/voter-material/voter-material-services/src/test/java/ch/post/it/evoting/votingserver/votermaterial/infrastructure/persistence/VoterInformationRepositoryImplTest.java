/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.infrastructure.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VoterInformation;

import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.db.transaction.TransactionHelper;
import de.akquinet.jbosscc.needle.db.transaction.VoidRunnable;
import de.akquinet.jbosscc.needle.junit.DatabaseRule;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

public class VoterInformationRepositoryImplTest {

	private final String TENANT_ID = "1";
	private final String ELECTION_EVENT_ID = "2";
	private final String VOTING_CARD_ID = "3";
	private final String CREDENTIAL_ID = "4";
	private final String BALLOT_ID = "5";
	private final String VERIFICATION_CARD_ID = "6";
	private final String BALLOT_BOX_ID = "7";
	private final String VERIFICATION_CARD_SET_ID = "8";
	private final String VOTING_CARD_SET_ID = "9";

	@Rule
	public DatabaseRule databaseRule = new DatabaseRule();
	private final TransactionHelper transactionHelper = databaseRule.getTransactionHelper();
	@Rule
	public NeedleRule needleRule = new NeedleRule(databaseRule);
	@Inject
	private EntityManager entityManager;
	@ObjectUnderTest
	private VoterInformationRepositoryImpl rut;
	private Long countResult = null;
	private List<VoterInformation> voterInformationListResult = null;
	private VoterInformation voterInformationResult = null;
	private Boolean booleanResult = null;

	@Test
	public void testCountByTenantIdElectionEventIdAndSearchTermsSuccessful() throws Exception {
		buildTestData();

		transactionHelper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(EntityManager entityManager) throws Exception {
				countResult = rut.countByTenantIdElectionEventIdAndSearchTerms(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);
			}
		});

		Long expectedResult = 1l;

		assertEquals(expectedResult, countResult);
	}

	@Test
	public void testFindByTenantIdElectionEventIdAndSearchTermsSuccessful() throws Exception {
		buildTestData();

		transactionHelper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(EntityManager entityManager) throws Exception {
				voterInformationListResult = rut.findByTenantIdElectionEventIdAndSearchTerms(TENANT_ID, ELECTION_EVENT_ID, "", 0, 100);
			}
		});

		assertTrue(voterInformationListResult.size() > 0);
		assertEquals(ELECTION_EVENT_ID, voterInformationListResult.get(0).getElectionEventId());
		assertEquals(VOTING_CARD_ID, voterInformationListResult.get(0).getVotingCardId());
	}

	@Test
	public void testFindByTenantIdElectionEventIdCredentialIdSuccessful() throws Exception {
		buildTestData();

		transactionHelper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(EntityManager entityManager) throws Exception {
				voterInformationResult = rut.findByTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID);
			}
		});

		assertEquals(CREDENTIAL_ID, voterInformationResult.getCredentialId());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testFindByTenantIdElectionEventIdCredentialIdNotFound() throws Exception {
		buildTestData();

		String unexistingCredentialId = "UnexistingCredentialId";

		transactionHelper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(EntityManager entityManager) throws Exception {
				voterInformationResult = rut.findByTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, unexistingCredentialId);
			}
		});
	}

	@Test
	public void testHasWithTenantIdElectionEventIdVotingCardIdSuccessful() throws Exception {
		buildTestData();

		transactionHelper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(EntityManager entityManager) throws Exception {
				booleanResult = rut.hasWithTenantIdElectionEventIdVotingCardId(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);
			}
		});

		assertTrue(booleanResult);
	}

	@Test
	public void testFindByTenantIdElectionEventIdVotingCardIdSuccessful() throws Exception {
		buildTestData();

		transactionHelper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(EntityManager entityManager) throws Exception {
				voterInformationResult = rut.findByTenantIdElectionEventIdVotingCardId(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);
			}
		});

		assertNotNull(voterInformationResult.getId());
		assertEquals(TENANT_ID, voterInformationResult.getTenantId());
		assertEquals(BALLOT_ID, voterInformationResult.getBallotId());
		assertEquals(BALLOT_BOX_ID, voterInformationResult.getBallotBoxId());
		assertEquals(VERIFICATION_CARD_ID, voterInformationResult.getVerificationCardId());
		assertEquals(VERIFICATION_CARD_SET_ID, voterInformationResult.getVerificationCardSetId());
		assertEquals(VOTING_CARD_ID, voterInformationResult.getVotingCardId());
		assertEquals(VOTING_CARD_SET_ID, voterInformationResult.getVotingCardSetId());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testFindByTenantIdElectionEventIdVotingCardIdNotFound() throws Exception {
		buildTestData();

		String unexistingVotingCardId = "UnexistingVotingCardId";

		transactionHelper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(EntityManager entityManager) throws Exception {
				voterInformationResult = rut.findByTenantIdElectionEventIdVotingCardId(TENANT_ID, ELECTION_EVENT_ID, unexistingVotingCardId);
			}
		});
	}

	public void buildTestData() {
		VoterInformationTestdataBuilder dataBuilder = new VoterInformationTestdataBuilder(entityManager);

		VoterInformation voterInformation = new VoterInformation();
		voterInformation.setTenantId(TENANT_ID);
		voterInformation.setElectionEventId(ELECTION_EVENT_ID);
		voterInformation.setVotingCardId(VOTING_CARD_ID);
		voterInformation.setBallotBoxId(BALLOT_BOX_ID);
		voterInformation.setBallotId(BALLOT_ID);
		voterInformation.setCredentialId(CREDENTIAL_ID);
		voterInformation.setVerificationCardId(VERIFICATION_CARD_ID);
		voterInformation.setVerificationCardSetId(VERIFICATION_CARD_SET_ID);
		voterInformation.setVotingCardSetId(VOTING_CARD_SET_ID);

		dataBuilder.setVoterInformation(voterInformation);

		dataBuilder.buildAndSave();
	}

}
