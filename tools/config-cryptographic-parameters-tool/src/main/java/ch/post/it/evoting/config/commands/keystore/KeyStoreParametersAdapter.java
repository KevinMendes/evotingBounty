/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.keystore;

import static ch.post.it.evoting.config.CommandParameter.ALIAS;
import static ch.post.it.evoting.config.CommandParameter.CERTIFICATE_COMMON_NAME;
import static ch.post.it.evoting.config.CommandParameter.CERTIFICATE_COUNTRY;
import static ch.post.it.evoting.config.CommandParameter.CERTIFICATE_LOCALITY;
import static ch.post.it.evoting.config.CommandParameter.CERTIFICATE_ORGANISATION;
import static ch.post.it.evoting.config.CommandParameter.CERTIFICATE_STATE;
import static ch.post.it.evoting.config.CommandParameter.OUT;
import static ch.post.it.evoting.config.CommandParameter.PASSWORD_LENGTH;
import static ch.post.it.evoting.config.CommandParameter.VALID_FROM;
import static ch.post.it.evoting.config.CommandParameter.VALID_UNTIL;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.config.Parameters;
import ch.post.it.evoting.cryptoprimitives.signing.AuthorityInformation;

/**
 * Map the internal generic parameters object to a dedicated pojo.
 */
@Service
public final class KeyStoreParametersAdapter {

	/**
	 * Processes {@code receivedParameters} returns an adapted version of the parameters, encapsulated within a {@link KeyStoreParametersContainer}.
	 *
	 * @param receivedParameters the parameters to adapt.
	 * @return the adapted parameters.
	 */
	public KeyStoreParametersContainer adapt(final Parameters receivedParameters) {
		AuthorityInformation authorityInformation = AuthorityInformation.builder()
				.setCommonName(receivedParameters.getParam(CERTIFICATE_COMMON_NAME.getParameterName()))
				.setCountry(receivedParameters.getParam(CERTIFICATE_COUNTRY.getParameterName()))
				.setState(receivedParameters.getParam(CERTIFICATE_STATE.getParameterName()))
				.setLocality(receivedParameters.getParam(CERTIFICATE_LOCALITY.getParameterName()))
				.setOrganisation(receivedParameters.getParam(CERTIFICATE_ORGANISATION.getParameterName()))
				.build();

		return KeyStoreParametersContainer.builder()
				.setAlias(receivedParameters.getParam(ALIAS.getParameterName()))
				.setOutputPath(Paths.get(receivedParameters.getParam(OUT.getParameterName())))
				.setValidFrom(stringToDate(receivedParameters.getParam(VALID_FROM.getParameterName())))
				.setValidUntil(stringToDate(receivedParameters.getParam(VALID_UNTIL.getParameterName())))
				.setAuthorityInformation(authorityInformation)
				.setPasswordLength(parseInt(receivedParameters.getParam(PASSWORD_LENGTH.getParameterName())))
				.build();
	}

	private Date stringToDate(final String date) {
		final String pattern = "dd/MM/yyyy";
		try {
			return new SimpleDateFormat(pattern).parse(date);
		} catch (ParseException e) {
			throw new IllegalArgumentException(format("The date %s do not match the patter %s", date, pattern));
		}
	}
}