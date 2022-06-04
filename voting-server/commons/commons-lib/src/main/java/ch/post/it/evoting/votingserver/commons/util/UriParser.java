/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.util;

/**
 * Utility class that provides methods for parsing data from a URI.
 * <p>
 * Instances of this classes are initialized with a given search string. Given this initial state, when passed a URI, this class will attempt to find
 * the value in the URI that corresponds to the search string. This class assumes that the URI will follow the following pattern:
 * /XXXXXXX/KEY/KEY_VALUE/OTHER_KEY/OTHER_KEY_VALUE/YYYYYYYY
 */
public class UriParser {

	private static final String URI_SEPARATOR = "/";

	private static final String EMPTY_STRING = "";

	private final String searchString;

	private final int searchStringLength;

	public UriParser(final String stringToSearchFor) {
		this.searchString = stringToSearchFor;
		this.searchStringLength = searchString.length();
	}

	/**
	 * Given the search string that was set when this object was initialized. Attempt to find the corresponding value in the specified URI string.
	 * <p>
	 * NOTE: if the search string cannot be found in the specified URI string then an empty string is returned.
	 */
	public String getValue(final String uriAsString) {

		int indexSearchString = uriAsString.indexOf(searchString);

		if (indexSearchString != -1) {

			int startIndex = indexSearchString + searchStringLength;
			int endIndex = uriAsString.indexOf(URI_SEPARATOR, startIndex);

			if (endIndex == -1) {
				endIndex = uriAsString.length();
			}

			if (endIndex > startIndex) {
				return uriAsString.substring(startIndex, endIndex);
			}
		}
		return EMPTY_STRING;
	}
}
