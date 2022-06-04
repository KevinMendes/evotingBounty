/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ChoiceCodeGenerationDTO;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationResponsePayload;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

@DisplayName("NodeContributionsResponsesFileRepository")
class NodeContributionsResponsesFileRepositoryTest {

	private static final String WRONG_ID = "0123456789abcdef0123456789abcdef";
	private static final String ELECTION_EVENT_ID = "8272a8061fb74d4bacfdf334d618636a";
	private static final String VERIFICATION_CARD_SET_ID = "0e8a969a7f134428901ada7f9b4bb940";

	private static final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

	private static NodeContributionsResponsesFileRepository nodeContributionsResponsesRepository;

	@BeforeAll
	static void setUpAll() throws URISyntaxException {

		final Path path = Paths.get(
				NodeContributionsResponsesFileRepository.class.getResource("/nodeContributionsResponsesFileRepositoryTest/valid").toURI());
		final PathResolver pathResolver = new PathResolver(path.toString());

		nodeContributionsResponsesRepository = new NodeContributionsResponsesFileRepository(objectMapper, pathResolver);
	}

	@Test
	@DisplayName("Find all")
	void findAll() {
		final List<List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>>> nodeContributions = nodeContributionsResponsesRepository.findAll(
				ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID);

		assertAll(
				() -> assertEquals(9, nodeContributions.size()),
				() -> assertEquals(4, nodeContributions.get(0).size()),
				() -> assertEquals(ELECTION_EVENT_ID, nodeContributions.get(0).get(0).getPayload().getElectionEventId()),
				() -> assertEquals(VERIFICATION_CARD_SET_ID, nodeContributions.get(0).get(0).getPayload().getVerificationCardSetId())
		);
	}

	@Test
	@DisplayName("Find all with invalid ids throws")
	void findAllWithInvalidIds() {
		assertAll(
				() -> assertThrows(FailedValidationException.class,
						() -> nodeContributionsResponsesRepository.findAll("invalidId", VERIFICATION_CARD_SET_ID)),
				() -> assertThrows(FailedValidationException.class,
						() -> nodeContributionsResponsesRepository.findAll("invalidId", VERIFICATION_CARD_SET_ID))
		);
	}

	@Test
	@DisplayName("Find all with wrong path return empty list")
	void findAllWithWrongPath() {
		assertAll(
				() -> assertEquals(Collections.emptyList(), nodeContributionsResponsesRepository.findAll(WRONG_ID, VERIFICATION_CARD_SET_ID)),
				() -> assertEquals(Collections.emptyList(), nodeContributionsResponsesRepository.findAll(ELECTION_EVENT_ID, WRONG_ID))
		);
	}

	@Test
	@DisplayName("Find all with invalid node contribution throws")
	void findAllWithInvalidNodeContributions() throws URISyntaxException {
		final Path path = Paths.get(
				NodeContributionsResponsesFileRepository.class.getResource("/nodeContributionsResponsesFileRepositoryTest/invalid").toURI());
		final PathResolver pathResolver = new PathResolver(path.toString());

		final NodeContributionsResponsesFileRepository repository = new NodeContributionsResponsesFileRepository(objectMapper, pathResolver);

		final UncheckedIOException exception = assertThrows(UncheckedIOException.class,
				() -> repository.findAll(ELECTION_EVENT_ID, VERIFICATION_CARD_SET_ID));

		assertTrue(exception.getMessage().startsWith("Failed to deserialize the node contributions."));
	}

}