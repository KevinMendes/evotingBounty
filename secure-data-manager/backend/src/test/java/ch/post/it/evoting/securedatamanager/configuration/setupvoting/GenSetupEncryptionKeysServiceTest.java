/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants.MAXIMUM_NUMBER_OF_VOTING_OPTIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevel;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;

@DisplayName("GenSetupEncryptionKeysService")
class GenSetupEncryptionKeysServiceTest {

	private final GenSetupEncryptionKeysService genSetupEncryptionKeysService = new GenSetupEncryptionKeysService(new RandomService());

	@Test
	@DisplayName("calling genSetupEncryptionKeys with null argument throws a NullPointerException.")
	void genSetupEncryptionKeysWithNullGroupThrows() {
		assertThrows(NullPointerException.class, () -> genSetupEncryptionKeysService.genSetupEncryptionKeys(null));
	}

	@Test
	@DisplayName("calling genSetupEncryptionKeys with a correct argument generates a keypair with the right size.")
	void genSetupEncryptionKeys() {
		final GqGroup group = GroupTestData.getGqGroup();
		final ElGamalMultiRecipientKeyPair elGamalMultiRecipientKeyPair;
		try (final MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(SecurityLevel.TESTING_ONLY);
			elGamalMultiRecipientKeyPair = genSetupEncryptionKeysService.genSetupEncryptionKeys(group);
		}
		assertEquals(MAXIMUM_NUMBER_OF_VOTING_OPTIONS, elGamalMultiRecipientKeyPair.size());
	}
}