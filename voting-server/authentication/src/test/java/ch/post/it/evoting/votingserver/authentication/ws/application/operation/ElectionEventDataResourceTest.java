/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.ws.application.operation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.domain.election.AuthenticationContextData;
import ch.post.it.evoting.domain.election.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoardRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCertsRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationContentRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.ElectionEventData;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.ElectionEventService;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.util.CryptoUtils;
import ch.post.it.evoting.votingserver.commons.verify.JSONVerifier;

public class ElectionEventDataResourceTest extends JerseyTest {

	public static final String PARAM_TENANT_ID = "tenantId";

	public static final String TENANT_ID = "100";

	public static final String PARAM_ELECTION_EVENT_ID = "electionEventId";

	public static final String ELECTION_EVENT_ID = "100";

	public static final String ADMIN_BOARD_ID = "100";

	public static final String PARAM_ADMIN_BOARD_ID = "adminBoardId";
	private static final String PATH_POST = "/electioneventdata/tenant/{tenantId}/electionevent/{electionEventId}/adminboard/{adminBoardId}";

	@Mock
	HttpServletRequest servletRequest;

	// An instance of the authentication content repository
	@Mock
	private AuthenticationContentRepository authenticationContentRepository;

	// An instance of the authentication certificates repository
	@Mock
	private AuthenticationCertsRepository authenticationCertsRepository;

	@Mock
	private Logger LOGGER;

	@Mock
	private RemoteCertificateService remoteCertificateService;

	@Mock
	private AdminBoardRepository adminBoardRepository;

	// The track id instance
	@Mock
	private TrackIdInstance trackIdInstance;

	private ElectionEventData electionEventData;

	@Mock
	private CertificateEntity certificateEntity;

	@Mock
	private AuthenticationContextData authenticationContextData;

	@Mock
	private ElectionEventService electionEventService;

	@Mock
	private JSONVerifier jsonVerifier;

	@Test
	public void uploadElectionEventData() throws GeneralCryptoLibException, RetrofitException {

		final int expectedStatus = 200;
		commonPreparation();
		prepareData();
		final KeyPair keyPairForSigning = CryptoUtils.getKeyPairForSigning();
		final CryptoAPIX509Certificate adminBoardCert = CryptoUtils
				.createCryptoAPIx509Certificate("adminBoard", CertificateParameters.Type.SIGN, keyPairForSigning);

		when(remoteCertificateService.getAdminBoardCertificate(anyString())).thenReturn(certificateEntity);
		when(certificateEntity.getCertificateContent()).thenReturn(new String(adminBoardCert.getPemEncoded(), StandardCharsets.UTF_8));

		final Response post = target().path(PATH_POST).resolveTemplate(PARAM_TENANT_ID, TENANT_ID)
				.resolveTemplate(PARAM_ELECTION_EVENT_ID, ELECTION_EVENT_ID).resolveTemplate(PARAM_ADMIN_BOARD_ID, ADMIN_BOARD_ID).request()
				.post(Entity.entity(electionEventData, MediaType.APPLICATION_JSON_TYPE));

		assertThat(post.getStatus(), is(expectedStatus));
	}

	private void commonPreparation() {
		when(servletRequest.getHeader("X-Request-ID")).thenReturn("request");
		when(servletRequest.getRemoteAddr()).thenReturn("");
		when(servletRequest.getLocalAddr()).thenReturn("");
	}

	private void prepareData() {

		electionEventData = new ElectionEventData();
		electionEventData.setAuthenticationContextData("{\"signature\":\"authenticationContextData\"}");
		electionEventData.setAuthenticationVoterData("{\"signature\":\"authenticationVoterData\"}");

	}

	@Override
	protected Application configure() {

		MockitoAnnotations.openMocks(this);

		final AbstractBinder binder = new AbstractBinder() {
			@Override
			protected void configure() {

				bind(authenticationCertsRepository).to(AuthenticationCertsRepository.class);
				bind(authenticationContentRepository).to(AuthenticationContentRepository.class);
				bind(remoteCertificateService).to(RemoteCertificateService.class);
				bind(adminBoardRepository).to(AdminBoardRepository.class);
				bind(trackIdInstance).to(TrackIdInstance.class);
				bind(servletRequest).to(HttpServletRequest.class);
				bind(jsonVerifier).to(JSONVerifier.class);
				bind(electionEventService).to(ElectionEventService.class);
				bind(LOGGER).to(Logger.class);
				bind(authenticationContextData).to(AuthenticationContextData.class);
			}
		};

		final ElectionEventDataResource electionEventDataResource = new ElectionEventDataResource(electionEventService, trackIdInstance);

		forceSet(TestProperties.CONTAINER_PORT, "0");
		return new ResourceConfig().register(electionEventDataResource).register(binder);

	}
}
