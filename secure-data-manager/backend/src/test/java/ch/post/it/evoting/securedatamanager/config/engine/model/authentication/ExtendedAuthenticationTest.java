/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.model.authentication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationKey;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ExtendedAuthChallenge;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ExtendedAuthInformation;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.StartVotingKey;
import ch.post.it.evoting.securedatamanager.config.engine.SpringConfigTest;
import ch.post.it.evoting.securedatamanager.config.engine.actions.ExtendedAuthenticationService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpringConfigTest.class, loader = AnnotationConfigContextLoader.class)
class ExtendedAuthenticationTest {

	private static final StartVotingKey startVotingKey = StartVotingKey.ofValue("zpfrcmn28mcct4pf682w");

	@Autowired
	private ExtendedAuthenticationService extendedAuthenticationService;

	@Test
	void generateExtendedAuthentication() {

		final String eeID = "d710a4df654a4d7480df52f0ae9de610";
		final ExtendedAuthInformation extendedAuthentication = extendedAuthenticationService.create(startVotingKey, eeID);
		assertNotNull(extendedAuthentication.getAuthenticationId());
		final AuthenticationKey authenticationKey = extendedAuthentication.getAuthenticationKey();
		assertNotNull(authenticationKey);

		assertNotNull(authenticationKey);

		assertNotNull(extendedAuthentication.getAuthenticationPin());

		final Optional<ExtendedAuthChallenge> extendedAuthChallengeOptional = extendedAuthentication.getExtendedAuthChallenge();

		assertNotNull(extendedAuthChallengeOptional);

		assertNotNull(extendedAuthentication.getEncryptedSVK());
	}

}
