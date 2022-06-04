/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.bouncycastle.cms.CMSException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.xml.sax.SAXException;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.configuration.ControlComponentPublicKeysService;
import ch.post.it.evoting.securedatamanager.integration.plugin.ExecutionListener;
import ch.post.it.evoting.securedatamanager.integration.plugin.ResultCode;
import ch.post.it.evoting.securedatamanager.integration.plugin.SequentialExecutor;
import ch.post.it.evoting.securedatamanager.services.application.exception.ConsistencyCheckException;
import ch.post.it.evoting.securedatamanager.services.application.service.ElectionEventService;
import ch.post.it.evoting.securedatamanager.services.application.service.ExportImportService;
import ch.post.it.evoting.securedatamanager.services.domain.model.operation.OperationResult;
import ch.post.it.evoting.securedatamanager.services.domain.model.operation.OperationsData;
import ch.post.it.evoting.securedatamanager.services.domain.model.operation.OperationsOutputCode;

@ExtendWith(MockitoExtension.class)
class OperationsControllerTest {

	private static final String ERROR = "Error";
	private static final String TEST_PATH = "testPath";
	private static final String BB_DOWNLOADED = "bb_downloaded";
	private static final String ELECTION_EVENT_ALIAS = "eeAlias";
	private static final String BALLOT_BOX_STATUS = "ballotBoxStatus";
	private static final String BASE64_PRIVATE_KEY = "NDQ0NDQ0NDQ0NDQ0NA";
	private static final String INVALID_ELECTION_EVENT_ID = "invalid-ee-id";
	private static final String ELECTION_EVENT_ID = "a9d805a0d9ef4deeb4b14838f7e8ed8a";

	private final List<String> result = Collections.singletonList("command");

	@Mock
	private Path pathMock;
	@Mock
	private PathResolver pathResolver;
	@Mock
	private SequentialExecutor sequentialExecutor;
	@Mock
	private ExportImportService exportImportService;
	@Mock
	private ElectionEventService electionEventService;
	@Mock
	private ControlComponentPublicKeysService controlComponentPublicKeysService;

	@Spy
	@InjectMocks
	private OperationsController operationsController;

