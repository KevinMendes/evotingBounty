/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;

import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignerDigestMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.configuration.ControlComponentPublicKeysService;
import ch.post.it.evoting.securedatamanager.integration.plugin.ExecutionListener;
import ch.post.it.evoting.securedatamanager.integration.plugin.KeyParameter;
import ch.post.it.evoting.securedatamanager.integration.plugin.Parameters;
import ch.post.it.evoting.securedatamanager.integration.plugin.PhaseName;
import ch.post.it.evoting.securedatamanager.integration.plugin.PluginSequenceResolver;
import ch.post.it.evoting.securedatamanager.integration.plugin.Plugins;
import ch.post.it.evoting.securedatamanager.integration.plugin.SequentialExecutor;
import ch.post.it.evoting.securedatamanager.integration.plugin.XmlObjectsLoader;
import ch.post.it.evoting.securedatamanager.services.application.exception.CCKeysNotExistException;
import ch.post.it.evoting.securedatamanager.services.application.exception.ConsistencyCheckException;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.application.service.ElectionEventService;
import ch.post.it.evoting.securedatamanager.services.application.service.ExportImportService;
import ch.post.it.evoting.securedatamanager.services.domain.model.operation.OperationResult;
import ch.post.it.evoting.securedatamanager.services.domain.model.operation.OperationsData;
import ch.post.it.evoting.securedatamanager.services.domain.model.operation.OperationsOutputCode;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;

