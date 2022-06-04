/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.ws.application.operation;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.delete;
import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Instant;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence.ExtendedAuthenticationService;

/**
 * Web service for uploading for extended authentication.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@javax.ws.rs.Path(ExtendedAuthDataResource.RESOURCE_PATH)
public class ExtendedAuthDataResource {

	public static final String RESOURCE_PATH = "/extendedauthentication";

	public static final String SAVE_EXTENDED_AUTHENTICATION_DATA = "/tenant/{tenantId}/electionevent/{electionevent}/adminboard/{adminBoardId}";
	public static final String PARAMETER_HEADER_X_REQUEST_ID = "X-Request-ID";
	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";
	private static final String PARAMETER_VALUE_ELECTION_EVENT = "electionevent";
	private static final String PARAMETER_VALUE_ADMIN_BOARD_ID = "adminBoardId";
	// The name of the query parameter tenantId
	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";
	// The name of the query parameter electionEventId
	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";
	// The name of the query parameter adminBoardId
	private static final String QUERY_PARAMETER_ADMIN_BOARD_ID = "adminBoardId";
	// The name of the resource handle by this web service.
	private static final String RESOURCE_NAME = "extendedauthentication";
	private static final String TEXT_CSV = "text/csv";

	private static final String EXPORT_BLOCKED_AUTHENTICATIONS = "/tenant/{tenantId}/electionevent/{electionevent}/blocked";

	private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	private static final String ATTACHMENT_FILENAME_SIZE = "attachment; filename=%s";
	private static final String EXPORT_NAME_FORMAT = "%s-vc-failed-authentication-%s";
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedAuthDataResource.class);
	@Inject
	protected ExtendedAuthenticationService extendedAuthenticationService;

	@Inject
	protected TrackIdInstance trackIdInstance;

	@javax.ws.rs.Path(SAVE_EXTENDED_AUTHENTICATION_DATA)
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(TEXT_CSV)
	public Response saveExtendedAuthenticationData(
			@HeaderParam(PARAMETER_HEADER_X_REQUEST_ID)
			final String trackingId,
			@NotNull
			@PathParam(PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@NotNull
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT)
			final String electionEventId,
			@NotNull
			@PathParam(PARAMETER_VALUE_ADMIN_BOARD_ID)
			final String adminBoardId,
			@NotNull
			final InputStream data,
			@Context
			final HttpServletRequest request) throws ApplicationException, IOException {

		trackIdInstance.setTrackId(trackingId);

		Response.Status status;

		LOGGER.info("Saving extended authentication information data for electionEventId: {}, tenantId: {}, and adminBoardId: {}.", electionEventId,
				tenantId, adminBoardId);

		validateParameters(tenantId, electionEventId, adminBoardId);

		java.nio.file.Path file = createTemporaryFile(data);

		try {
			boolean saveExtendedAuthenticationFromFile = extendedAuthenticationService
					.saveExtendedAuthenticationFromFile(file, tenantId, electionEventId, adminBoardId);
			status = saveExtendedAuthenticationFromFile ? Status.OK : Status.PRECONDITION_FAILED;
		} catch (IOException e) {
			// debug because this exception should not be logged in production
			LOGGER.debug(e.getMessage(), e);
			status = Status.PRECONDITION_FAILED;
		} finally {
			deleteTemporaryFile(file);
		}

		return Response.status(status).build();
	}

	@javax.ws.rs.Path(EXPORT_BLOCKED_AUTHENTICATIONS)
	@GET
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.TEXT_PLAIN })
	public Response exportVotingCardsWithExceedAuthenticationAttempts(
			@NotNull
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@NotNull
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT)
					String electionEventId,
			@NotNull
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Context
					HttpServletRequest request) {

		trackIdInstance.setTrackId(trackId);

		String timestamp = Long.toString(Instant.now(Clock.systemUTC()).getEpochSecond());
		String filename = String.format(EXPORT_NAME_FORMAT, electionEventId, timestamp) + ".csv";
		StreamingOutput entity = stream -> extendedAuthenticationService
				.findAndWriteVotingCardsWithFailedAuthentication(tenantId, electionEventId, stream);

		return Response.ok().entity(entity).header(HEADER_CONTENT_DISPOSITION, String.format(ATTACHMENT_FILENAME_SIZE, filename))
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM).build();

	}

	private Path createTemporaryFile(final InputStream data) throws IOException {
		Path file = Files.createTempFile("extendedAuthentication", ".csv");
		try {
			copy(data, file, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			deleteTemporaryFile(file);
			throw e;
		}
		return file;
	}

	private void deleteTemporaryFile(final Path file) {
		try {
			delete(file);
		} catch (IOException e) {
			LOGGER.warn(format("Failed to delete temporary file ''{0}''.", file), e);
		}
	}

	private void validateParameters(final String tenantId, final String electionEventId, final String adminBoardId) throws ApplicationException {

		if (tenantId == null || tenantId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_TENANT_ID);
		}

		if (electionEventId == null || electionEventId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_ELECTION_EVENT_ID);
		}

		if (adminBoardId == null || adminBoardId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_ADMIN_BOARD_ID);
		}
	}
}
