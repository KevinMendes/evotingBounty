/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

@DisplayName("EncryptedNodeLongCodeSharesService")
class EncryptedNodeLongReturnCodeSharesServiceTest {

	private static final String WRONG_ID = "0123456789abcdef0123456789abcdef";
	private static final String ELECTION_EVENT_ID = "8272a8061fb74d4bacfdf334d618636a";
	private static final String VERIFICATION_CARD_SET_ID = "0e8a969a7f134428901ada7f9b4bb940";

	private static EncryptedNodeLongReturnCodeSharesService encryptedNodeLongReturnCodeSharesService;

	@BeforeAll
	static void setUpAll() throws URISyntaxException {

		final Path path = Paths.get(EncryptedNodeLongReturnCodeSharesService.class.getResource("/encryptedNodeLongCodeSharesServiceTest").toURI());
		final PathResolver pathResolver = new PathResolver(path.toString());
		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		final NodeContributionsResponsesFileRepository nodeContributionsResponsesFileRepository = new NodeContributionsResponsesFileRepository(
				objectMapper, pathResolver);
		final NodeContributionsResponsesService nodeContributionsResponsesService = new NodeContributionsResponsesService(
				nodeContributionsResponsesFileRepository);

		encryptedNodeLongReturnCodeSharesService = new EncryptedNodeLongReturnCodeSharesService(nodeContributionsResponsesService);
	}

	@Test
	@DisplayName("Combine node contributions")
	void combine() {
		final EncryptedNodeLongReturnCodeShares encryptedNodeLongReturnCodeShares = encryptedNodeLongReturnCodeSharesService.load(ELECTION_EVENT_ID,
				VERIFICATION_CARD_SET_ID);

		final int expectedNodeSize = 4;
		final int expectedListSize = 90;

		assertAll(
				() -> assertEquals(ELECTION_EVENT_ID, encryptedNodeLongReturnCodeShares.getElectionEventId()),
				() -> assertEquals(VERIFICATION_CARD_SET_ID, encryptedNodeLongReturnCodeShares.getVerificationCardSetId()),
				() -> assertEquals(expectedListSize, encryptedNodeLongReturnCodeShares.getVerificationCardIds().size(), "Verification card ids size"),
				() -> assertEquals(expectedNodeSize, encryptedNodeLongReturnCodeShares.getNodeReturnCodesValues().size(),
						"Node return codes values size"),
				() -> assertEquals(expectedListSize,
						encryptedNodeLongReturnCodeShares.getNodeReturnCodesValues().get(0).getExponentiatedEncryptedConfirmationKeys().size(),
						"Exponentiated confirmation key size"),
				() -> assertEquals(expectedListSize,
						encryptedNodeLongReturnCodeShares.getNodeReturnCodesValues().get(0).getExponentiatedEncryptedPartialChoiceReturnCodes()
								.size(),
						"Exponentiated partial choice return codes size")
		);
	}

	@Test
	@DisplayName("Combine node contributions with wrong ids throws")
	void combineWithWrongIds() {
		assertAll(
				() -> assertThrows(IllegalStateException.class,
						() -> encryptedNodeLongReturnCodeSharesService.load(WRONG_ID, VERIFICATION_CARD_SET_ID)),
				() -> assertThrows(IllegalStateException.class,
						() -> encryptedNodeLongReturnCodeSharesService.load(ELECTION_EVENT_ID, WRONG_ID))
		);
	}

}