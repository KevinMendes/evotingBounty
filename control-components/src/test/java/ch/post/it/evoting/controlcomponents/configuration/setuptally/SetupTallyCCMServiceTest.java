/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.configuration.setuptally;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;

@DisplayName("A SetupTallyCCMService")
class SetupTallyCCMServiceTest {

	private static SetupTallyCCMService setupTallyCCMService;
	private static GqGroup group;

	@BeforeAll
	static void setUpAll() {
		setupTallyCCMService = new SetupTallyCCMService(new RandomService());
		group = GroupTestData.getGqGroup();
	}

	@Test
	@DisplayName("with a valid parameter does not throw any Exception.")
	void validParamDoesNotThrow() {
		assertDoesNotThrow(() -> setupTallyCCMService.setupTallyCCM(group));
	}

	@Test
	@DisplayName("with a null parameter throws a NullPointerException.")
	void nullParamThrowsANullPointer() {
		assertThrows(NullPointerException.class, () -> setupTallyCCMService.setupTallyCCM(null));
	}

	@Test
	@DisplayName("with a valid parameter returns a non-null keypair with expected size.")
	void nonNullOutput() {
		final int mu = VotingOptionsConstants.MAXIMUM_NUMBER_OF_WRITE_IN_OPTIONS + 1;

		final ElGamalMultiRecipientKeyPair keyPair = setupTallyCCMService.setupTallyCCM(group);

		assertNotNull(keyPair);
		assertEquals(mu, keyPair.getPublicKey().size());
		assertEquals(mu, keyPair.getPrivateKey().size());
	}
}
