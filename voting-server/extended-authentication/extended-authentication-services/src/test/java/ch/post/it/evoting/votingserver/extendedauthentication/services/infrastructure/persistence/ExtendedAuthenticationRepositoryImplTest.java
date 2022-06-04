/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence;

import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthentication;

import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.db.transaction.TransactionHelper;
import de.akquinet.jbosscc.needle.db.transaction.VoidRunnable;
import de.akquinet.jbosscc.needle.junit.DatabaseRule;
import de.akquinet.jbosscc.needle.junit.NeedleRule;

public class ExtendedAuthenticationRepositoryImplTest {

	private static final String VOTING_CARD_ID_2 = "26f4e529b51bca0f5f4fa6f1339a0245;Fk0vw+glXPNlTUnziNKH8w==";
	private static final String ENCRYPTED_START_VOTING_KEY = "QzZB//+c1Kn0WjppIsJhfQwuF4RUmdTaJf5YoG9iCaeMbW5en/eMxG+1W2NYkv0e";
	private static final String TENANT_ID = "1";
	private static final String ELECTION_EVENT = "2";
	private static final String CREDENTIAL_ID = "3";

	@Rule
	public DatabaseRule databaseRule = new DatabaseRule();

	private final TransactionHelper transactionHelper = databaseRule.getTransactionHelper();

	@Rule
	public NeedleRule needleRule = new NeedleRule(databaseRule);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	Optional<ExtendedAuthentication> extendedAuthenticationFromDb = Optional.empty();

	@Inject
	private EntityManager entityManager;

	@ObjectUnderTest
	private ExtendedAuthenticationRepositoryImpl extendedAuthenticationRepository;

	@Test
	public void find() throws Exception {
		final ExtendedAuthentication extendedAuthentication = buildAndSave(VOTING_CARD_ID_2);
		transactionHelper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(final EntityManager entityManager) throws Exception {

				extendedAuthenticationFromDb = extendedAuthenticationRepository.getForUpdate(TENANT_ID, VOTING_CARD_ID_2, ELECTION_EVENT);
			}
		});

		Assert.assertNotNull(extendedAuthenticationFromDb);
		Assert.assertEquals(ENCRYPTED_START_VOTING_KEY, extendedAuthenticationFromDb.get().getEncryptedStartVotingKey());
		Assert.assertEquals(extendedAuthentication.getAuthId(), extendedAuthenticationFromDb.get().getAuthId());
		Assert.assertEquals(extendedAuthentication.getTenantId(), extendedAuthenticationFromDb.get().getTenantId());
		Assert.assertEquals(extendedAuthentication.getElectionEvent(), extendedAuthenticationFromDb.get().getElectionEvent());
	}

	@Test
	public void getWithoutLockingSuccessful() throws Exception {
		final ExtendedAuthentication extendedAuthentication = buildAndSave(VOTING_CARD_ID_2);
		transactionHelper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(final EntityManager entityManager) throws Exception {

				extendedAuthenticationFromDb = extendedAuthenticationRepository.getForRead(TENANT_ID, VOTING_CARD_ID_2, ELECTION_EVENT);
			}
		});

		Assert.assertNotNull(extendedAuthenticationFromDb);
		Assert.assertEquals(ENCRYPTED_START_VOTING_KEY, extendedAuthenticationFromDb.get().getEncryptedStartVotingKey());
		Assert.assertEquals(extendedAuthentication.getAuthId(), extendedAuthenticationFromDb.get().getAuthId());
		Assert.assertEquals(extendedAuthentication.getTenantId(), extendedAuthenticationFromDb.get().getTenantId());
		Assert.assertEquals(extendedAuthentication.getElectionEvent(), extendedAuthenticationFromDb.get().getElectionEvent());
	}

	@Test
	public void not_found() throws Exception {
		buildAndSave("Voting Card Id");
		transactionHelper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(final EntityManager entityManager) throws Exception {
				extendedAuthenticationFromDb = extendedAuthenticationRepository.getForUpdate(TENANT_ID, "NOT_IN_DB", ELECTION_EVENT);
			}
		});
		Assert.assertFalse(extendedAuthenticationFromDb.isPresent());
	}

	@Test
	public void update() throws Exception {
		final ExtendedAuthentication extendedAuthentication = buildAndSave(VOTING_CARD_ID_2);
		transactionHelper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(final EntityManager entityManager) throws Exception {
				extendedAuthenticationFromDb = extendedAuthenticationRepository.getForUpdate(TENANT_ID, VOTING_CARD_ID_2, ELECTION_EVENT);
			}
		});

		Assert.assertNotNull(extendedAuthenticationFromDb);
		Assert.assertTrue(extendedAuthenticationFromDb.isPresent());
		Assert.assertEquals(ENCRYPTED_START_VOTING_KEY, extendedAuthenticationFromDb.get().getEncryptedStartVotingKey());
		Assert.assertEquals(extendedAuthentication.getAuthId(), extendedAuthenticationFromDb.get().getAuthId());
		Assert.assertEquals(extendedAuthentication.getTenantId(), extendedAuthenticationFromDb.get().getTenantId());
		Assert.assertEquals(extendedAuthentication.getElectionEvent(), extendedAuthenticationFromDb.get().getElectionEvent());

		transactionHelper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(final EntityManager entityManager) throws Exception {
				extendedAuthenticationFromDb = extendedAuthenticationRepository.getForUpdate(TENANT_ID, VOTING_CARD_ID_2, ELECTION_EVENT);
				extendedAuthenticationRepository.delete(extendedAuthenticationFromDb.get());
			}
		});
		transactionHelper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(final EntityManager entityManager) throws Exception {
				extendedAuthenticationFromDb = extendedAuthenticationRepository.getForUpdate(TENANT_ID, VOTING_CARD_ID_2, ELECTION_EVENT);
			}
		});
		Assert.assertNotNull(extendedAuthenticationFromDb);
		Assert.assertFalse(extendedAuthenticationFromDb.isPresent());
	}

	@Test
	public void testGetForReadNotFoundNoException() throws Exception {

		buildAndSave(VOTING_CARD_ID_2);

		transactionHelper.executeInTransaction(new VoidRunnable() {
			@Override
			public void doRun(final EntityManager entityManager) throws Exception {
				final Optional<ExtendedAuthentication> ea = extendedAuthenticationRepository.getForRead("notFound", "notFound", "notFound");
				Assert.assertFalse(ea.isPresent());
			}
		});
	}

	private ExtendedAuthentication buildAndSave(final String votingCardId2) {
		return new ExtendedAuthenticationTestdataBuilder(entityManager).withEncryptedStartVotingKey(ENCRYPTED_START_VOTING_KEY)
				.withVotingCardId2(votingCardId2).withTenantId(TENANT_ID).withElectionEvent(ELECTION_EVENT).withAttempts(0)
				.withCredentialId(CREDENTIAL_ID).buildAndSave();
	}

}
