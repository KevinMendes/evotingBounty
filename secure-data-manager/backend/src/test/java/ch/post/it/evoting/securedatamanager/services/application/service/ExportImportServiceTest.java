/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Scanner;

import org.bouncycastle.cms.CMSException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.util.ReflectionTestUtils;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.cmssigner.CMSSigner;
import ch.post.it.evoting.cryptolib.stores.service.StoresService;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.administrationauthority.AdministrationAuthorityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballottext.BallotTextRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent.ElectionEventRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electoralauthority.ElectoralAuthorityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@ExtendWith(MockitoExtension.class)
class ExportImportServiceTest {

	private static final String USB_DRIVE = "usbDrive";
	private static final String TENANT_ID = "tenantId";
	private static final String EE_ID = "eeid";
	private static final String ALIAS = "alias";
	private static final String EXPORT = "export";
	private static final String DOWNLOADED = "downloaded";
	private static final String DECRYPTED = "decrypted";
	private static final String VOTING_CARD_SETS_FOLDER_NAME = "vcs";

	@InjectMocks
	@Spy
	private final ExportImportService exportImportService = new ExportImportService();

	@Mock
	private PathResolver pathResolver;

	@Mock
	@Qualifier("absolutePathResolver")
	private PathResolver absolutePathResolver;

	@Mock
	private HashService hashService;

	@Mock
	private SignaturesVerifierService signaturesVerifierService;

	@Mock
	private ConsistencyCheckService consistencyCheckService;

	@Mock
	private VotingCardSetRepository votingCardSetRepository;

	@Mock
	private BallotRepository ballotRepository;

	@Mock
	private AdministrationAuthorityRepository administrationAuthorityRepository;

	@Mock
	private ElectionEventRepository electionEventRepository;

	@Mock
	private BallotTextRepository ballotTextRepository;

	@Mock
	private BallotBoxRepository ballotBoxRepository;

	@Mock
	private ElectoralAuthorityRepository electoralAuthorityRepository;

	@Spy
	private StoresService storesService;

	private String votingCardSetJson;
	private String administrationBoardJson;
	private String electionEventJson;
	private String ballotJson;
	private String ballotTextJson;
	private String ballotBoxJson;
	private String electoralAuthorityJson;

