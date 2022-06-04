/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.integration.plugin;

import java.util.HashMap;
import java.util.Map;

public class Parameters {

	private final Map<String, String> parametersMap;

	public Parameters() {

		parametersMap = new HashMap<>();
	}

	public Parameters(final Map<String, String> map) {

		parametersMap = map;
	}

	/**
	 * Add param.
	 *
	 * @param param the param
	 * @param value the value
	 */
	public void addParam(final String param, final String value) {

		parametersMap.put(param, value);

	}

	/**
	 * Gets param.
	 *
	 * @param param the param
	 * @return the param
	 */
	public String getParam(final String param) {

		return parametersMap.get(param);

	}

}
