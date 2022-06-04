/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.tally.mixoffline;

import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;
import static ch.post.it.evoting.securedatamanager.tally.mixoffline.VotePrimeFactorsFileRepository.DECOMPRESSED_VOTES_FILENAME;
import static ch.post.it.evoting.securedatamanager.tally.mixoffline.VotePrimeFactorsFileRepository.SEMICOLON_SEPARATOR;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.securedatamanager.services.domain.model.mixing.PrimeFactors;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

@ExtendWith(MockitoExtension.class)
class VotePrimeFactorsFileRepositoryTest {

	private final GqGroup group = GroupTestData.getGroupP59();

	private final String ballotId = "b7e28ca876364dfa9a9315d795f59172";
	private final String ballotBoxId = "089378cdc15c480b85560b7f9adcea64";
	private final String electionEventId = "3a2434c5a1004d71ac53b55d3ccdbfb8";

	private final List<PrimeFactors> selectedEncodedVotingOptions = Arrays.asList(
			new PrimeFactors(ImmutableList.of(
					GqElementFactory.fromValue(BigInteger.valueOf(35), group),
					GqElementFactory.fromValue(BigInteger.ONE, group),
					GqElementFactory.fromValue(BigInteger.valueOf(21), group),
					GqElementFactory.fromValue(BigInteger.valueOf(7), group),
					GqElementFactory.fromValue(BigInteger.valueOf(45), group))),
			new PrimeFactors(ImmutableList.of(
					GqElementFactory.fromValue(BigInteger.valueOf(48), group),
					GqElementFactory.fromValue(BigInteger.valueOf(28), group),
					GqElementFactory.fromValue(BigInteger.valueOf(25), group),
					GqElementFactory.fromValue(BigInteger.valueOf(53), group))));

	@Mock
	private PathResolver pathResolverMock;

	@InjectMocks
	private VotePrimeFactorsFileRepository votePrimeFactorsFileRepository;

	@Test
	void persistDecompressedVotesTest(
			@TempDir
			final Path tempDir) throws IOException {

		when(pathResolverMock.resolveBallotBoxPath(any(), any(), any())).thenReturn(tempDir);

		assertDoesNotThrow(() -> votePrimeFactorsFileRepository
				.saveDecompressedVotes(selectedEncodedVotingOptions, electionEventId, ballotId, ballotBoxId));

		// decompressed votes must contain actorsList.size() lines.
		assertEquals(selectedEncodedVotingOptions.size(), Files.lines(tempDir.resolve(DECOMPRESSED_VOTES_FILENAME)).count());

		// decompressed votes must contain exactly the given input selectedEncodedVotingOptions.
		assertEquals(selectedEncodedVotingOptions, Files.lines(tempDir.resolve(DECOMPRESSED_VOTES_FILENAME))
				.map(line -> Arrays.stream(line.split(SEMICOLON_SEPARATOR + ""))
						.map(encodedVotingOption -> GqElementFactory.fromValue(BigInteger.valueOf(Long.parseLong(encodedVotingOption)), group))
						.collect(Collectors.collectingAndThen(toImmutableList(), PrimeFactors::new))).collect(Collectors.toList()));
	}

	@Test
	void persistDecompressedVotesNullTest() {
		assertAll(
				() -> assertThrows(NullPointerException.class,
						() -> votePrimeFactorsFileRepository.saveDecompressedVotes(null, electionEventId, ballotId, ballotBoxId)),
				() -> assertThrows(NullPointerException.class, () -> votePrimeFactorsFileRepository
						.saveDecompressedVotes(selectedEncodedVotingOptions, electionEventId, null, ballotBoxId)),
				() -> assertThrows(NullPointerException.class, () -> votePrimeFactorsFileRepository
						.saveDecompressedVotes(selectedEncodedVotingOptions, electionEventId, ballotId, null)),
				() -> assertThrows(NullPointerException.class, () -> votePrimeFactorsFileRepository
						.saveDecompressedVotes(selectedEncodedVotingOptions, null, ballotId, ballotBoxId)),
				() -> assertThrows(NullPointerException.class, () -> votePrimeFactorsFileRepository
						.saveDecompressedVotes(selectedEncodedVotingOptions, electionEventId, ballotId, ballotBoxId)));
	}

	@Test
	void persistDecompressedVotesInvalidUUIDTest() {
		assertAll(
				() -> assertThrows(FailedValidationException.class, () -> votePrimeFactorsFileRepository
						.saveDecompressedVotes(selectedEncodedVotingOptions, electionEventId, "123", ballotBoxId)),
				() -> assertThrows(FailedValidationException.class, () -> votePrimeFactorsFileRepository
						.saveDecompressedVotes(selectedEncodedVotingOptions, electionEventId, ballotId, "456")),
				() -> assertThrows(FailedValidationException.class, () -> votePrimeFactorsFileRepository
						.saveDecompressedVotes(selectedEncodedVotingOptions, "789", ballotId, ballotBoxId)));
	}

	@Test
	void persistDecompressedVotesEmptyInputsTest(
			@TempDir
			final Path tempDir) {
		when(pathResolverMock.resolveBallotBoxPath(any(), any(), any())).thenReturn(tempDir);

		final List<PrimeFactors> emptySelectedEncodedVotingOptions = Collections.emptyList();
		assertAll(
				() -> assertDoesNotThrow(() -> votePrimeFactorsFileRepository
						.saveDecompressedVotes(emptySelectedEncodedVotingOptions, electionEventId, ballotId, ballotBoxId)),

				// decompressed votes must be empty.
				() -> assertEquals(0, Files.lines(tempDir.resolve(DECOMPRESSED_VOTES_FILENAME)).count()));

		assertAll(
				() -> assertThrows(FailedValidationException.class, () -> votePrimeFactorsFileRepository
						.saveDecompressedVotes(selectedEncodedVotingOptions, electionEventId, "", ballotBoxId)),
				() -> assertThrows(FailedValidationException.class, () -> votePrimeFactorsFileRepository
						.saveDecompressedVotes(selectedEncodedVotingOptions, electionEventId, ballotId, "")),
				() -> assertThrows(FailedValidationException.class, () -> votePrimeFactorsFileRepository
						.saveDecompressedVotes(selectedEncodedVotingOptions, "", ballotId, ballotBoxId)));
	}

	@Test
	void persistDecompressedVotesExistingFilesTest(
			@TempDir
			final Path tempDir) throws IOException {
		when(pathResolverMock.resolveBallotBoxPath(any(), any(), any())).thenReturn(tempDir);

		assertTrue(tempDir.resolve(DECOMPRESSED_VOTES_FILENAME).toFile().createNewFile());

		assertAll(
				() -> assertDoesNotThrow(() -> votePrimeFactorsFileRepository
						.saveDecompressedVotes(selectedEncodedVotingOptions, electionEventId, ballotId, ballotBoxId)),

				// decompressed votes must contain exactly the given input selectedEncodedVotingOptions.
				() -> assertEquals(selectedEncodedVotingOptions, Files.lines(tempDir.resolve(DECOMPRESSED_VOTES_FILENAME))
						.map(line -> Arrays.stream(line.split(SEMICOLON_SEPARATOR + ""))
								.map(encodedVotingOption -> GqElementFactory.fromValue(BigInteger.valueOf(Long.parseLong(encodedVotingOption)),
										group))
								.collect(Collectors.collectingAndThen(toImmutableList(), PrimeFactors::new))).collect(Collectors.toList())));
	}
}

