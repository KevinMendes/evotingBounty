/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * JUnit for the class {@link SettingsJson2EEPropertiesMapper}
 */
@ExtendWith(MockitoExtension.class)
class SettingsJson2EEPropertiesMapperTest {

	private final SettingsJson2EEPropertiesMapper settingsJson2EEPropertiesMapper = new SettingsJson2EEPropertiesMapper();

	private String electionEventAsJson = null;

	@Test
	void createConfigPropertiesFromJson4Null() {
		Properties properties = settingsJson2EEPropertiesMapper.createConfigPropertiesFromJson(electionEventAsJson);

		assertTrue(properties.isEmpty());
	}

	@Test
	void createConfigPropertiesFromJson4Empty() {
		electionEventAsJson = "";
		Properties properties = settingsJson2EEPropertiesMapper.createConfigPropertiesFromJson(electionEventAsJson);

		assertTrue(properties.isEmpty());
	}

	@Test
	void createConfigPropertiesFromJson4Blank() {
		electionEventAsJson = " ";
		Properties properties = settingsJson2EEPropertiesMapper.createConfigPropertiesFromJson(electionEventAsJson);

		assertTrue(properties.isEmpty());
	}

	@Test
	void createConfigPropertiesFromJson() {
		final int AUTH_TOKEN_EXP_TIME = 1600;
		final int CHALLENGE_LENGTH = 16;
		final int MAX_NR_ATTEMPTS = 5;
		electionEventAsJson = "{\"id\":\"db033b3f729c45719db8aba15d24043c\"," + "\"defaultTitle\":\"Election Event Title\","
				+ "\"defaultDescription\":\"Election Event Description\"," + "\"alias\":\"Election Event Alias\","
				+ "\"dateFrom\":\"12/12/2012 10:30\"," + "\"dateTo\":\"14/12/2012 10:30\"," + "\"settings\":{" + "\"certificatesValidityPeriod\":1,"
				+ "\"challengeLength\":" + CHALLENGE_LENGTH + "," + "\"challengeResponseExpirationTime\":2000," + "\"authTokenExpirationTime\":"
				+ AUTH_TOKEN_EXP_TIME + "," + "\"numberVotesPerVotingCard\":1," + "\"numberVotesPerAuthToken\":1," + "\"maximumNumberOfAttempts\":"
				+ MAX_NR_ATTEMPTS + "," + "\"encryptionParameters\":{" + "\"p\":\"123\"," + "\"q\":\"456\"," + "\"g\":\"3\"" + "}" + "}}}";
		Properties properties = settingsJson2EEPropertiesMapper.createConfigPropertiesFromJson(electionEventAsJson);

		assertFalse(properties.isEmpty());
		assertEquals(properties.getProperty(SettingsJson2EEPropertiesMapper.CONFIG_PROPERTY_NAME_AUTH_TOKEN_EXP_TIME),
				String.valueOf(AUTH_TOKEN_EXP_TIME));
		assertEquals(properties.getProperty(SettingsJson2EEPropertiesMapper.CONFIG_PROPERTY_NAME_CHALLENGE_LENGTH), String.valueOf(CHALLENGE_LENGTH));
		assertEquals(properties.getProperty(SettingsJson2EEPropertiesMapper.CONFIG_PROPERTY_NAME_MAX_NUMBER_OF_ATTEMPTS),
				String.valueOf(MAX_NR_ATTEMPTS));
		assertNotEquals(properties.getProperty(SettingsJson2EEPropertiesMapper.CONFIG_PROPERTY_NAME_AUTH_TOKEN_EXP_TIME),
				String.valueOf(CHALLENGE_LENGTH));
		assertNotEquals(properties.getProperty(SettingsJson2EEPropertiesMapper.CONFIG_PROPERTY_NAME_CHALLENGE_LENGTH),
				String.valueOf(MAX_NR_ATTEMPTS));
		assertNotEquals(properties.getProperty(SettingsJson2EEPropertiesMapper.CONFIG_PROPERTY_NAME_MAX_NUMBER_OF_ATTEMPTS),
				String.valueOf(AUTH_TOKEN_EXP_TIME));
	}
}
