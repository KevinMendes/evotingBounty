/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

/**
 * This class defines the application path for initializing the REST engine.
 */
@ApplicationPath("")
public class RestApplication extends Application {

	public static final String WITH_CHARSET_UTF_8 = ";charset=UTF-8";

	public static final String MEDIA_TYPE_JSON_UTF_8 = MediaType.APPLICATION_JSON + WITH_CHARSET_UTF_8;

	public static final String API_OV_VOTING_BASEURI_AUTH = "/api/ov/voting/{version}/tenant/{tenantId}/electionevent/{electionEventId}/credential/{credentialId}";

	public static final String API_OV_VOTING_BASEURI = "/api/ov/voting/{version}/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}";

	public static final String API_OV_VOTING_BASEURI_EXTENDED_AUTH = "/api/ov/voting/{version}/tenant/{tenantId}/electionevent/{electionEventId}";

	public static final String API_OV_MONITORING_VERSION_BASEURI = "/api/ov/monitoring/{version}";

	public static final String API_OV_MONITORING_BASEURI = API_OV_MONITORING_VERSION_BASEURI + "/tenant/{tenantId}/electionevent/{electionEventId}";

	public static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	public static final String PARAMETER_VALUE_VOTING_CARD_ID = "votingCardId";

	public static final String PARAMETER_VALUE_VERIFICATION_CARD_SET_ID = "verificationCardSetId";

	public static final String PARAMETER_VALUE_CREDENTIAL_ID = "credentialId";

	public static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	public static final String PARAMETER_VALUE_VERSION = "version";

	public static final String PARAMETER_AUTHENTICATION_TOKEN = "authenticationToken";

	public static final String PARAMETER_EXTENDED_AUTH_PATH = "extended_authenticate";

	public static final String PARAMETER_VALUE_CHUNK_ID = "chunkId";

	public static final String PARAMETER_VALUE_CHUNK_COUNT = "chunkCount";

}
