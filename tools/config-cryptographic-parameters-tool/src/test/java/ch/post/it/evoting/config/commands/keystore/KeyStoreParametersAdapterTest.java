/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.config.commands.keystore;

import static ch.post.it.evoting.config.commands.keystore.ParametersMapGenerator.ALIAS_VALUE;
import static ch.post.it.evoting.config.commands.keystore.ParametersMapGenerator.CERTIFICATE_COMMON_NAME_VALUE;
import static ch.post.it.evoting.config.commands.keystore.ParametersMapGenerator.CERTIFICATE_COUNTRY_VALUE;
import static ch.post.it.evoting.config.commands.keystore.ParametersMapGenerator.CERTIFICATE_LOCALITY_VALUE;
import static ch.post.it.evoting.config.commands.keystore.ParametersMapGenerator.CERTIFICATE_ORGANISATION_VALUE;
import static ch.post.it.evoting.config.commands.keystore.ParametersMapGenerator.CERTIFICATE_STATE_VALUE;
import static ch.post.it.evoting.config.commands.keystore.ParametersMapGenerator.OUT_VALUE;
import static ch.post.it.evoting.config.commands.keystore.ParametersMapGenerator.PASSWORD_LENGTH_VALUE;
import static ch.post.it.evoting.config.commands.keystore.ParametersMapGenerator.VALID_FROM_VALUE;
import static ch.post.it.evoting.config.commands.keystore.ParametersMapGenerator.VALID_UNTIL_VALUE;
import static ch.post.it.evoting.config.commands.keystore.ParametersMapGenerator.getMapWithAllParameter;
import static ch.post.it.evoting.config.commands.keystore.ParametersMapGenerator.getMapWithMandatoryParameter;
import static ch.post.it.evoting.config.commands.keystore.ParametersMapGenerator.mapToParameters;
import static java.lang.Integer.parseInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.config.Parameters;
import ch.post.it.evoting.cryptoprimitives.signing.AuthorityInformation;

class KeyStoreParametersAdapterTest {

	final KeyStoreParametersAdapter adapter = new KeyStoreParametersAdapter();
	final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

	@Test
	void provideAllValidParameters_holderCreatedWithoutIssue() {
		// given
		final Parameters currentParameter = mapToParameters(getMapWithAllParameter());

		// when
		final KeyStoreParametersContainer holder = adapter.adapt(currentParameter);
		final AuthorityInformation info = holder.getAuthorityInformation();

		// then
		assertThat(holder.getAlias()).isEqualTo(ALIAS_VALUE);
		assertThat(holder.getOutputPath()).isEqualTo(Paths.get(OUT_VALUE));
		assertThat(holder.getPasswordLength()).isEqualTo(parseInt(PASSWORD_LENGTH_VALUE));
		assertThat(formatter.format(holder.getValidFrom())).isEqualTo(VALID_FROM_VALUE);
		assertThat(formatter.format(holder.getValidUntil())).isEqualTo(VALID_UNTIL_VALUE);

		assertThat(info.getCommonName()).isEqualTo(CERTIFICATE_COMMON_NAME_VALUE);
		assertThat(info.getState()).isEqualTo(CERTIFICATE_STATE_VALUE);
		assertThat(info.getCountry()).isEqualTo(CERTIFICATE_COUNTRY_VALUE);
		assertThat(info.getOrganisation()).isEqualTo(CERTIFICATE_ORGANISATION_VALUE);
		assertThat(info.getLocality()).isEqualTo(CERTIFICATE_LOCALITY_VALUE);
	}

	@ParameterizedTest(name = "{0} is expected to be invalid.")
	@MethodSource("invalidParametersProvider")
	void provideOneMissingParameter_holderThrowAnException(String missingParameterName, Map<String, String> parameters) {
		// given
		final Parameters currentParameter = mapToParameters(parameters);

		// when / then
		assertThatThrownBy(() -> adapter.adapt(currentParameter))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining(missingParameterName);
	}

	static Stream<Arguments> invalidParametersProvider() {
		return getMapWithMandatoryParameter().keySet().stream()
				.map(s -> {
					final Map<String, String> map = getMapWithAllParameter();
					map.remove(s);
					return Arguments.of(s, map);
				});
	}
}