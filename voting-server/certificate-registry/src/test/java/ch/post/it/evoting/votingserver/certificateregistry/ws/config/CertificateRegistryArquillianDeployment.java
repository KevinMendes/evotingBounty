/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.certificateregistry.ws.config;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import ch.post.it.evoting.votingserver.certificateregistry.ws.operation.SystemPropertiesLoader;

@ArquillianSuiteDeployment
public class CertificateRegistryArquillianDeployment {

	private static final SystemPropertiesLoader SYSTEM_PROPERTIES_LOADER = new SystemPropertiesLoader();

	@Deployment
	public static WebArchive buildDeployable() {
		beforeDeployment();
		return ShrinkWrap.create(WebArchive.class, "cr-ws-rest.war").addClasses(org.slf4j.Logger.class).addPackage("ch.post.it.evoting.logging")
				.addPackages(true, "ch.post.it.evoting.cryptolib").addPackages(true, "org.h2").addPackages(true, "com.fasterxml.jackson.jaxrs")
				.addPackages(true, "ch.post.it.evoting.votingserver.certificateregistry").addPackages(true, "ch.post.it.evoting.votingserver.commons")
				.deletePackages(true, "ch.post.it.evoting.votingserver.commons.infrastructure.health").addAsResource("log4j2.xml")
				.addAsManifestResource("test-persistence.xml", "persistence.xml").addAsWebInfResource("beans.xml");
	}

	private static void beforeDeployment() {
		SYSTEM_PROPERTIES_LOADER.setProperties();
	}
}
