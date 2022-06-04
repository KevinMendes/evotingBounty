/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.ws.application.operation;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.domain.configuration.ReturnCodesMappingTablePayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.voteverification.service.ReturnCodesMappingTableService;

/**
 * Web service for saving and retrieving the return codes mapping table.
 */
@Path("api/v1/configuration/returncodesmappingtable")
@Stateless
public class ReturnCodesMappingTableResource {

	@VisibleForTesting
	static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";
	@VisibleForTesting
	static final String PARAMETER_VALUE_VERIFICATION_CARD_SET_ID = "verificationCardSetId";
	private static final Logger LOGGER = LoggerFactory.getLogger(ReturnCodesMappingTableResource.class);
	private static final String RESOURCE_NAME = "returncodesmappingtable";

	@Inject
	private ReturnCodesMappingTableService returnCodesMappingTableService;

	@Inject
	private TrackIdInstance tackIdInstance;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveReturnCodesMappingTable(
			@NotNull
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
			final String trackingId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(PARAMETER_VALUE_VERIFICATION_CARD_SET_ID)
			final String verificationCardSetId,
			@NotNull
			final ReturnCodesMappingTablePayload returnCodesMappingTablePayload,
			@Context
			final HttpServletRequest request) throws ApplicationException {

		tackIdInstance.setTrackId(trackingId);

		validateInput(PARAMETER_VALUE_ELECTION_EVENT_ID, electionEventId, returnCodesMappingTablePayload.getElectionEventId());
		validateInput(PARAMETER_VALUE_VERIFICATION_CARD_SET_ID, verificationCardSetId, returnCodesMappingTablePayload.getVerificationCardSetId());

		try {
			returnCodesMappingTableService.saveReturnCodesMappingTable(returnCodesMappingTablePayload);
		} catch (final DuplicateEntryException e) {
			LOGGER.warn("Duplicate entry tried to be inserted for return codes mapping table. [electionEventId: {}, verificationCardSetId: {}]",
					electionEventId, verificationCardSetId);
			return Response.noContent().build();
		}

		LOGGER.info("Successfully saved the return codes mapping table. [electionEventId: {}, verificationCardSetId: {}]", electionEventId,
				verificationCardSetId);

		return Response.ok().build();
	}

	@VisibleForTesting
	void validateInput(final String field, final String pathId, final String payloadId) throws ApplicationException {
		// Path id is invalid
		try {
			validateUUID(pathId);
		} catch (final FailedValidationException | NullPointerException e) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL_OR_INVALID, RESOURCE_NAME,
					ErrorCodes.VALIDATION_EXCEPTION, field);
		}
		// Path id does not match payload
		if (!pathId.equals(payloadId)) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_NOT_MATCH_PAYLOAD, RESOURCE_NAME,
					ErrorCodes.VALIDATION_EXCEPTION, field);
		}
	}
}
