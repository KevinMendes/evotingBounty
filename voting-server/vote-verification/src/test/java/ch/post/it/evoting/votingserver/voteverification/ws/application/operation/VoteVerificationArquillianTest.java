/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.ws.application.operation;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.junit.Before;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIKDFDeriver;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIPBKDFDeriver;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomInteger;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomString;

public class VoteVerificationArquillianTest {

	protected static final String CHOICE_CODE_VALUE = "45611111111111111111111110";
	protected static final String ELECTION_EVENT_ID = "67cd59d8183f47a3a4cc64abfcc2916b";
	protected static final String TENANT_ID = "100";
	protected static final String CAST_CODE_MESSAGE_SIGNATURE = "test-signature";
	@PersistenceContext(unitName = "persistenceUnitJdbc")
	protected EntityManager entityManager;
	protected int STATUS_OK = 200;
	@Resource
	UserTransaction userTransaction;

	@Before
	public void cleanDatabase()
			throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException {
		userTransaction.begin();
		entityManager.createQuery("delete from Verification").executeUpdate();
		entityManager.createQuery("delete from VerificationContent ").executeUpdate();
		entityManager.createQuery("delete from CodesMapping ").executeUpdate();
		userTransaction.commit();
	}

	public static class PrimitivesServiceMock implements PrimitivesServiceAPI {

		@Override
		public CryptoAPIRandomString get32CharAlphabetCryptoRandomString() {
			return null;
		}

		@Override
		public CryptoAPIRandomString get64CharAlphabetCryptoRandomString() {
			return null;
		}

		@Override
		public CryptoAPIRandomInteger getCryptoRandomInteger() {
			return null;
		}

		@Override
		public byte[] getHash(byte[] arg0) {
			return new byte[0];
		}

		@Override
		public byte[] getHash(InputStream arg0) {
			return null;
		}

		@Override
		public CryptoAPIKDFDeriver getKDFDeriver() {
			return null;
		}

		@Override
		public CryptoAPIPBKDFDeriver getPBKDFDeriver() {
			return null;
		}

		@Override
		public byte[] genRandomBytes(int lengthInBytes) {
			return new byte[0];
		}

		@Override
		public MessageDigest getRawMessageDigest() {
			return null;
		}

		@Override
		public byte[] getHashOfObjects(Stream<?> objectsToHash, Charset charset) {
			return null;
		}
	}

}