import io.jsonwebtoken.SignatureException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/sdm-backend/operation")
@Api(value = "SDM Operations REST API")
public class OperationsController {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationsController.class);
	private static final String REQUEST_CAN_NOT_BE_PERFORMED = "The request can not be performed for the current resource";
	private static final String PATH_REQUIRED = "path parameter is required";
	private static final String PLUGIN_FILE_NAME = "plugin.xml";
	private final PathResolver pathResolver;
	private final SequentialExecutor sequentialExecutor;
	private final ExportImportService exportImportService;
	private final ElectionEventService electionEventService;
	private final ControlComponentPublicKeysService controlComponentPublicKeysService;

	public OperationsController(
			final PathResolver pathResolver,
			final SequentialExecutor sequentialExecutor,
			final ExportImportService exportImportService,
			final ElectionEventService electionEventService,
			final ControlComponentPublicKeysService controlComponentPublicKeysService) {
		this.pathResolver = pathResolver;
		this.sequentialExecutor = sequentialExecutor;
		this.exportImportService = exportImportService;
		this.electionEventService = electionEventService;
		this.controlComponentPublicKeysService = controlComponentPublicKeysService;
	}

	@PostMapping(value = "/generate-pre-voting-outputs/{electionEventId}")
	@ApiOperation(value = "Export operation service")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Not Found"), @ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<OperationResult> generatePreVotingOutputsOperation(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final
			String electionEventId,
			@RequestBody
			final OperationsData request) {

		validateUUID(electionEventId);

		if (StringUtils.isEmpty(request.getPrivateKeyInBase64())) {
			throw new IllegalArgumentException("PrivateKey parameter is required");
		}

		final Parameters parameters = buildParameters(electionEventId, request.getPrivateKeyInBase64(), "");
		return executeOperationForPhase(parameters, PhaseName.GENERATE_PRE_VOTING_OUTPUTS, true);

	}

	@PostMapping(value = "/generate-post-voting-outputs/{electionEventId}/{ballotBoxStatus}")
	@ApiOperation(value = "Export operation service")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Not Found"), @ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<OperationResult> generatePostVotingOutputsOperation(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final
			String electionEventId,
			@ApiParam(value = "String", required = true)
			@PathVariable
			final
			String ballotBoxStatus,
			@RequestBody
			final OperationsData request) {

		validateUUID(electionEventId);

		if (StringUtils.isEmpty(request.getPrivateKeyInBase64())) {
			throw new IllegalArgumentException("PrivateKey parameter is required");
		}
		final PhaseName phaseName;
		final Parameters parameters;
		try {
			final Status status = Status.valueOf(ballotBoxStatus.toUpperCase());
			phaseName = determinePhase(status);
			if (phaseName == null) {
				return handleException(OperationsOutputCode.ERROR_STATUS_NAME);
			}

			parameters = buildParameters(electionEventId, request.getPrivateKeyInBase64(), "");
		} catch (final InvalidParameterException e) {
			final int errorCode = OperationsOutputCode.MISSING_PARAMETER.value();
			return handleInvalidParamException(e, errorCode);
		} catch (final Exception e) {
			final int errorCode = OperationsOutputCode.ERROR_STATUS_NAME.value();
			return handleException(e, errorCode);
		}

		return executeOperationForPhase(parameters, phaseName, true);
	}

	private PhaseName determinePhase(final Status status) {
		PhaseName phaseName = null;
		switch (status) {
		case BB_DOWNLOADED:
			phaseName = PhaseName.DOWNLOAD;
			break;
		case DECRYPTED:
			phaseName = PhaseName.DECRYPTION;
			break;
		default:
			break;
		}
		return phaseName;
	}

	@PostMapping(value = "/export/{electionEventId}")
	@ApiOperation(value = "Export operation service")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Not Found"), @ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<OperationResult> exportOperation(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String electionEventId,
			@RequestBody
			final OperationsData request) {
		try {
			validateUUID(electionEventId);

			if (StringUtils.isEmpty(request.getPath())) {
				throw new ResourceNotFoundException(PATH_REQUIRED);
			}

			final String eeAlias = getEeAlias(electionEventId);

			exportImportService.dumpDatabase(electionEventId);

			exportImportService.signDumpDatabaseAndElectionsConfig(request.getPassword());

			if (request.isElectionEventData()) {
				exportImportService.exportElectionEventWithoutElectionInformation(request.getPath(), electionEventId, eeAlias);
				exportImportService.exportElectionEventElectionInformation(request.getPath(), electionEventId, eeAlias);
			}

			if (request.isVotingCardsData()) {
				exportImportService.exportVotingCards(request.getPath(), electionEventId, eeAlias);
			}

			if (request.isCustomerData()) {
				exportImportService.exportCustomerSpecificData(request.getPath(), electionEventId, eeAlias);
			}

			if (request.isComputedChoiceCodes()) {
				exportImportService.exportComputedChoiceCodes(request.getPath(), electionEventId, eeAlias);
			}

			if (request.isPreComputedChoiceCodes()) {
				exportImportService.exportPreComputedChoiceCodes(request.getPath(), electionEventId, eeAlias);
			}

			if (request.isBallotBoxes()) {
				exportImportService.exportBallotBoxes(request.getPath(), electionEventId, eeAlias);
			}

			if (request.isElectionEventContextAndControlComponentKeys()) {
				exportImportService.exportElectionEventContextAndControlComponentKeys(request.getPath(), electionEventId, eeAlias);
			}

		} catch (final InvalidParameterException e) {
			final OperationsOutputCode code = OperationsOutputCode.MISSING_PARAMETER;
			return handleInvalidParamException(e, code.value());
		} catch (final ResourceNotFoundException e) {
			final OperationsOutputCode code = OperationsOutputCode.MISSING_PARAMETER;
			return handleException(e, code.value());
		} catch (final IOException e) {
			final OperationsOutputCode code = OperationsOutputCode.ERROR_IO_OPERATIONS;
			return handleException(e, code.value());
		} catch (final CMSSignerDigestMismatchException e) {
			final OperationsOutputCode code = OperationsOutputCode.SIGNATURE_VERIFICATION_FAILED;
			return handleException(e, code.value());
		} catch (final CMSException e) {
			final OperationsOutputCode code = OperationsOutputCode.ERROR_SIGNING_OPERATIONS;
			return handleException(e, code.value());
		} catch (final GeneralCryptoLibException e) {
			final OperationsOutputCode code = OperationsOutputCode.KEYSTORE_READING_FAILED;
			return handleException(e, code.value());
		} catch (final CCKeysNotExistException e) {
			final OperationsOutputCode code = OperationsOutputCode.MISSING_CONTROL_COMPONENTS_KEYS;
			return handleException(e, code.value());
		} catch (final Exception e) {
			final OperationsOutputCode code = OperationsOutputCode.GENERAL_ERROR;
			return handleException(e, code.value());
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private String getEeAlias(final String electionEventId) throws ResourceNotFoundException, CCKeysNotExistException {

		// Check if Control Components keys exists
		if (!controlComponentPublicKeysService.exist(electionEventId)) {
			throw new CCKeysNotExistException(
					String.format("The Control Components keys do not exist for this election event. [electionEventId: %s]", electionEventId));
		}

		final String electionEventAlias = electionEventService.getElectionEventAlias(electionEventId);
		if (StringUtils.isBlank(electionEventAlias)) {
			throw new InvalidParameterException("Invalid Election Event Id: " + electionEventId);
		}

		return electionEventAlias;
	}

	@PostMapping(value = "/import")
	@ApiOperation(value = "Import operation service")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Not Found"), @ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<OperationResult> importOperation(
			@RequestBody
			final OperationsData request) {

		if (StringUtils.isEmpty(request.getPath())) {
			return handleIncompleteRequest();
		}

		try {
			exportImportService.importData(request.getPath());
			exportImportService.verifySignaturesOnImport();
			exportImportService.importDatabase();

		} catch (final IOException e) {
			final OperationsOutputCode code = OperationsOutputCode.ERROR_IO_OPERATIONS;
			return handleException(e, code.value());
		} catch (final InvalidParameterException e) {
			final OperationsOutputCode code = OperationsOutputCode.MISSING_PARAMETER;
			return handleInvalidParamException(e, code.value());
		} catch (final CMSException | SignatureException e) {
			final OperationsOutputCode code = OperationsOutputCode.SIGNATURE_VERIFICATION_FAILED;
			return handleException(e, code.value());
		} catch (final GeneralCryptoLibException e) {
			final OperationsOutputCode code = OperationsOutputCode.CHAIN_VALIDATION_FAILED;
			return handleException(e, code.value());
		} catch (final CertificateException e) {
			final OperationsOutputCode code = OperationsOutputCode.ERROR_CERTIFICATE_PARSING;
			return handleException(e, code.value());
		} catch (final ConsistencyCheckException e) {
			final OperationsOutputCode code = OperationsOutputCode.CONSISTENCY_ERROR;
			return handleException(e, code.value());
		} catch (final Exception e) {
			final OperationsOutputCode code = OperationsOutputCode.GENERAL_ERROR;
			return handleException(e, code.value());
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	protected List<String> getCommands(final PhaseName phaseName) throws IOException, JAXBException, SAXException, XMLStreamException {

		Path pluginXmlPath = pathResolver.resolve(Constants.SDM_DIR_NAME).resolve(PLUGIN_FILE_NAME);
		if (!pluginXmlPath.toFile().exists()) {
			pluginXmlPath = pathResolver.resolve(Constants.SDM_DIR_NAME).resolve(Constants.SDM_CONFIG_DIR_NAME).resolve(PLUGIN_FILE_NAME);
			if (!pluginXmlPath.toFile().exists()) {
				LOGGER.error("The plugin.xml file is not found");
				return new ArrayList<>();
			}
		}

		final Plugins plugins = XmlObjectsLoader.unmarshal(pluginXmlPath);
		final PluginSequenceResolver pluginSequence = new PluginSequenceResolver(plugins);
		return pluginSequence.getActionsForPhase(phaseName);
	}

	private ResponseEntity<OperationResult> executeOperationForPhase(final Parameters parameters, final PhaseName phaseName,
			final boolean failOnEmptyCommandsForPhase) {
		try {
			final List<String> commandsForPhase = getCommands(phaseName);

			if (failOnEmptyCommandsForPhase && commandsForPhase.isEmpty()) {
				return handleException(OperationsOutputCode.MISSING_COMMANDS_FOR_PHASE);
			}

			final ExecutionListener listener = new ExecutionListener();
			sequentialExecutor.execute(commandsForPhase, parameters, listener);

			if (listener.getError() != 0) {
				return handleException(listener);
			}

		} catch (final IOException e) {
			final int errorCode = OperationsOutputCode.ERROR_IO_OPERATIONS.value();
			return handleException(e, errorCode);
		} catch (final JAXBException | SAXException e) {
			final int errorCode = OperationsOutputCode.ERROR_PARSING_FILE.value();
			return handleException(e, errorCode);
		} catch (final Exception e) {
			final int errorCode = OperationsOutputCode.GENERAL_ERROR.value();
			return handleException(e, errorCode);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private Parameters buildParameters(final String electionEventId, final String privateKeyInBase64, final String path) {
		final Parameters parameters = new Parameters();

		if (StringUtils.isNotEmpty(electionEventId)) {
			final String electionEventAlias = electionEventService.getElectionEventAlias(electionEventId);
			if (StringUtils.isBlank(electionEventAlias)) {
				throw new InvalidParameterException("Invalid Election Event Id: " + electionEventId);
			}
			parameters.addParam(KeyParameter.EE_ALIAS.name(), electionEventAlias);
			parameters.addParam(KeyParameter.EE_ID.name(), electionEventId);
		}
		final Path sdmPath = pathResolver.resolve(Constants.SDM_DIR_NAME);
		parameters.addParam(KeyParameter.SDM_PATH.name(), sdmPath.toString().replace("\\", "/"));
		if (StringUtils.isNotEmpty(privateKeyInBase64)) {
			parameters.addParam(KeyParameter.PRIVATE_KEY.name(), privateKeyInBase64);
		}
		if (StringUtils.isNotEmpty(path)) {
			parameters.addParam(KeyParameter.USB_LETTER.name(), path.replace("\\", "/"));
		}
		return parameters;
	}

	private ResponseEntity<OperationResult> handleIncompleteRequest() {
		LOGGER.error("{}: {}", OperationsOutputCode.MISSING_PARAMETER.getReasonPhrase(), PATH_REQUIRED);
		final OperationResult output = new OperationResult();
		output.setError(OperationsOutputCode.MISSING_PARAMETER.value());
		output.setMessage(PATH_REQUIRED);
		return new ResponseEntity<>(output, HttpStatus.BAD_REQUEST);
	}

	private ResponseEntity<OperationResult> handleException(final Exception e, final int errorCode) {
		LOGGER.error("{}{}", REQUEST_CAN_NOT_BE_PERFORMED, errorCode, e);
		final OperationResult output = new OperationResult();
		output.setError(errorCode);
		output.setException(e.getClass().getName());
		output.setMessage(e.getMessage());
		return new ResponseEntity<>(output, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private ResponseEntity<OperationResult> handleException(final int error, final String message) {
		LOGGER.error("{}{}: {}", REQUEST_CAN_NOT_BE_PERFORMED, error, message);
		final OperationResult output = new OperationResult();
		output.setError(error);
		output.setMessage(message);
		return new ResponseEntity<>(output, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private ResponseEntity<OperationResult> handleInvalidParamException(final Exception e, final int errorCode) {
		LOGGER.error("{}{}", REQUEST_CAN_NOT_BE_PERFORMED, errorCode, e);
		final OperationResult output = new OperationResult();
		output.setError(errorCode);
		output.setException(e.getClass().getName());
		output.setMessage(e.getMessage());
		return new ResponseEntity<>(output, HttpStatus.NOT_FOUND);
	}

	private ResponseEntity<OperationResult> handleException(final ExecutionListener listener) {
		return handleException(listener.getError(), listener.getMessage());
	}

	private ResponseEntity<OperationResult> handleException(final OperationsOutputCode operationsOutputCode) {
		return handleException(operationsOutputCode.value(), operationsOutputCode.getReasonPhrase());
	}

}
