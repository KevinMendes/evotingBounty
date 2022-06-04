/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.ballot;

import javax.json.JsonObject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * implementation of the repository using a REST CLIENT
 */
@Repository
public class BallotUploadRepository {

	private static final String ADMIN_BOARD_ID_PARAM = "adminBoardId";

	private static final String BALLOT_ID_PARAM = "ballotId";

	private static final String ELECTION_EVENT_ID_PARAM = "electionEventId";

	private static final String TENANT_ID_PARAM = "tenantId";

	private static final String UPLOAD_BALLOT_URL = "ballotdata/tenant/{tenantId}/electionevent/{electionEventId}/ballot/{ballotId}/adminboard/{adminBoardId}";

	@Value("${EI_URL}")
	private String electionInformationUrl;

	@Value("${tenantID}")
	private String tenantId;

	/**
	 * Uploads the available ballots and ballot texts to the voter portal.
	 */
	public boolean uploadBallot(final JsonObject jsonBallot, final String electionEventId, final String ballotId, final String adminBoardId) {

		final WebTarget target = ClientBuilder.newClient().target(electionInformationUrl);
		final Response response = target.path(UPLOAD_BALLOT_URL).resolveTemplate(TENANT_ID_PARAM, tenantId)
				.resolveTemplate(ELECTION_EVENT_ID_PARAM, electionEventId).resolveTemplate(BALLOT_ID_PARAM, ballotId)
				.resolveTemplate(ADMIN_BOARD_ID_PARAM, adminBoardId).request()
				.post(Entity.entity(jsonBallot.toString(), MediaType.APPLICATION_JSON_TYPE));

		return response.getStatus() == Response.Status.OK.getStatusCode();

	}
}
