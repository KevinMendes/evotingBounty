/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import static com.google.common.base.Preconditions.checkNotNull;

import java.security.KeyManagementException;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;

@Service
public class ElectionSigningKeysService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionSigningKeysService.class);

	private final KeysManager keysManager;
	private final KeysAndCertificateGenerator keysAndCertificateGenerator;
	private final KeysRepository keysRepository;
	private final String controlComponentId;

	public ElectionSigningKeysService(final KeysManager keysManager, final KeysAndCertificateGenerator keysAndCertificateGenerator,
			final KeysRepository keysRepository,
			@Value("${key.node.id}")
			final String controlComponentId) {
		this.keysManager = keysManager;
		this.keysAndCertificateGenerator = keysAndCertificateGenerator;
		this.keysRepository = keysRepository;
		this.controlComponentId = controlComponentId;
	}

	/**
	 * Creates the election signing keys.
	 *
	 * @param electionEventId the election event Id for which to get or create the keys
	 * @param validFrom       the start date of validity of the key
	 * @param validTo         the end date of validity of the key
	 * @throws KeyManagementException if there is a problem to get or create the key
	 */
	public ElectionSigningKeys createElectionSigningKeys(final String electionEventId, final ZonedDateTime validFrom, final ZonedDateTime validTo)
			throws KeyManagementException {

		checkNotNull(electionEventId);
		checkNotNull(validFrom);
		checkNotNull(validTo);

		keysManager.throwIfNodeKeysAbsent();

		keysRepository.loadElectionSigningKeys(electionEventId).ifPresent(keys -> {
			throw new IllegalStateException("Election signing keys already created.");
		});

		final ElectionSigningKeys electionSigningkeys;
		try {
			electionSigningkeys = keysAndCertificateGenerator.generateElectionSigningKeys(electionEventId, Date.from(validFrom.toInstant()),
					Date.from(validTo.toInstant()), keysManager.getNodeKeys());
		} catch (final KeyManagementException e) {
			final String errorGeneratingSigningCertificate = String.format(
					"Error generating the Control Component Signing Certificate. [electionEventId=%s, controlComponentId=%s]", electionEventId,
					controlComponentId);

			LOGGER.error(errorGeneratingSigningCertificate);
			throw e;
		}

		final String signingCertificateGenerated = String.format(
				"Control Component Signing Certificate successfully generated. [electionEventId=%s, controlComponentId=%s]", electionEventId,
				controlComponentId);
		LOGGER.info(signingCertificateGenerated);

		keysRepository.saveElectionSigningKeys(electionEventId, electionSigningkeys);

		final String keyPairsGeneratedStored = String.format(
				"Key pair successfully generated and stored. [electionEventId=%s, controlComponentId=%s]", electionEventId, controlComponentId);
		LOGGER.info(keyPairsGeneratedStored);

		return electionSigningkeys;
	}

	@Cacheable("electionSigningKeys")
	public ElectionSigningKeys getElectionSigningKeys(final String electionEventId) throws KeyManagementException {
		checkNotNull(electionEventId);

		keysManager.throwIfNodeKeysAbsent();

		final Optional<ElectionSigningKeys> electionSigningKeys = keysRepository.loadElectionSigningKeys(electionEventId);

		return electionSigningKeys.orElseThrow(() -> new IllegalStateException("Election signing keys missing."));
	}

	/* Wether the election signing key is valid during the whole time period defined by validFrom and validTo*/
	@VisibleForTesting
	boolean isValidInPeriod(final ElectionSigningKeys electionSigningKeys, final Date validFrom, final Date validTo) {
		final X509Certificate certificate = electionSigningKeys.certificate();
		return !certificate.getNotBefore().after(validFrom) && !certificate.getNotAfter().before(validTo);
	}
}
