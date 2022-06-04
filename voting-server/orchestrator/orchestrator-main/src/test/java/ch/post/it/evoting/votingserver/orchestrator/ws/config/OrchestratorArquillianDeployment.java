/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.ws.config;

import javax.enterprise.inject.Produces;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.extendedkeystore.service.ExtendedKeyStoreService;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingService;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingServiceImpl;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions.ApplicationExceptionHandler;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions.DuplicateEntryExceptionHandler;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions.GlobalExceptionHandler;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions.ResourceNotFoundExceptionHandler;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions.RetrofitErrorHandler;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions.ValidationExceptionHandler;
import ch.post.it.evoting.votingserver.orchestrator.commons.messaging.MessagingServiceProducer;
import ch.post.it.evoting.votingserver.orchestrator.infrastructure.health.HealthCheckRegistryProducer;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.remote.ElectionInformationClient;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.remote.producer.ElectionInformationRemoteClientProducer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.mock.Calls;

@ArquillianSuiteDeployment
public class OrchestratorArquillianDeployment {

	@Deployment(testable = false)
	public static WebArchive buildDeployable() {

		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "or-ws-rest.war");

		return webArchive.addPackages(true, Filters.exclude(ExtendedKeyStoreService.class, AsymmetricService.class),
				"ch.post.it.evoting.cryptolib").addPackages(true, "org.h2").addPackages(true, "ch.post.it.evoting.votingserver.orchestrator")
				.addPackages(true, "ch.post.it.evoting.votingserver.commons.logging.service")
				.addPackages(true, "ch.post.it.evoting.votingserver.commons.tracking")
				.addPackages(true, "ch.post.it.evoting.votingserver.commons.crypto").addPackages(true, "com.fasterxml.jackson.jaxrs")
				.addPackages(true,
						Filters.exclude(ResourceNotFoundExceptionHandler.class, GlobalExceptionHandler.class, ValidationExceptionHandler.class,
								ApplicationExceptionHandler.class, DuplicateEntryExceptionHandler.class, RetrofitErrorHandler.class,
								BouncyCastleProvider.class, ExtendedKeyStoreService.class), "ch.post.it.evoting.commons")
				.addClasses(org.slf4j.Logger.class).deleteClass(HealthCheckRegistryProducer.class)
				// Remove the default queue client producers, to be replaced with
				// non-SSL counterparts.
				.deleteClass(MessagingServiceProducer.class).deleteClass(ElectionInformationRemoteClientProducer.class)
				.addAsManifestResource("test-persistence.xml", "persistence.xml").addAsResource("log4j2.xml").addAsWebInfResource("beans.xml");
	}

	/**
	 * Overriding default messaging service that uses SSL so that in the tests no certificate creation is required.
	 *
	 * @return a producer for messaging service instances
	 */
	@Produces
	public MessagingService messagingService() {
		return new MessagingServiceImpl.Builder().setHostName("localhost").setPort(5672).setVirtualHost("cco").setUsername("admin")
				.setPassword("admin").setSenderPoolSize(Runtime.getRuntime().availableProcessors()).build();
	}

	/**
	 * A mock of the election information client that provides a fixed set of ballot boxes.
	 */
	public static class ElectionInformationClientMock implements ElectionInformationClient {

		@Override
		public Call<ValidationResult> validateElectionInDates(String requestId, String pathValidations, String tenantId, String electionEventId,
				String ballotBoxId) {
			return Calls.response(new ValidationResult(true));
		}

		@Override
		public Call<ResponseBody> isBallotBoxEmpty(String requestId, String pathCleansedBallotBoxes, String tenantId, String electionEventId,
				String ballotBoxId) {
			return null;
		}

		@Override
		public Call<ResponseBody> getMixnetInitialPayload(String requestId, String pathCleansedBallotBoxes, String tenantId, String electionEventId,
				String ballotBoxId) {
			return Calls.response(ResponseBody.create(okhttp3.MediaType.parse("application/octet-stream"), new byte[] {}));
		}

	}
}
