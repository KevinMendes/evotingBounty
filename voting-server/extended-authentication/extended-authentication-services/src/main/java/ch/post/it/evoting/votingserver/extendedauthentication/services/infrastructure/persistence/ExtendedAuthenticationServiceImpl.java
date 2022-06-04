/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence;

import static java.nio.file.Files.newBufferedReader;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.LockTimeoutException;
import javax.persistence.PessimisticLockException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.MappingIterator;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIPBKDFDeriver;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.commons.binary.ByteArrays;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;
import ch.post.it.evoting.domain.csv.CsvObjectReader;
import ch.post.it.evoting.domain.election.model.Information.VoterInformation;
import ch.post.it.evoting.domain.election.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.config.InfrastructureConfig;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.infrastructure.transaction.TransactionController;
import ch.post.it.evoting.votingserver.commons.infrastructure.transaction.TransactionalAction;
import ch.post.it.evoting.votingserver.commons.infrastructure.transaction.TransactionalActionException;
import ch.post.it.evoting.votingserver.commons.verify.CSVVerifier;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.EncryptedSVK;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.AllowedAttemptsExceededException;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.EaCsvVerifier;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthentication;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthenticationRepository;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.csv.FailedAuthenticationVotingCardItem;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.csv.FailedAuthenticationVotingCardsItemWriter;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.EaRemoteCertificateService;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.VoterMaterialService;

/**
 * The implementation of an Extended Authentication Service.
 */
@Stateless
public class ExtendedAuthenticationServiceImpl implements ExtendedAuthenticationService {

	// 4 means that the allowed max attempts is 5!
	public static final Integer MAX_ALLOWED_NUMBER_OF_ATTEMPTS = 4;

	private static final int MIN_EXTRA_PARAM_LENGTH = 16;

	private static final int BATCH_SIZE = Integer.parseInt(InfrastructureConfig.getEnvWithDefaultOption("synchronization.batch.size", "1000"));

