/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.tally.mixoffline;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.securedatamanager.services.domain.model.mixing.PrimeFactors;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

/**
 * Writer to persist prime factors as files. Moreover, this class signs the produced files and persists the signature in a separate file with the same
 * filename and a suffix ".metadata".
 */
@Repository
public class VotePrimeFactorsFileRepository {

	@VisibleForTesting
	static final String DECOMPRESSED_VOTES_FILENAME = "decompressedVotes.csv";

	@VisibleForTesting
	static final char SEMICOLON_SEPARATOR = ';';

	private static final CsvMapper CSV_MAPPER = new CsvMapper();
	private static final CsvSchema CSV_SCHEMA_WITH_QUOTE_CHAR = CSV_MAPPER.schemaFor(ImmutableList.class).withColumnSeparator(SEMICOLON_SEPARATOR);
	private static final ObjectWriter CSV_OBJECT_WRITER_WITH_QUOTE_CHAR = CSV_MAPPER.writer(CSV_SCHEMA_WITH_QUOTE_CHAR);
	private static final CsvSchema CSV_SCHEMA_WITHOUT_QUOTE_CHAR = CSV_MAPPER.schemaFor(ImmutableList.class).withColumnSeparator(SEMICOLON_SEPARATOR)
			.withoutQuoteChar();
	private static final ObjectWriter CSV_OBJECT_WRITER_WITHOUT_QUOTE_CHAR = CSV_MAPPER.writer(CSV_SCHEMA_WITHOUT_QUOTE_CHAR);

	private final PathResolver pathResolver;

	public VotePrimeFactorsFileRepository(final PathResolver pathResolver) {
		this.pathResolver = pathResolver;
	}

	/**
	 * Persists the decompressed votes to {@value DECOMPRESSED_VOTES_FILENAME} in the corresponding ballot box directory {@link
	 * PathResolver#resolveBallotBoxPath}.
	 * <p>
	 * The content of {@value DECOMPRESSED_VOTES_FILENAME} is printed separated with a semicolon (";").
	 * <p>
	 * If the files do not exist, they will be created and filled with the expected content. If the files already exist, their content will be
	 * overridden by this call.
	 *
	 * @param selectedEncodedVotingOptions the list containing the selections of all votes in the ballot box. One vote contains a list of GqElements:
	 *                                     the prime numbers representing each selected voting option. Must be non-null.
	 * @param electionEventId              the election event id. Must be non-null and a valid UUID.
	 * @param ballotId                     the ballot id. Must be non-null and a valid UUID.
	 * @param ballotBoxId                  the ballot box id. Must be non-null and a valid UUID.
	 * @throws IllegalArgumentException if the ballot id, the ballot box id or the election event id is not a valid UUID.
	 */
	public void saveDecompressedVotes(final List<PrimeFactors> selectedEncodedVotingOptions, final String electionEventId, final String ballotId,
			final String ballotBoxId) {
		checkNotNull(selectedEncodedVotingOptions);
		validateUUID(ballotId);
		validateUUID(ballotBoxId);
		validateUUID(electionEventId);

		final ImmutableList<ImmutableList<GqElement>> immutableSelectedEncodedVotingOptions = selectedEncodedVotingOptions.stream()
				.map(PrimeFactors::getFactors).map(ImmutableList::copyOf).collect(ImmutableList.toImmutableList());

		final Path ballotBoxPath = pathResolver.resolveBallotBoxPath(electionEventId, ballotId, ballotBoxId);
		final Path decompressedVotesFilePath = ballotBoxPath.resolve(DECOMPRESSED_VOTES_FILENAME);

		saveContent(decompressedVotesFilePath.toFile(), immutableSelectedEncodedVotingOptions.stream()
				.map(list -> list.stream().map(factor -> factor.getValue().toString()).collect(ImmutableList.toImmutableList()))
				.collect(ImmutableList.toImmutableList()), false);
	}

	/**
	 * Persists the given content to the given file.
	 * <p>
	 * The outer list is the lines to be printed and the inner list is each line content. This content is printed separated with a semicolon (";") and
	 * wrapped in double-quotes depending on the value of {@code withQuoteChar}.
	 * <p>
	 * If the file does not exist, it will be created and filled with the given content. If the file already exists, its content will be overridden by
	 * this one.
	 *
	 * @param file          the file to which the content must be written. Must be non-null.
	 * @param content       the content to be written. Must be non-null.
	 * @param withQuoteChar a boolean to indicate whether the content must be wrapped in quotes or not.
	 */
	private void saveContent(final File file, final ImmutableList<ImmutableList<String>> content, final boolean withQuoteChar) {
		checkNotNull(content);
		checkNotNull(file);

		final ObjectWriter objectWriter = withQuoteChar ? CSV_OBJECT_WRITER_WITH_QUOTE_CHAR : CSV_OBJECT_WRITER_WITHOUT_QUOTE_CHAR;
		try {
			objectWriter.writeValue(file, content);
		} catch (final IOException e) {
			throw new UncheckedIOException(String.format("Couldn't create, write or close file %s", file), e);
		}
	}

}