	@Test
	void testExportOperationPathParameterIsRequired() {
		final OperationsData requestBody = new OperationsData();

		final ResponseEntity<OperationResult> exportOperation = operationsController.exportOperation(ELECTION_EVENT_ID, requestBody);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exportOperation.getStatusCode());
		assertEquals(OperationsOutputCode.MISSING_PARAMETER.value(), exportOperation.getBody().getError());
	}

	@Test
	void testExportOperationElectionEventNotFound() {
		final String electionEventId = "17ccbe962cf341bc93208c26e911090c";
		final OperationsData requestBody = new OperationsData();
		requestBody.setPath(TEST_PATH);

		when(controlComponentPublicKeysService.exist(anyString())).thenReturn(true);

		final ResponseEntity<OperationResult> exportOperation = operationsController.exportOperation(electionEventId, requestBody);

		assertEquals(HttpStatus.NOT_FOUND, exportOperation.getStatusCode());
		assertEquals(OperationsOutputCode.MISSING_PARAMETER.value(), exportOperation.getBody().getError());
	}

	@Test
	void testExportOperationEmptyPathThrows() {
		final OperationsData requestBody = new OperationsData();
		requestBody.setPath("");
		final ResponseEntity<OperationResult> exportOperation = operationsController.exportOperation(ELECTION_EVENT_ID, requestBody);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exportOperation.getStatusCode());
		assertEquals(OperationsOutputCode.MISSING_PARAMETER.value(), exportOperation.getBody().getError());
	}

	@Test
	void testExportOperationCCKeysMissing() {
		when(controlComponentPublicKeysService.exist(anyString())).thenReturn(false);

		final OperationsData requestBody = new OperationsData();
		requestBody.setPath(TEST_PATH);
		final ResponseEntity<OperationResult> exportOperation = operationsController.exportOperation(ELECTION_EVENT_ID, requestBody);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exportOperation.getStatusCode());
		assertEquals(OperationsOutputCode.MISSING_CONTROL_COMPONENTS_KEYS.value(), exportOperation.getBody().getError());
	}

	@Test
	void testExportOperationWhenExportElectionEventError() throws IOException {
		final OperationsData requestBody = new OperationsData();
		requestBody.setPath(TEST_PATH);
		requestBody.setElectionEventData(true);

		when(controlComponentPublicKeysService.exist(anyString())).thenReturn(true);

		doThrow(new IOException("Export Election Event Error")).when(exportImportService)
				.exportElectionEventWithoutElectionInformation(anyString(), anyString(), anyString());

		when(electionEventService.getElectionEventAlias(anyString())).thenReturn("electionEventAlias");

		final ResponseEntity<OperationResult> exportOperation = operationsController.exportOperation(ELECTION_EVENT_ID, requestBody);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exportOperation.getStatusCode());
		assertEquals(OperationsOutputCode.ERROR_IO_OPERATIONS.value(), exportOperation.getBody().getError());
	}

	@Test
	void testExportOperationHappyPath() {
		final OperationsData requestBody = new OperationsData();
		requestBody.setPath(TEST_PATH);

		when(controlComponentPublicKeysService.exist(anyString())).thenReturn(true);
		when(electionEventService.getElectionEventAlias(ELECTION_EVENT_ID)).thenReturn(ELECTION_EVENT_ALIAS);

		final ResponseEntity<OperationResult> exportOperation = operationsController.exportOperation(ELECTION_EVENT_ID, requestBody);

		assertEquals(HttpStatus.OK, exportOperation.getStatusCode());
		assertNull(exportOperation.getBody());
	}

	@Test
	void testGeneratePreVotingOutputsOperationPrivateKeyInBase64Missing() {
		final OperationsData requestBody = new OperationsData();

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> operationsController.generatePreVotingOutputsOperation(ELECTION_EVENT_ID, requestBody));
		assertEquals("PrivateKey parameter is required", exception.getMessage());
	}

	@Test
	void testGeneratePreVotingOutputsOperationWhenCallbackError() throws XMLStreamException, JAXBException, IOException, SAXException {
		doReturn(result).when(operationsController).getCommands(any());
		final OperationsData requestBody = new OperationsData();
		requestBody.setPrivateKeyInBase64(BASE64_PRIVATE_KEY);

		when(electionEventService.getElectionEventAlias(ELECTION_EVENT_ID)).thenReturn(ELECTION_EVENT_ALIAS);
		when(pathResolver.resolve(anyString())).thenReturn(pathMock);

		doAnswer((Answer<ExecutionListener>) invocation -> {
			final Object[] args = invocation.getArguments();
			final ExecutionListener callback = (ExecutionListener) args[2];
			callback.onError(ResultCode.GENERAL_ERROR.value());
			callback.onMessage(ERROR);
			return null;
		}).when(sequentialExecutor).execute(any(), any(), any(ExecutionListener.class));

		final ResponseEntity<OperationResult> preVotingOutputs = operationsController.generatePreVotingOutputsOperation(ELECTION_EVENT_ID,
				requestBody);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, preVotingOutputs.getStatusCode());
		assertEquals(ResultCode.GENERAL_ERROR.value(), preVotingOutputs.getBody().getError());
	}

	@Test
	void testGeneratePreVotingOutputsHappyPath() throws XMLStreamException, JAXBException, IOException, SAXException {
		doReturn(result).when(operationsController).getCommands(any());
		final OperationsData requestBody = new OperationsData();
		requestBody.setPrivateKeyInBase64(BASE64_PRIVATE_KEY);

		when(electionEventService.getElectionEventAlias(ELECTION_EVENT_ID)).thenReturn(ELECTION_EVENT_ALIAS);
		when(pathResolver.resolve(anyString())).thenReturn(pathMock);

		final ResponseEntity<OperationResult> preVotingOutputs = operationsController.generatePreVotingOutputsOperation(ELECTION_EVENT_ID,
				requestBody);

		assertEquals(HttpStatus.OK, preVotingOutputs.getStatusCode());
		assertNull(preVotingOutputs.getBody());
	}

	@Test
	void testGeneratePostVotingOutputsOperationPrivateKeyInBase64Missing() {
		final OperationsData requestBody = new OperationsData();

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> operationsController.generatePostVotingOutputsOperation(ELECTION_EVENT_ID, BALLOT_BOX_STATUS, requestBody));
		assertEquals("PrivateKey parameter is required", exception.getMessage());
	}

	@Test
	void testGeneratePostVotingOutputsOperationErrorStatusNameNoEnumConstant() {
		final OperationsData requestBody = new OperationsData();
		requestBody.setPrivateKeyInBase64(BASE64_PRIVATE_KEY);

		final ResponseEntity<OperationResult> postVotingOutputs = operationsController
				.generatePostVotingOutputsOperation(ELECTION_EVENT_ID, BALLOT_BOX_STATUS, requestBody);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, postVotingOutputs.getStatusCode());
		assertEquals(OperationsOutputCode.ERROR_STATUS_NAME.value(), postVotingOutputs.getBody().getError());
	}

	@Test
	void testGeneratePostVotingOutputsOperationStatusNameErrorPhaseNameNull() {
		final String ballotBoxStatus = "new";
		final OperationsData requestBody = new OperationsData();
		requestBody.setPrivateKeyInBase64(BASE64_PRIVATE_KEY);

		final ResponseEntity<OperationResult> postVotingOutputs = operationsController
				.generatePostVotingOutputsOperation(ELECTION_EVENT_ID, ballotBoxStatus, requestBody);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, postVotingOutputs.getStatusCode());
		assertEquals(OperationsOutputCode.ERROR_STATUS_NAME.value(), postVotingOutputs.getBody().getError());
	}

	@Test
	void testGeneratePostVotingOutputsOperationWhenCallbackError() throws XMLStreamException, JAXBException, IOException, SAXException {
		doReturn(result).when(operationsController).getCommands(any());
		final OperationsData requestBody = new OperationsData();
		requestBody.setPrivateKeyInBase64(BASE64_PRIVATE_KEY);

		when(electionEventService.getElectionEventAlias(ELECTION_EVENT_ID)).thenReturn(ELECTION_EVENT_ALIAS);
		when(pathResolver.resolve(anyString())).thenReturn(pathMock);

		doAnswer((Answer<ExecutionListener>) invocation -> {
			final Object[] args = invocation.getArguments();
			final ExecutionListener callback = (ExecutionListener) args[2];
			callback.onError(ResultCode.GENERAL_ERROR.value());
			callback.onMessage(ERROR);
			return null;
		}).when(sequentialExecutor).execute(any(), any(), any(ExecutionListener.class));

		final ResponseEntity<OperationResult> postVotingOutputs = operationsController
				.generatePostVotingOutputsOperation(ELECTION_EVENT_ID, BB_DOWNLOADED, requestBody);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, postVotingOutputs.getStatusCode());
		assertEquals(ResultCode.GENERAL_ERROR.value(), postVotingOutputs.getBody().getError());
	}

	@Test
	void testGeneratePostVotingOutputsOperationHappyPath() throws XMLStreamException, JAXBException, IOException, SAXException {
		doReturn(result).when(operationsController).getCommands(any());
		final OperationsData requestBody = new OperationsData();
		requestBody.setPrivateKeyInBase64(BASE64_PRIVATE_KEY);

		when(electionEventService.getElectionEventAlias(ELECTION_EVENT_ID)).thenReturn(ELECTION_EVENT_ALIAS);
		when(pathResolver.resolve(anyString())).thenReturn(pathMock);

		final ResponseEntity<OperationResult> postVotingOutputs = operationsController
				.generatePostVotingOutputsOperation(ELECTION_EVENT_ID, BB_DOWNLOADED, requestBody);

		assertEquals(HttpStatus.OK, postVotingOutputs.getStatusCode());
		assertNull(postVotingOutputs.getBody());
	}

	@Test
	void testGeneratePostVotingOutputsOperationInvalidEEId() {
		final OperationsData requestBody = new OperationsData();
		requestBody.setPrivateKeyInBase64(BASE64_PRIVATE_KEY);

		final ResponseEntity<OperationResult> postVotingOutputs = operationsController
				.generatePostVotingOutputsOperation(ELECTION_EVENT_ID, BB_DOWNLOADED, requestBody);

		assertEquals(HttpStatus.NOT_FOUND, postVotingOutputs.getStatusCode());
		assertEquals(OperationsOutputCode.MISSING_PARAMETER.value(), postVotingOutputs.getBody().getError());
	}

	@Test
	void testImportOperationPathParameterMissing() {
		final OperationsData requestBody = new OperationsData();

		final ResponseEntity<OperationResult> importOperation = operationsController.importOperation(requestBody);

		assertEquals(HttpStatus.BAD_REQUEST, importOperation.getStatusCode());
		assertEquals(OperationsOutputCode.MISSING_PARAMETER.value(), importOperation.getBody().getError());
	}

	@Test
	void testImportOperationLoadDatabaseError()
			throws IOException, CertificateException, ConsistencyCheckException, GeneralCryptoLibException, CMSException {
		final OperationsData requestBody = new OperationsData();
		requestBody.setPath(TEST_PATH);

		doThrow(new IOException(ERROR)).when(exportImportService).importDatabase();

		final ResponseEntity<OperationResult> importOperation = operationsController.importOperation(requestBody);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, importOperation.getStatusCode());
		assertEquals(OperationsOutputCode.ERROR_IO_OPERATIONS.value(), importOperation.getBody().getError());
	}

	@Test
	void testImportOperationWhenCallBackError() throws IOException {
		final OperationsData requestBody = new OperationsData();
		requestBody.setPath(TEST_PATH);

		doThrow(new IOException(ERROR)).when(exportImportService).importData(anyString());

		final ResponseEntity<OperationResult> importOpertaion = operationsController.importOperation(requestBody);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, importOpertaion.getStatusCode());
		assertEquals(OperationsOutputCode.ERROR_IO_OPERATIONS.value(), importOpertaion.getBody().getError());
	}

	@Test
	void testImportOperationConsistencyCheckError()
			throws CertificateException, ConsistencyCheckException, GeneralCryptoLibException, CMSException, IOException {
		final OperationsData requestBody = new OperationsData();
		requestBody.setPath(TEST_PATH);

		doThrow(new ConsistencyCheckException("Encryption parameters consistency check between election event data and signed jwt failed."))
				.when(exportImportService).importDatabase();

		final ResponseEntity<OperationResult> importOpertaion = operationsController.importOperation(requestBody);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, importOpertaion.getStatusCode());
		assertEquals(OperationsOutputCode.CONSISTENCY_ERROR.value(), importOpertaion.getBody().getError());
	}

	@Test
	void testImportOperationHappyPath() {
		final OperationsData requestBody = new OperationsData();
		requestBody.setPath(TEST_PATH);

		final ResponseEntity<OperationResult> importOpertaion = operationsController.importOperation(requestBody);

		assertEquals(HttpStatus.OK, importOpertaion.getStatusCode());
		assertNull(importOpertaion.getBody());
	}

	@Test
	void generatePreVotingOutputsOperationWrongElectionEventId() {
		final OperationsData operationsData = new OperationsData();
		assertThrows(FailedValidationException.class, () -> operationsController.generatePreVotingOutputsOperation("34dfasG3", operationsData));
	}

	@Test
	void generatePreVotingOutputsOperationEmptyElectionEventId() {
		final OperationsData operationsData = new OperationsData();
		assertThrows(FailedValidationException.class, () -> operationsController.generatePreVotingOutputsOperation("", operationsData));
	}

	@Test
	void generatePostVotingOutputsOperationWrongElectionEventId() {
		final OperationsData operationsData = new OperationsData();
		assertThrows(FailedValidationException.class,
				() -> operationsController.generatePostVotingOutputsOperation(INVALID_ELECTION_EVENT_ID, BALLOT_BOX_STATUS, operationsData));
	}

}
