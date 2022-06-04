/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.verificationcardset;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import ch.post.it.evoting.securedatamanager.services.infrastructure.InputStreamTypedOutput;
import ch.post.it.evoting.securedatamanager.services.infrastructure.RestClientService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.clients.VoteVerificationClient;
import ch.post.it.evoting.securedatamanager.services.infrastructure.exception.VerificationCardSetUploadRepositoryException;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * implementation of the repository using a REST CLIENT
 */
@Repository
public class VerificationCardSetUploadRepository {

	private static final MediaType TEXT_CSV_TYPE = new MediaType("text", "csv");

	final VoteVerificationClient voteVerificationClient;
	@Value("${tenantID}")
	private String tenantId;

	@Autowired
	public VerificationCardSetUploadRepository(
			@Value("${VV_URL}")
			final
			String voteVerificationURL,
			@Value("${connection.time.out}")
			final
			String connectionTimeOut,
			@Value("${read.time.out}")
			final
			String readTimeOut,
			@Value("${write.time.out}")
			final
			String writeTimeOut) {
		setTimeouts(connectionTimeOut, readTimeOut, writeTimeOut);
		voteVerificationClient = getVoteVerificationClient(voteVerificationURL);
	}

	/**
	 * Uploads the Return Codes mapping table from a given stream. The client is responsible for closing the stream.
	 *
	 * @param electionEventId       the election event identifier
	 * @param verificationCardSetId the verification card set identifier
	 * @param adminBoardId          the administrative board identifier
	 * @param stream                the stream
	 * @throws IOException failed to upload the mapping.
	 */
	public void uploadCodesMapping(final String electionEventId, final String verificationCardSetId, final String adminBoardId, final InputStream stream) throws IOException {
		final InputStreamTypedOutput body = new InputStreamTypedOutput(TEXT_CSV_TYPE.toString(), stream);

		final Response<ResponseBody> response = voteVerificationClient
				.saveCodesMappingData(tenantId, electionEventId, verificationCardSetId, adminBoardId, body).execute();

		handleErrorResponse(response, "Failed to upload codes mapping: ");
	}

	/**
	 * Uploads the verification card data from a given stream. The client is responsible for closing the stream.
	 *
	 * @param electionEventId       the election event identifier
	 * @param verificationCardSetId the verification card set identifier
	 * @param adminBoardId          the administrative board identifier
	 * @param stream                the stream
	 * @throws IOException failed to upload the data.
	 */
	public void uploadVerificationCardData(
			final String electionEventId, final String verificationCardSetId, final String adminBoardId, final InputStream stream)
			throws IOException {

		final InputStreamTypedOutput body = new InputStreamTypedOutput(TEXT_CSV_TYPE.toString(), stream);

		final Response<ResponseBody> response = voteVerificationClient
				.saveVerificationCardData(tenantId, electionEventId, verificationCardSetId, adminBoardId, body).execute();

		handleErrorResponse(response, "Failed to upload verification card data: ");
	}

	/**
	 * Uploads the verification card set data given a voter information JSON object The client is responsible for closing the stream.
	 *
	 * @param electionEventId        the election event identifier
	 * @param verificationCardSetId  the verification card set identifier
	 * @param adminBoardId           the administrative board identifier
	 * @throws IOException failed to upload the data.
	 */
	public void uploadVerificationCardSetData(final String electionEventId, final String verificationCardSetId, final String adminBoardId,
			final JsonObject verificationCardSetData) throws IOException {

		final InputStreamTypedOutput body = new InputStreamTypedOutput(MediaType.APPLICATION_JSON,
				new ByteArrayInputStream(verificationCardSetData.toString().getBytes(StandardCharsets.UTF_8)));

		final Response<ResponseBody> response = voteVerificationClient
				.saveVerificationCardSetData(tenantId, electionEventId, verificationCardSetId, adminBoardId, body).execute();

		handleErrorResponse(response, "Failed to upload verification card set data: ");
	}

	private VoteVerificationClient getVoteVerificationClient(final String voteVerificationURL) {
		final Retrofit restAdapter = RestClientService.getInstance().getRestClientWithJacksonConverter(voteVerificationURL);
		return restAdapter.create(VoteVerificationClient.class);
	}

	private void setTimeouts(final String connectionTimeOut, final String readTimeOut, final String writeTimeOut) {
		System.setProperty("connection.time.out", connectionTimeOut);
		System.setProperty("read.time.out", readTimeOut);
		System.setProperty("write.time.out", writeTimeOut);
	}

	/**
	 * Handles response if the request failed.
	 *
	 * @param response     the response of the failed request to handle.
	 * @param errorMessage an error message if the request failed.
	 * @throws VerificationCardSetUploadRepositoryException with {@code errorMessage} if the failure response could not be converted to a String.
	 */
	private void handleErrorResponse(final Response<ResponseBody> response, final String errorMessage) {
		if (!response.isSuccessful()) {
			final String errorBodyString;
			try {
				errorBodyString = response.errorBody().string();
			} catch (final IOException e) {
				throw new UncheckedIOException("Failed to convert response body to string.", e);
			}
			throw new VerificationCardSetUploadRepositoryException(String.format("%s%s", errorMessage, errorBodyString));
		}
	}

}
