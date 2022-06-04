/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ch.post.it.evoting.cryptoprimitives.CryptoPrimitives;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitivesService;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ZqGroupGenerator;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SystemStubsExtension.class)
@DisplayName("CcrjReturnCodesKeysService")
class CcrjReturnCodesKeysServiceTest {

	private static final CryptoPrimitives cryptoPrimitives = CryptoPrimitivesService.get();

	@SystemStub
	private static EnvironmentVariables environmentVariables;

	private static String electionEventId;
	private static GqGroup encryptionGroup;

	@Autowired
	private CcrjReturnCodesKeysService ccrjReturnCodesKeysService;

	@BeforeAll
	static void setUpAll(
			@Autowired
			final ElectionEventService electionEventService) {
		environmentVariables.set("SECURITY_LEVEL", "TESTING_ONLY");

		electionEventId = cryptoPrimitives.genRandomBase16String(32).toLowerCase();
		encryptionGroup = GroupTestData.getGqGroup();
		electionEventService.save(electionEventId, encryptionGroup);
	}

	@Test
	@DisplayName("return codes keys saves to database")
	void save() {
		final ZqGroupGenerator zqGroupGenerator = new ZqGroupGenerator(ZqGroup.sameOrderAs(encryptionGroup));
		final ZqElement ccrjReturnCodesGenerationSecretKey = zqGroupGenerator.genRandomZqElementMember();
		final ElGamalMultiRecipientKeyPair ccrjChoiceReturnCodesEncryptionKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(encryptionGroup, 1,
				new RandomService());
		final CcrjReturnCodesKeys ccrjReturnCodesKeys = new CcrjReturnCodesKeys(electionEventId, ccrjReturnCodesGenerationSecretKey,
				ccrjChoiceReturnCodesEncryptionKeyPair);

		assertDoesNotThrow(() -> ccrjReturnCodesKeysService.save(ccrjReturnCodesKeys));
	}
}