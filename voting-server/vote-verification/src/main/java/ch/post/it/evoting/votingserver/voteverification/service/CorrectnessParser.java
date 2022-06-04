/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;

/**
 * Utility class that parses a string (that may contain an array of arrays of strings, represented
 * as a string) into its object representation.
 */
public class CorrectnessParser {

	/**
	 * The result of parsing and formating the received string.
	 * <p>
	 * This method expects strings of the following format:
	 * "[["6c"],[],["71","96","b9"],["71","96","b9"],["71","96","b9"],[],["f7","77","1c"]]"
	 * <p>
	 * Note: the string above contains double quotes inside of the string, these are escaped however
	 * for the sake of readability, the escape characters are not shown here.
	 * <p>
	 * The above string would result in the following List:
	 * <ol>
	 * <li>["6c"]</li>
	 * <li>[]</li>
	 * <li>["71","96","b9"</li>
	 * <li>["71","96","b9"]</li>
	 * <li>["71","96","b9"]</li>
	 * <li>[]</li>
	 * <li>["f7","77","1c"]</li>
	 * </ol>
	 *
	 * @param correctnessIds the array (possibly of arrays) of strings, represented as a single string
	 * @return a list of strings.
	 */
	public List<List<String>> parse(final String correctnessIds) {

		try {
			final String[][] mappedIds = DomainObjectMapper.getNewInstance().readValue(correctnessIds, String[][].class);
			return Arrays.stream(mappedIds).map(Arrays::asList).collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
}
