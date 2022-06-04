/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static java.nio.file.Files.newDirectoryStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetUploadRepository;

/**
 * Uploads the signed extendedAuthentication information
 */
@Service
public class ExtendedAuthenticationUploadService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedAuthenticationUploadService.class);

	@Autowired
	SignatureService signatureService;

	@Autowired
	VotingCardSetUploadRepository votingCardSetUploadRepository;

	/**
	 * Uploads the extended authentication information belonging to a voting card set
	 *
	 * @param extAuthPath
	 * @param electionEventId
	 * @param adminBoardId
	 * @throws IOException
	 */
	public void uploadExtendedAuthenticationFiles(final Path extAuthPath, final String electionEventId, final String adminBoardId)
			throws IOException {

		try (final DirectoryStream<Path> files = newDirectoryStream(extAuthPath, Constants.CSV_GLOB)) {
			for (final Path file : files) {
				final String name = file.getFileName().toString();
				if (name.startsWith(Constants.CONFIG_FILE_EXTENDED_AUTHENTICATION_DATA)) {
					LOGGER.info("Uploading extended authentication file and its signature {}", name);
					uploadExtendedAuthentication(electionEventId, adminBoardId, file);
				}
			}
		}
	}

	private void uploadExtendedAuthentication(final String electionEventId, final String adminBoardId, final Path filePath) throws IOException {

		try (final InputStream stream = signatureService.newCSVAndSignatureInputStream(filePath)) {
			votingCardSetUploadRepository.uploadExtendedAuthData(electionEventId, adminBoardId, stream);
		}

	}
}
