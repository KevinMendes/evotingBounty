/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.model.authentication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIPBKDFDeriver;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ChallengeGenerator;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ExtraParams;
import ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service.ChallengeService;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

	private final String challenge = "d710a4df654a4d7480df52f0ae9de610";
	private final String alias = "alias";
	private final ExtraParams extraParams = ExtraParams.ofChallenges(Optional.of(challenge), Optional.of(alias));

	@Mock
	CryptoAPIDerivedKey cryptoApiDerivedKeyMock;

	@InjectMocks
	private ChallengeService challengeService;

	@Mock
	private PrimitivesServiceAPI primitivesServiceMock;

	@Mock
	private CryptoAPIPBKDFDeriver deriverMock;

	@Mock
	private ChallengeGenerator challengeGenerator;

	@Test
	void createChallenge() throws GeneralCryptoLibException {

		when(primitivesServiceMock.getPBKDFDeriver()).thenReturn(deriverMock);
		when(deriverMock.deriveKey(any(), any())).thenReturn(cryptoApiDerivedKeyMock);
		String derivedChallengeString = "adsfkasdfklhaskldfhlskajhflkawjshdlkfjhaslkjfdh";
		when(cryptoApiDerivedKeyMock.getEncoded()).thenReturn(derivedChallengeString.getBytes(StandardCharsets.UTF_8));
		when(challengeGenerator.generateExtraParams()).thenReturn(extraParams);

		assertNotNull(challengeService.createExtendedAuthChallenge());

	}

	@Test
	void createWithCryptoLibException() throws GeneralCryptoLibException {
		when(primitivesServiceMock.getPBKDFDeriver()).thenReturn(deriverMock);
		when(deriverMock.deriveKey(any(), any())).thenThrow(GeneralCryptoLibException.class);
		when(challengeGenerator.generateExtraParams()).thenReturn(extraParams);

		assertThrows(GeneralCryptoLibException.class, () -> challengeService.createExtendedAuthChallenge());
	}

}