	private static final String ADMINISTRATION_BOARD_CN_PREFIX = "AdministrationBoard ";
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedAuthenticationServiceImpl.class);
	@Inject
	protected ExtendedAuthenticationRepository extendedAuthenticationRepository;
	@EJB(beanName = "ExtendedAuthenticationTransactionController")
	protected TransactionController transactionController;
	protected PrimitivesService primitivesService;
	@Inject
	@EaCsvVerifier
	protected CSVVerifier verifier;

	@Inject
	@EaRemoteCertificateService
	protected RemoteCertificateService remoteCertificateService;

	@Inject
	private VoterMaterialService voterMaterialService;

	public ExtendedAuthenticationServiceImpl() {
		primitivesService = new PrimitivesService();
	}

	private static String leftPadding(final String s, final int n) {
		String format = "%1$" + n + "s";
		return String.format(format, s);
	}

	private static ExtendedAuthentication createNewEA(final String newSVK, final String newAuthId,
			final ExtendedAuthentication extendedAuthentication) {
		ExtendedAuthentication newEA = new ExtendedAuthentication();
		newEA.setAttempts(extendedAuthentication.getAttempts());
		newEA.setAuthId(newAuthId);
		newEA.setElectionEvent(extendedAuthentication.getElectionEvent());
		newEA.setEncryptedStartVotingKey(newSVK);
		newEA.setExtraParam(extendedAuthentication.getExtraParam());
		newEA.setSalt(extendedAuthentication.getSalt());
		newEA.setTenantId(extendedAuthentication.getTenantId());
		newEA.setCredentialId(extendedAuthentication.getCredentialId());
		return newEA;
	}

	private static String padExtraParameter(String providedExtraParam) {
		String providedExtraParamPadded;

		if (providedExtraParam.length() < MIN_EXTRA_PARAM_LENGTH) {
			providedExtraParamPadded = leftPadding(providedExtraParam, MIN_EXTRA_PARAM_LENGTH);
		} else {
			providedExtraParamPadded = providedExtraParam;
		}

		return providedExtraParamPadded;
	}

	private static byte[] toByteFromNullableString(String s) {
		byte[] result;
		if (s == null || s.isEmpty()) {
			result = new byte[0];
		} else {
			result = Base64.getDecoder().decode(s);
		}
		return result;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public EncryptedSVK authenticate(final String tenantId, final String authId, final String extraParam, final String electionEventId)
			throws ResourceNotFoundException, AllowedAttemptsExceededException, AuthenticationException, ApplicationException,
			GeneralCryptoLibException {

		ExtendedAuthentication extendedAuthentication = retrieveExistingExtendedAuthenticationForUpdate(tenantId, authId, electionEventId);

		Integer attempts = extendedAuthentication.getAttempts();
		if (attempts == null) {
			attempts = 0;
		}

		if (MAX_ALLOWED_NUMBER_OF_ATTEMPTS.compareTo(attempts) < 0) {
			LOGGER.error("Exceeded allowed attempts.");
			throw new AllowedAttemptsExceededException();
		}

		if (!attemptIsValid(extraParam, extendedAuthentication.getExtraParam(), extendedAuthentication.getSalt())) {
			int increasedAttempts = attempts + 1;
			extendedAuthentication.setAttempts(increasedAttempts);

			try {
				extendedAuthenticationRepository.update(extendedAuthentication);
			} catch (EntryPersistenceException e) {
				LOGGER.error("Error in repository trying to update the ExtendedAuthentication object.", e);
				throw new AuthenticationException(e, attempts);
			}
			LOGGER.error("Invalid attempt.");
			throw new AuthenticationException("invalid extra parameter", MAX_ALLOWED_NUMBER_OF_ATTEMPTS - attempts);
		}
		return new EncryptedSVK(extendedAuthentication.getEncryptedStartVotingKey());
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ExtendedAuthentication renewExistingExtendedAuthentication(final String tenantId, final String oldAuthId, final String newAuthId,
			final String newSVK, final String electionEventId) throws ApplicationException, ResourceNotFoundException {

		ExtendedAuthentication existingExtendedAuthentication = retrieveExistingExtendedAuthenticationForUpdate(tenantId, oldAuthId, electionEventId);

		extendedAuthenticationRepository.delete(existingExtendedAuthentication);

		ExtendedAuthentication newExtendedAuthentication = createNewEA(newSVK, newAuthId, existingExtendedAuthentication);

		try {
			extendedAuthenticationRepository.save(newExtendedAuthentication);
			return newExtendedAuthentication;

		} catch (DuplicateEntryException e) {
			LOGGER.error("Error in repository trying to save the ExtendedAuthentication object.", e);
			throw new ApplicationException(e.getMessage());
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ExtendedAuthentication retrieveExistingExtendedAuthenticationForUpdate(final String tenantId, final String authId,
			final String electionEventId) throws ResourceNotFoundException, ApplicationException {
		Optional<ExtendedAuthentication> extendedAuthentication;
		try {
			extendedAuthentication = extendedAuthenticationRepository.getForUpdate(tenantId, authId, electionEventId);
			if (!extendedAuthentication.isPresent()) {
				String errorMsg = String
						.format("Not found Extended Authentication with Id=%s, Tenant Id=%s and Election Event Id= %s.", authId, tenantId,
								electionEventId);
				LOGGER.error(errorMsg);
				throw new ResourceNotFoundException(errorMsg);
			}
		} catch (PessimisticLockException | LockTimeoutException e) {
			LOGGER.debug(e.getMessage(), e);
			String errorMsg = String
					.format("Cannot create lock on Extended Authentication entity with Id=%s, Tenant Id=%s and Election Event Id= %s.", authId,
							tenantId, electionEventId);
			LOGGER.error(errorMsg);
			throw new ApplicationException(errorMsg);
		}
		return extendedAuthentication.get();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public ExtendedAuthentication retrieveExistingExtendedAuthenticationForRead(final String tenantId, final String authId,
			final String electionEventId) throws ResourceNotFoundException {
		Optional<ExtendedAuthentication> extendedAuthentication;
		extendedAuthentication = extendedAuthenticationRepository.getForRead(tenantId, authId, electionEventId);
		if (!extendedAuthentication.isPresent()) {
			String errorMsg = String.format("Not found Extended Authentication with Id=%s, Tenant Id=%s and Election Event Id= %s.", authId, tenantId,
					electionEventId);
			LOGGER.error(errorMsg);
			throw new ResourceNotFoundException(errorMsg);
		}
		return extendedAuthentication.get();
	}

	@Override
	public boolean saveExtendedAuthenticationFromFile(final Path file, final String tenantId, final String electionEventId, final String adminBoardId)
			throws IOException {
		if (verifyAndRemoveSignature(adminBoardId, file)) {
			saveExtendedAuthenticationFileContents(file, tenantId, electionEventId, adminBoardId);
			return true;
		}
		return false;

	}

	private boolean attemptIsValid(final String extraParam, String storedExtraParam, String salt) throws GeneralCryptoLibException {
		if (salt != null && salt.length() != 0) {
			return checkIfChallengeMatches(extraParam, salt, storedExtraParam);
		}
		// In case the attempt does not contain a salt, that means that we are not using challenge based
		// authentication.
		return true;
	}

	private boolean verifyAndRemoveSignature(final String adminBoardId, final Path file) throws IOException {
		boolean valid = false;

		try {
			PublicKey key = getAdminBoardPublicKey(adminBoardId);
			valid = verifier.verify(key, file);
			if (!valid) {
				LOGGER.error("Extended Authentication data signature is not valid");
			}
		} catch (GeneralCryptoLibException | RetrofitException e) {
			LOGGER.error("Extended Authentication data signature could not be verified", e);
		}
		return valid;
	}

	private PublicKey getAdminBoardPublicKey(final String adminBoardId) throws GeneralCryptoLibException, RetrofitException {
		LOGGER.info("Fetching the administration board certificate");
		String name = ADMINISTRATION_BOARD_CN_PREFIX + adminBoardId;
		CertificateEntity entity = remoteCertificateService.getAdminBoardCertificate(name);
		String content = entity.getCertificateContent();
		Certificate certificate = PemUtils.certificateFromPem(content);
		return certificate.getPublicKey();
	}

	private void saveExtendedAuthenticationFileContents(final Path extendedAuthenticationFilePath, final String tenantId,
			final String electionEventId, final String adminBoardId) throws IOException {
		try {
			try (Reader reader = newBufferedReader(extendedAuthenticationFilePath, StandardCharsets.UTF_8);
					MappingIterator<ExtendedAuthentication> iterator = CsvObjectReader
							.readCsv(reader, ExtendedAuthentication.class, "authId", "extraParam", "encryptedStartVotingKey", "electionEvent", "salt",
									"credentialId")) {

				TransactionalAction<Void> action = context -> saveExtendedAuthenticationByBatch(tenantId, iterator, BATCH_SIZE);

				while (iterator.hasNext()) {
					transactionController.doInNewTransaction(action);
				}
			}
			LOGGER.info("Extended authentication data saved for electionEventId: {}, tenantId: {}, and adminBoardId: {}.", electionEventId, tenantId,
					adminBoardId);

		} catch (TransactionalActionException e) {
			LOGGER.debug(e.getMessage(), e);
			LOGGER.warn("Extended authentication data - duplicate entry for electionEventId: {}, tenantId: {}, and adminBoardId: {}.",
					electionEventId, tenantId, adminBoardId);
		}
	}

	@Override
	public void updateExistingExtendedAuthentication(ExtendedAuthentication extendedAuthentication) throws EntryPersistenceException {

		extendedAuthenticationRepository.update(extendedAuthentication);
	}

	@Override
	public void saveNewExtendedAuthentication(ExtendedAuthentication extendedAuthentication) throws DuplicateEntryException {
		extendedAuthenticationRepository.save(extendedAuthentication);
	}

	@Override
	public void findAndWriteVotingCardsWithFailedAuthentication(final String tenantId, final String electionEventId, final OutputStream stream)
			throws IOException {
		final List<ExtendedAuthentication> extendedAuthentications = extendedAuthenticationRepository
				.findAllExceededExtendedAuthentication(tenantId, electionEventId, MAX_ALLOWED_NUMBER_OF_ATTEMPTS);

		try (FailedAuthenticationVotingCardsItemWriter writer = new FailedAuthenticationVotingCardsItemWriter(stream)) {
			extendedAuthentications.stream().map(this::getAssociatedVotingCard).filter(Objects::nonNull).forEach(writer::write);
		}
	}

	private FailedAuthenticationVotingCardItem getAssociatedVotingCard(final ExtendedAuthentication ea) {
		final Optional<VoterInformation> voterInformation = voterMaterialService
				.getVoterInformationByCredentialId(ea.getTenantId(), ea.getElectionEvent(), ea.getCredentialId());
		return voterInformation.map(vi -> new FailedAuthenticationVotingCardItem(ea.getCredentialId(), vi.getVotingCardId())).orElse(null);
	}

	/**
	 * Private method that saves a defined number (batchSize) of new Extended Authentication entities from a generated iterator.
	 *
	 * @param tenantId  The Tenant Id.
	 * @param iterator  An iterator containing the new Extended Authentication entities to be saved.
	 * @param batchSize The number of elements to be saved in this batch.
	 * @return Nothing to return but a Void object expected by the transaction Controller.
	 */
	private Void saveExtendedAuthenticationByBatch(final String tenantId, final MappingIterator<ExtendedAuthentication> iterator,
			final int batchSize) {

		for (int i = 0; i < batchSize && iterator.hasNext(); i++) {

			ExtendedAuthentication extendedAuthentication = iterator.next();
			extendedAuthentication.setTenantId(tenantId);
			extendedAuthentication.setAttempts(0);

			try {
				this.saveNewExtendedAuthentication(extendedAuthentication);

			} catch (DuplicateEntryException e) {
				LOGGER.warn("Duplicate Warning: Error trying to save a new Extended Authentication with authId={} because it already exists.",
						extendedAuthentication.getAuthId(), e);
			}
		}
		return null;
	}

	private boolean checkIfChallengeMatches(final String providedExtraParam, final String salt, final String saltedExtraParam)
			throws GeneralCryptoLibException {

		final byte[] decodedSalt = toByteFromNullableString(salt);
		final byte[] saltedProvidedExtraParam = calculateHashFromDataAndSalt(providedExtraParam, decodedSalt);
		final byte[] decodedSaltedExtraParam = toByteFromNullableString(saltedExtraParam);
		return ByteArrays.constantTimeEquals(saltedProvidedExtraParam, decodedSaltedExtraParam);
	}

	private byte[] calculateHashFromDataAndSalt(String providedExtraParam, final byte[] salt) throws GeneralCryptoLibException {

		if (providedExtraParam == null || providedExtraParam.isEmpty()) {
			return new byte[0];
		}

		String providedExtraParamPadded = padExtraParameter(providedExtraParam);
		final CryptoAPIPBKDFDeriver derived = primitivesService.getPBKDFDeriver();
		final CryptoAPIDerivedKey cryptoAPIDerivedKeyPIN = derived.deriveKey(providedExtraParamPadded.toCharArray(), salt);
		return cryptoAPIDerivedKeyPIN.getEncoded();
	}
}
