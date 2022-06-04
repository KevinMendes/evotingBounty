/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SharesException;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.application.service.AdminBoardService;
import ch.post.it.evoting.securedatamanager.services.domain.model.administrationauthority.ActivateOutputData;
import ch.post.it.evoting.securedatamanager.services.domain.model.administrationauthority.ConstituteOutcomeMessages;
import ch.post.it.evoting.securedatamanager.services.domain.model.administrationauthority.ReadShareInputData;
import ch.post.it.evoting.securedatamanager.services.domain.model.administrationauthority.ReadShareOutputData;
import ch.post.it.evoting.securedatamanager.services.domain.model.administrationauthority.ReconstructInputData;
import ch.post.it.evoting.securedatamanager.services.domain.model.administrationauthority.ReconstructOutputData;
import ch.post.it.evoting.securedatamanager.services.domain.model.administrationauthority.WriteShareInputData;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.SmartCardStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * The admin board end-point.
 */
@RestController
@RequestMapping("/sdm-backend/adminboards")
@Api(value = "Administration Board REST API")
public class AdminAuthorityController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdminAuthorityController.class);

	@Autowired
	private AdminBoardService adminBoardService;

	@Value("${tenantID}")
	private String tenantId;

	/**
	 * Returns a list of all admin boards.
	 *
	 * @return The list of admin boards.
	 */
	@GetMapping(produces = "application/json")
	@ResponseBody
	@ApiOperation(value = "List administration boards", response = String.class, notes = "Service to retrieve the list of adminitration boards.")
	public String getAdminBoards() {
		return adminBoardService.getAdminBoards();
	}

	/**
	 * Execute the constitute action: create keypair, split private key on shares and keep them in memory. It also puts the public key into a CSR.
	 *
	 * @param adminBoardId     the admin board ID.
	 * @param file             - file containing the keystore
	 * @param keystorePassword - password to open keystore
	 */
	@PostMapping(value = "/constitute/{adminBoardId}")
	@ApiOperation(value = "Constitute Service", notes = "Service to generate a key pair and splits the private key into shares.", response = Void.class)
	public ResponseEntity<Void> constitute(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String adminBoardId,
			@ApiParam(value = "file", required = true)
			@RequestParam("file")
			final
			MultipartFile file,
			@ApiParam(value = "keyStorePassword", required = true)
			@RequestParam("keystorePassword")
			final char[] keystorePassword) throws ResourceNotFoundException, SharesException, IOException {

		validateUUID(adminBoardId);

		try (final InputStream in = file.getInputStream()) {

			adminBoardService.constitute(adminBoardId, in, keystorePassword);

		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Check smartcard reader status: it can be EMPTY or INSERTED.
	 *
	 * @return STATUS
	 */
	@GetMapping(value = "/shares/status")
	@ApiOperation(value = "Check smartcard reader status", notes = "Checks the smartcard reader status.", response = SmartCardStatus.class)
	public ResponseEntity<SmartCardStatus> status() {

		final SmartCardStatus status = adminBoardService.getSmartCardReaderStatus();

		return new ResponseEntity<>(status, HttpStatus.OK);
	}

	/**
	 * Write share for the corresponding member.
	 *
	 * @return STATUS
	 */
	@PostMapping(value = "/{adminBoardId}/shares/{shareNumber}")
	@ApiOperation(value = "Write share", notes = "Writes a the share specified by the share number into the smartcard", response = Void.class)
	public ResponseEntity<Void> writeShare(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String adminBoardId,
			@ApiParam(value = "Integer", required = true)
			@PathVariable
			final Integer shareNumber,
			@ApiParam(value = "WriteShareInputData", required = true)
			@RequestBody
			final WriteShareInputData inputData) throws ResourceNotFoundException, SharesException, IOException {

		validateUUID(adminBoardId);

		adminBoardService.writeShare(adminBoardId, shareNumber, inputData.getPin());

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ConstituteOutcomeMessages> handleConfigurationEngineError(final HttpServletRequest req, final Exception exception) {

		final ConstituteOutcomeMessages constituteOutcomeMessages = new ConstituteOutcomeMessages();
		final List<String> messages = new ArrayList<>();
		messages.add(exception.getMessage());
		constituteOutcomeMessages.setMessages(messages);

		return new ResponseEntity<>(constituteOutcomeMessages, HttpStatus.PRECONDITION_FAILED);
	}

	/**
	 * Executes the activate action.
	 */
	@PostMapping(value = "/{adminBoardId}/activate")
	@ApiOperation(value = "Activate", notes = "Starts the process to activate an administration"
			+ " board. The issuer and the subject public key are returned.", response = ActivateOutputData.class)
	public ResponseEntity<ActivateOutputData> activate(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final
			String adminBoardId) throws GeneralCryptoLibException {

		validateUUID(adminBoardId);

		final ActivateOutputData output = adminBoardService.activate(adminBoardId);
		return new ResponseEntity<>(output, HttpStatus.OK);

	}

	/**
	 * Executes the read share action, and returns the serialized share in Base64 encoded format.
	 */
	@PostMapping(value = "/{adminBoardId}/read/{shareNumber}")
	@ApiOperation(value = "Read share", notes = "Service to read a share and returns it serialized.", response = ReadShareOutputData.class)
	public ResponseEntity<ReadShareOutputData> readShare(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final
			String adminBoardId,
			@ApiParam(value = "Integer", required = true)
			@PathVariable
			final
			Integer shareNumber,
			@ApiParam(value = "ReadShareInputData", required = true)
			@RequestBody
			final
			ReadShareInputData inputData) {

		validateUUID(adminBoardId);

		try {
			final String share = adminBoardService.readShare(adminBoardId, shareNumber, inputData.getPin(), inputData.getPublicKeyPEM());

			final ReadShareOutputData outputData = new ReadShareOutputData();
			outputData.setSerializedShare(share);

			return new ResponseEntity<>(outputData, HttpStatus.OK);

		} catch (final ResourceNotFoundException e) {
			final String errorMessage = "Missing information required to read admin board share";
			LOGGER.error(errorMessage, e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (final SharesException e) {
			final String errorMessage = "Error occurred reading the admin board share";
			LOGGER.error(errorMessage, e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (final IllegalArgumentException e) {
			final String errorMessage = "Invalid input data to read admin board share";
			LOGGER.error(errorMessage, e);
			return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
		}
	}

	/**
	 * Executes the reconstruct action, given a list of serialized admin board shares.
	 */
	@PostMapping(value = "/{adminBoardId}/reconstruct")
	@ApiOperation(value = "Reconstruct", notes = "Reconstructs the private key of an administration board, "
			+ "given a list of serialized private keys.", response = ReconstructOutputData.class)
	public ResponseEntity<ReconstructOutputData> reconstruct(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final
			String adminBoardId,
			@ApiParam(value = "ReconstructInputData", required = true)
			@RequestBody
			final
			ReconstructInputData inputData) throws SharesException, GeneralCryptoLibException {

		validateUUID(adminBoardId);

		final String privateKeyPEM = adminBoardService.reconstruct(adminBoardId, inputData.getSerializedShares(), inputData.getSerializedPublicKey());

		final ReconstructOutputData output = new ReconstructOutputData();
		output.setSerializedPrivateKey(privateKeyPEM);

		return new ResponseEntity<>(output, HttpStatus.OK);
	}

}
