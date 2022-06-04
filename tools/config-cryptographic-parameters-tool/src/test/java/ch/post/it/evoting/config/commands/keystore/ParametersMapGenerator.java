/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.keystore;

import java.util.HashMap;
import java.util.Map;

import ch.post.it.evoting.config.CommandParameter;
import ch.post.it.evoting.config.Parameters;

public final class ParametersMapGenerator {

	private ParametersMapGenerator() {
		// utility class
	}

	public static final String ALIAS_VALUE = "alias_value";
	public static final String CERTIFICATE_COMMON_NAME_VALUE = "certificate_common_name_value";
	public static final String CERTIFICATE_STATE_VALUE = "certificate_state_value";
	public static final String CERTIFICATE_COUNTRY_VALUE = "certificate_country_value";
	public static final String CERTIFICATE_ORGANISATION_VALUE = "certificate_organisation_value";
	public static final String CERTIFICATE_LOCALITY_VALUE = "certificate_locality_value";
	public static final String OUT_VALUE = "out_value";
	public static final String VALID_FROM_VALUE = "29/03/1997";
	public static final String VALID_UNTIL_VALUE = "31/03/1997";
	public static final String PASSWORD_LENGTH_VALUE = "20";

	public static Map<String, String> getMapWithAllParameter() {
		final Map<String, String> parameters = getMapWithMandatoryParameter();
		parameters.put(CommandParameter.OUT.getParameterName(), OUT_VALUE);
		parameters.put(CommandParameter.PASSWORD_LENGTH.getParameterName(), PASSWORD_LENGTH_VALUE);
		return parameters;
	}

	public static Map<String, String> getMapWithMandatoryParameter() {
		final Map<String, String> parameters = new HashMap<>();
		parameters.put(CommandParameter.ALIAS.getParameterName(), ALIAS_VALUE);
		parameters.put(CommandParameter.VALID_FROM.getParameterName(), VALID_FROM_VALUE);
		parameters.put(CommandParameter.VALID_UNTIL.getParameterName(), VALID_UNTIL_VALUE);
		parameters.put(CommandParameter.CERTIFICATE_COMMON_NAME.getParameterName(), CERTIFICATE_COMMON_NAME_VALUE);
		parameters.put(CommandParameter.CERTIFICATE_STATE.getParameterName(), CERTIFICATE_STATE_VALUE);
		parameters.put(CommandParameter.CERTIFICATE_COUNTRY.getParameterName(), CERTIFICATE_COUNTRY_VALUE);
		parameters.put(CommandParameter.CERTIFICATE_ORGANISATION.getParameterName(), CERTIFICATE_ORGANISATION_VALUE);
		parameters.put(CommandParameter.CERTIFICATE_LOCALITY.getParameterName(), CERTIFICATE_LOCALITY_VALUE);
		return parameters;
	}

	public static Parameters mapToParameters(Map<String, String> map) {
		final Parameters parameters = new Parameters();
		map.forEach(parameters::addParam);
		return parameters;
	}
}
