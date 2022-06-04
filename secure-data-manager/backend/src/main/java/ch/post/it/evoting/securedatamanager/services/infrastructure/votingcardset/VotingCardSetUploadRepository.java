/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import ch.post.it.evoting.securedatamanager.services.infrastructure.InputStreamTypedOutput;
import ch.post.it.evoting.securedatamanager.services.infrastructure.RestClientService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.clients.ExtendedAuthenticationClient;
import ch.post.it.evoting.securedatamanager.services.infrastructure.clients.VoterMaterialClient;
import ch.post.it.evoting.securedatamanager.services.infrastructure.exception.VotingCardSetUploadRepositoryException;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * implementation of the repository using a REST CLIENT
 */
@Repository
public class VotingCardSetUploadRepository {

	private static final MediaType TEXT_CSV_TYPE = new MediaType("text", "csv");

	private final VoterMaterialClient voterMaterialClient;
	private final ExtendedAuthenticationClient extendedAuthenticationClient;
	@Value("${tenantID}")
	private String tenantId;

	@Autowired
	public VotingCardSetUploadRepository(
			@Value("${VM_URL}")
			final
			String voterMaterialURL,
			@Value("${EA_URL}")
			final
			String extendedAuthenticationURL,
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
		voterMaterialClient = getVoterMaterialClient(voterMaterialURL);
		extendedAuthenticationClient = getExtendedAuthenticationClient(extendedAuthenticationURL);
	}

	/**
	 * Uploads the voter information represented by a given stream. Client is responsible for closing the stream.
	 *
	 * @param electionEventId the election event identifier
	 * @param votingCardSetId the voting card set identifier
	 * @param adminBoardId    the administrative board identifier
	 * @param stream          the information
	 * @throws IOException failed to upload the information.
	 */
	public void uploadVoterInformation(final String electionEventId, final String votingCardSetId, final String adminBoardId,
			final InputStream stream) throws IOException {
		final InputStreamTypedOutput body = new InputStreamTypedOutput(TEXT_CSV_TYPE.toString(), stream);

		final Response<ResponseBody> response = voterMaterialClient
				.saveVoterInformationData(tenantId, electionEventId, votingCardSetId, adminBoardId, body).execute();

		handleErrorResponse(response, "Failed to upload voter information: ");
	}

	/**
	 * Uploads the credential data represented by a given stream. Client is responsible for closing the stream.
	 *
	 * @param electionEventId the election event identifier
	 * @param votingCardSetId the voting card set identifier
	 * @param stream          the data
	 * @throws IOException failed to upload the data.
	 */
	public void uploadCredentialData(final String electionEventId, final String votingCardSetId, final String adminBoardId, final InputStream stream)
			throws IOException {

		final InputStreamTypedOutput body = new InputStreamTypedOutput(TEXT_CSV_TYPE.toString(), stream);

		final Response<ResponseBody> response = voterMaterialClient.saveCredentialData(tenantId, electionEventId, votingCardSetId, adminBoardId, body)
				.execute();

		handleErrorResponse(response, "Failed to upload credential data: ");
	}

	/**
	 * Uploads the extended authentication represented by a given stream, Client is responsible for closing the stream.
	 *
	 * @param electionEventId the election event identifier
	 * @param adminBoardId    the adminBoard id
	 * @param stream          the data
	 * @throws IOException failed to upload the data.
	 */
	public void uploadExtendedAuthData(final String electionEventId, final String adminBoardId, final InputStream stream) throws IOException {
		final InputStreamTypedOutput body = new InputStreamTypedOutput(TEXT_CSV_TYPE.toString(), stream);

		final Response<ResponseBody> response = extendedAuthenticationClient
				.saveExtendedAuthenticationData(tenantId, electionEventId, adminBoardId, body).execute();

		handleErrorResponse(response, "Failed to upload extended authentication data: ");
	}

	private VoterMaterialClient getVoterMaterialClient(final String voterMaterialURL) {
		final Retrofit restAdapter = RestClientService.getInstance().getRestClientWithJacksonConverter(voterMaterialURL);
		return restAdapter.create(VoterMaterialClient.class);
	}

	private ExtendedAuthenticationClient getExtendedAuthenticationClient(final String extendedAuthenticationURL) {
		final Retrofit restAdapter = RestClientService.getInstance().getRestClientWithJacksonConverter(extendedAuthenticationURL);
		return restAdapter.create(ExtendedAuthenticationClient.class);
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
	 * @throws VotingCardSetUploadRepositoryException with {@code errorMessage} if the request failed.
	 * @throws UncheckedIOException                   if the failure response could not be converted to a String.
	 */
	private void handleErrorResponse(final Response<ResponseBody> response, final String errorMessage) {
		if (!response.isSuccessful()) {
			final String errorBodyString;
			try {
				errorBodyString = response.errorBody().string();
			} catch (final IOException e) {
				throw new UncheckedIOException("Failed to convert response body to string.", e);
			}
			throw new VotingCardSetUploadRepositoryException(String.format("%s%s", errorMessage, errorBodyString));
		}
	}

}
