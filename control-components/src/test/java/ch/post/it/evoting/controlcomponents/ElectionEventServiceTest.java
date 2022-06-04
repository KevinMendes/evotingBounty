/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.CryptoPrimitives;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitivesService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SystemStubsExtension.class)
@DisplayName("ElectionEventService")
class ElectionEventServiceTest {

	private static final CryptoPrimitives cryptoPrimitives = CryptoPrimitivesService.get();

	@SystemStub
	private static EnvironmentVariables environmentVariables;

	private String electionEventId;
	private GqGroup encryptionGroup;

	@Autowired
	private ElectionEventService electionEventService;

	@SpyBean
	private ElectionEventRepository electionEventRepository;

	@BeforeAll
	static void setUpAll() {
		environmentVariables.set("SECURITY_LEVEL", "TESTING_ONLY");
	}

	@BeforeEach
	void setUp() {
		electionEventId = cryptoPrimitives.genRandomBase16String(32).toLowerCase();
		encryptionGroup = GroupTestData.getGqGroup();

		reset(electionEventRepository);
	}

	@Nested
	@DisplayName("saving")
	class SaveTest {

		@Test
		@DisplayName("with any null parameter throws NullPointerException")
		void saveNullParams() {
			assertThrows(NullPointerException.class, () -> electionEventService.save(null, encryptionGroup));
			assertThrows(NullPointerException.class, () -> electionEventService.save(electionEventId, null));
		}

		@Test
		@DisplayName("new encryption parameters saves to database")
		void save() {
			electionEventService.save(electionEventId, encryptionGroup);
			verify(electionEventRepository).save(any());
		}

	}

	@Nested
	@DisplayName("loading")
	class LoadTest {

		@Test
		@DisplayName("with null parameter throws NullPointerException")
		void loadNullParam() {
			assertThrows(NullPointerException.class, () -> electionEventService.getEncryptionGroup(null));
		}

		@Test
		@DisplayName("for the first time calls database")
		void firstTimeLoad() {
			electionEventService.save(electionEventId, encryptionGroup);

			final GqGroup loadedGroup = electionEventService.getEncryptionGroup(electionEventId);
			assertEquals(encryptionGroup, loadedGroup);

			verify(electionEventRepository).findByElectionEventId(electionEventId);
		}

		@Test
		@DisplayName("for the second time uses cache")
		void secondLoadUsesCache() {
			electionEventService.save(electionEventId, encryptionGroup);

			electionEventService.getEncryptionGroup(electionEventId);
			electionEventService.getEncryptionGroup(electionEventId);

			verify(electionEventRepository, times(1)).findByElectionEventId(electionEventId);
		}

		@Test
		@DisplayName("non existent election throws IllegalStateException")
		void nonExistentElection() {
			final String nonExistentId = cryptoPrimitives.genRandomBase16String(32).toLowerCase();

			final IllegalStateException exception = assertThrows(IllegalStateException.class,
					() -> electionEventService.getEncryptionGroup(nonExistentId));

			final String expectedMessage = String.format("Encryption group not found. [electionEventId: %s]", nonExistentId);
			assertEquals(expectedMessage, Throwables.getRootCause(exception).getMessage());
		}

	}

}