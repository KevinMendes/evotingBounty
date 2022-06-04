/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.helpers;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class holds replacements that are pairs composed of placeholders and value. The replacements can be applied to String that have the expected
 * replacements. By default has two pairs eeid and id.
 */
public class ReplacementsHolder {

	private static final String EEID_PLACEHOLDER = "\\$\\{eeid\\}";
	private static final String ID_PLACEHOLDER = "\\$\\{id\\}";

	private final Map<String, String> replacements = new LinkedHashMap<>();

	public ReplacementsHolder(final String eeid) {
		addReplacement(EEID_PLACEHOLDER, eeid);
	}

	public ReplacementsHolder(final String eeid, final String id) {
		addReplacement(EEID_PLACEHOLDER, eeid);
		addReplacement(ID_PLACEHOLDER, id);
	}

	/**
	 * Adds a new replacements to the collection of replacements.
	 *
	 * @param placeHolder the place holder that will be find and replaced
	 * @param value       the value that will replace the placeHolder
	 */
	public void addReplacement(final String placeHolder, final String value) {
		replacements.put(placeHolder, value);
	}

	/**
	 * Apply all the replacements to the input string.
	 *
	 * @param text the string to apply the replacements
	 * @return the input string after apply all the replacements
	 */
	public String applyReplacements(final String text) {

		String result = text;
		for (final Map.Entry<String, String> entry : replacements.entrySet()) {
			result = result.replaceAll(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
