/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Parameters.
 */
public class Parameters {

	private final Map<String, String> param2value;

	/**
	 * Instantiates a new Parameters.
	 */
	public Parameters() {
		param2value = new HashMap<>();
	}

	/**
	 * Add param.
	 *
	 * @param param the param.
	 * @param value the value.
	 */
	public void addParam(final String param, final String value) {
		param2value.put(param, value);
	}

	/**
	 * Gets param.
	 *
	 * @param param the param.
	 * @return the param.
	 */
	public String getParam(final String param) {
		if (!param2value.containsKey(param)) {
			throw new IllegalArgumentException("The parameter \"" + param + "\" has been requested and could not be found.");
		}
		return param2value.get(param);
	}

	public boolean contains(final String param) {
		return param2value.containsKey(param);
	}
}