	private static Path getPathOfFileInResources(final String path) throws URISyntaxException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL resource = classLoader.getResource(path);
		return Paths.get(resource.toURI());
	}

	@BeforeEach
	public void setUp() {

		ReflectionTestUtils.setField(exportImportService, "tenantId", TENANT_ID);
	}

	@Test
	void exportCACertificates() throws URISyntaxException, IOException {

		Path export = getPathOfFileInResources(EXPORT);
		Path source = export.resolve(DOWNLOADED);
		final Path usbDrive = Files.createTempDirectory(USB_DRIVE);
		when(pathResolver.resolve(Constants.SDM_DIR_NAME)).thenReturn(source);

		when(absolutePathResolver.resolve(eq(USB_DRIVE), anyString())).thenReturn(usbDrive);

		doCallRealMethod().when(exportImportService).exportElectionEventWithoutElectionInformation(anyString(), anyString(), anyString());
		exportImportService.exportElectionEventWithoutElectionInformation(USB_DRIVE, EE_ID, ALIAS);

		assertTrue(checkIfExists(Constants.CONFIG_FILE_NAME_PLATFORM_ROOT_CA, usbDrive));
		assertTrue(checkIfExists(String.format(Constants.CONFIG_FILE_NAME_TENANT_CA_PATTERN, TENANT_ID), usbDrive));

	}

	@Test
	void exportDecryptedBallotBox() throws URISyntaxException, IOException {

		Path export = getPathOfFileInResources(EXPORT);
		Path source = export.resolve(DECRYPTED);
		final Path usbDrive = Files.createTempDirectory(USB_DRIVE);
		when(pathResolver.resolve(Constants.SDM_DIR_NAME)).thenReturn(source);

		when(absolutePathResolver.resolve(eq(USB_DRIVE), anyString())).thenReturn(usbDrive);

		doCallRealMethod().when(exportImportService).exportElectionEventElectionInformation(anyString(), anyString(), anyString());
		exportImportService.exportElectionEventElectionInformation(USB_DRIVE, EE_ID, ALIAS);
		assertFalse(checkIfExists(Constants.CONFIG_FILE_NAME_AUDITABLE_VOTES, usbDrive));

	}

	@Test
	void exportDownloadedBallotBox() throws URISyntaxException, IOException {

		Path export = getPathOfFileInResources(EXPORT);
		Path source = export.resolve(DOWNLOADED);
		final Path usbDrive = Files.createTempDirectory(USB_DRIVE);
		when(pathResolver.resolve(Constants.SDM_DIR_NAME)).thenReturn(source);

		when(absolutePathResolver.resolve(eq(USB_DRIVE), anyString())).thenReturn(usbDrive);

		doCallRealMethod().when(exportImportService).exportElectionEventElectionInformation(anyString(), anyString(), anyString());
		exportImportService.exportElectionEventElectionInformation(USB_DRIVE, EE_ID, ALIAS);

		assertTrue(checkIfExists(Constants.CONFIG_FILE_NAME_ELECTION_INFORMATION_DOWNLOADED_BALLOT_BOX, usbDrive));
	}

	@Test
	void exportPreComputedChoiceCodes() throws URISyntaxException, IOException {
		Path export = getPathOfFileInResources(EXPORT);
		Path source = export.resolve(VOTING_CARD_SETS_FOLDER_NAME);
		when(pathResolver.resolve(any())).thenReturn(source);

		final Path usbDrive = Files.createTempDirectory(USB_DRIVE);
		when(absolutePathResolver.resolve(any())).thenReturn(usbDrive);

		when(votingCardSetRepository.listByElectionEvent(anyString())).thenReturn(getVotingCardSetJson());
		doCallRealMethod().when(exportImportService).exportPreComputedChoiceCodes(anyString(), anyString(), anyString());

		exportImportService.exportPreComputedChoiceCodes(USB_DRIVE, EE_ID, ALIAS);

		assertTrue(checkIfExists(Constants.CONFIG_FILE_NAME_PREFIX_CHOICE_CODE_GENERATION_REQUEST_PAYLOAD + 0
				+ Constants.CONFIG_FILE_NAME_SUFFIX_CHOICE_CODE_GENERATION_REQUEST_PAYLOAD, usbDrive));

		assertFalse(checkIfExists(Constants.CONFIG_VERIFICATION_CARDS_KEY_PAIR_DIRECTORY, usbDrive));
		assertFalse(checkIfExists(Constants.CONFIG_VERIFICATION_CARDS_KEY_PAIR_DIRECTORY + Constants.CSV, usbDrive));
	}

	@Test
	void doSignaturesInExport()
			throws UnrecoverableKeyException, NoSuchAlgorithmException, IOException, GeneralCryptoLibException, KeyStoreException, CMSException,
			URISyntaxException {
		final Path usbDrive = Files.createTempDirectory(USB_DRIVE);
		final Path dbDump = Files.createTempFile(usbDrive, "db_dump", ".json");
		final Path electionsConfig = Files.createTempFile(usbDrive, "elections_config", ".json");
		Path keyStoreOnline = getPathOfFileInResources(EXPORT).resolve(Constants.INTEGRATION_KEYSTORE_ONLINE_FILE);

		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, Constants.INTEGRATION_KEYSTORE_ONLINE_FILE)).thenReturn(keyStoreOnline);
		when(pathResolver.resolve(Constants.SDM_DIR_NAME, Constants.DBDUMP_FILE_NAME)).thenReturn(dbDump);
		when(pathResolver.resolve(Constants.SDM_DIR_NAME, Constants.SDM_CONFIG_DIR_NAME, Constants.CONFIG_FILE_NAME_ELECTIONS_CONFIG_JSON))
				.thenReturn(electionsConfig);
		exportImportService.signDumpDatabaseAndElectionsConfig(new char[] { '2', '2', '2', '2', '2', '2' });

		assertTrue(checkIfExists(dbDump.getFileName().toString() + CMSSigner.SIGNATURE_FILE_EXTENSION, usbDrive),
				"Check if exists db_dump.json.p7 in temporary test folder");
		assertTrue(checkIfExists(electionsConfig.getFileName().toString() + CMSSigner.SIGNATURE_FILE_EXTENSION, usbDrive),
				"Check if exists elections_config.json.p7 in temporary test folder");
	}

	@Test
	void dumpDatabase() throws URISyntaxException, IOException, ResourceNotFoundException {
		Path export = getPathOfFileInResources(EXPORT);
		when(pathResolver.resolve(any())).thenReturn(export.resolve(Constants.DBDUMP_FILE_NAME));

		when(administrationAuthorityRepository.list()).thenReturn(getAdministrationBoardJson());
		when(electionEventRepository.find(anyString())).thenReturn(getElectionEventJson());
		when(ballotRepository.listByElectionEvent(anyString())).thenReturn(getBallotJson());
		when(ballotTextRepository.list(any())).thenReturn(getBallotTextJson());
		when(ballotBoxRepository.listByElectionEvent(anyString())).thenReturn(getBallotBoxJson());
		when(electoralAuthorityRepository.listByElectionEvent(anyString())).thenReturn(getElectoralAuthorityJson());
		when(votingCardSetRepository.listByElectionEvent(anyString())).thenReturn(getVotingCardSetJson());

		doCallRealMethod().when(exportImportService).dumpDatabase(anyString());

		exportImportService.dumpDatabase(EE_ID);

		assertTrue(checkIfExists(Constants.DBDUMP_FILE_NAME, export), "db_dump.json not found after generation");
	}

	@Test
	void verifySignaturesOnImport() throws CertificateException, GeneralCryptoLibException, CMSException, IOException, URISyntaxException {
		Path export = getPathOfFileInResources(EXPORT);
		when(pathResolver.resolve(any())).thenReturn(export);

		doCallRealMethod().when(exportImportService).verifySignaturesOnImport();
		exportImportService.verifySignaturesOnImport();
	}

	private boolean checkIfExists(String fileName, Path source) throws IOException {

		return Files.walk(source, 20).anyMatch(path -> path.getFileName().toString().equals(fileName));
	}

	/**
	 * Returns a sample voting card set JSON string.
	 */
	private String getVotingCardSetJson() throws IOException {
		if (null == votingCardSetJson) {
			votingCardSetJson = readJson("votingCardSet.json");
		}

		return votingCardSetJson;
	}

	private String getAdministrationBoardJson() throws IOException {
		if (null == administrationBoardJson) {
			administrationBoardJson = readJson("administrationAuthorities.json");
		}

		return administrationBoardJson;
	}

	private String getElectionEventJson() throws IOException {
		if (null == electionEventJson) {
			electionEventJson = readJson("electionEventRepository.json");
		}

		return electionEventJson;
	}

	private String getBallotJson() throws IOException {
		if (null == ballotJson) {
			ballotJson = readJson("ballot_result_with_representations.json");
		}

		return ballotJson;
	}

	private String getBallotTextJson() throws IOException {
		if (null == ballotTextJson) {
			ballotTextJson = readJson("ballotText_result.json");
		}

		return ballotTextJson;
	}

	private String getBallotBoxJson() throws IOException {
		if (null == ballotBoxJson) {
			ballotBoxJson = readJson("ballotBox_result.json");
		}

		return ballotBoxJson;
	}

	private String getElectoralAuthorityJson() throws IOException {
		if (null == electoralAuthorityJson) {
			electoralAuthorityJson = readJson("electoralAuthority_result.json");
		}

		return electoralAuthorityJson;
	}

	private String readJson(String fileName) throws IOException {
		try (InputStream is = getClass().getResourceAsStream("/" + fileName); Scanner scanner = new Scanner(is, "UTF-8")) {
			return scanner.useDelimiter("\\A").next();
		}
	}
}
