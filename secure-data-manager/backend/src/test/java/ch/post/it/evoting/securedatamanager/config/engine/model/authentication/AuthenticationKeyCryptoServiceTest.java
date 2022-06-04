/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.model.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationDerivedElement;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationKey;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.StartVotingKey;
import ch.post.it.evoting.securedatamanager.config.engine.SpringConfigTest;
import ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service.AuthenticationKeyCryptoService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpringConfigTest.class, loader = AnnotationConfigContextLoader.class)
class AuthenticationKeyCryptoServiceTest {

	private static final StartVotingKey startVotingKey = StartVotingKey.ofValue("zpfrcmn28mcct4pf682w");
	private static final AuthenticationKey authenticationKey = AuthenticationKey
			.ofSecrets("zpfrcmn28mcct4pf682w", Optional.of(Collections.singletonList("zpfrcmn28mcct4pf682w")));
	private final String eeID = "d710a4df654a4d7480df52f0ae9de610";

	@Autowired
	private AuthenticationKeyCryptoService authenticationKeyCryptoService;

	@Test
	void deriveAuthKeyId() {

		final AuthenticationDerivedElement authId = authenticationKeyCryptoService
				.deriveElement(Constants.AUTH_ID, eeID, authenticationKey.getValue());
		assertNotNull(authId);

	}

	@Test
	void deriveAuthKeyPassword() {

		final AuthenticationDerivedElement authenticationKeyPassword = authenticationKeyCryptoService
				.deriveElement(Constants.AUTH_PW, eeID, authenticationKey.getValue());
		assertNotNull(authenticationKeyPassword);

	}

	@Test
	void encryptSVK() {

		final AuthenticationDerivedElement authenticationKeyPassword = authenticationKeyCryptoService
				.deriveElement(Constants.AUTH_PW, eeID, authenticationKey.getValue());
		String encryptStartVotingKey = authenticationKeyCryptoService.encryptSVK(startVotingKey, authenticationKeyPassword);
		assertNotNull(encryptStartVotingKey);

	}

	@Test
	void decryptSVK() {
		final AuthenticationDerivedElement authenticationKeyPassword = authenticationKeyCryptoService
				.deriveElement(Constants.AUTH_PW, eeID, authenticationKey.getValue());
		String encryptedStartVotingKey = authenticationKeyCryptoService.encryptSVK(startVotingKey, authenticationKeyPassword);
		final String originalSVK = authenticationKeyCryptoService.decryptSVK(encryptedStartVotingKey, authenticationKeyPassword);
		assertEquals(originalSVK, startVotingKey.getValue());

	}

}
