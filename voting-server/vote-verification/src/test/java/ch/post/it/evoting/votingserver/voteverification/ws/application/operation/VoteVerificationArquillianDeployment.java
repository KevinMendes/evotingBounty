/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.ws.application.operation;

import java.security.KeyPair;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ArchiveExportException;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ch.post.it.evoting.cryptolib.extendedkeystore.service.ExtendedKeyStoreService;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions.ApplicationExceptionHandler;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions.DuplicateEntryExceptionHandler;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions.GlobalExceptionHandler;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions.ResourceNotFoundExceptionHandler;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions.RetrofitErrorHandler;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions.ValidationExceptionHandler;
import ch.post.it.evoting.votingserver.commons.util.CryptoUtils;
import ch.post.it.evoting.votingserver.voteverification.infrastructure.health.HealthCheckRegistryProducer;
import ch.post.it.evoting.votingserver.voteverification.infrastructure.remote.VvRemoteCertificateServiceImpl;
import ch.post.it.evoting.votingserver.voteverification.service.VoteCastReturnCodeService;
import ch.post.it.evoting.votingserver.voteverification.service.crypto.PrimitivesServiceProducer;

@ArquillianSuiteDeployment
public class VoteVerificationArquillianDeployment {

	public static KeyPair keyPair;

	static {
		keyPair = CryptoUtils.getKeyPairForSigning();
	}

	@Deployment
	public static WebArchive deploy() throws ArchiveExportException, IllegalArgumentException {

		return ShrinkWrap.create(WebArchive.class, "vv-ws-rest.war")
				.addPackages(true, Filters.exclude(VoteCastReturnCodeService.class, PrimitivesServiceProducer.class),
						"ch.post.it.evoting.votingserver.voteverification")
				.addPackages(true, "org.h2")
				.addPackages(true,
						Filters.exclude(ResourceNotFoundExceptionHandler.class, GlobalExceptionHandler.class, ValidationExceptionHandler.class,
								ApplicationExceptionHandler.class, DuplicateEntryExceptionHandler.class, RetrofitErrorHandler.class,
								ExtendedKeyStoreService.class), "ch.post.it.evoting.votingserver.commons")
				.deleteClass(VvRemoteCertificateServiceImpl.class)
				.addClass(HideVvRemoteCertificateServiceImpl.class)
				.addClass(VoteVerificationArquillianTest.PrimitivesServiceMock.class)
				.deleteClass(HealthCheckRegistryProducer.class)
				.addPackages(true, "com.fasterxml.jackson.jaxrs")
				.addPackages(true, "org.bouncycastle.jce")
				.addAsManifestResource("test-persistence.xml", "persistence.xml")
				.addAsWebInfResource("beans.xml");
	}
}
