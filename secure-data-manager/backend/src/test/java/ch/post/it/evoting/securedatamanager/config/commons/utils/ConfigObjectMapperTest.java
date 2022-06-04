/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.domain.election.AuthenticationVoterData;

class ConfigObjectMapperTest {

	private final ConfigObjectMapper target = new ConfigObjectMapper();

	@Test
	void fromJavaToJSON_and_fromJSONToJava() throws IOException {

		AuthenticationVoterData originalAuthenticationVoterDataContents = createDummyAuthenticationVoterDataContents();

		String json = target.fromJavaToJSON(originalAuthenticationVoterDataContents);

		AuthenticationVoterData returnedAuthenticationVoterDataContents = target.fromJSONToJava(json, AuthenticationVoterData.class);

		compareTwoAuthenticationContents(originalAuthenticationVoterDataContents, returnedAuthenticationVoterDataContents);
	}

	@Test
	void fromJavaToFile_and_fromFileToJava() throws IOException {

		AuthenticationVoterData originalAuthenticationVoterDataContents = createDummyAuthenticationVoterDataContents();

		File dest = new File("./target", "fromJavaToFile_and_fromFileToJava.txt");

		assertFalse(dest.exists());

		target.fromJavaToJSONFile(originalAuthenticationVoterDataContents, dest);

		assertTrue(dest.exists());

		AuthenticationVoterData returnedAuthenticationVoterDataContents = target.fromJSONFileToJava(dest, AuthenticationVoterData.class);

		compareTwoAuthenticationContents(originalAuthenticationVoterDataContents, returnedAuthenticationVoterDataContents);

		// Cleanup
		FileUtils.deleteQuietly(dest);
	}

	/**
	 * @return AuthenticationContents dummy object
	 */
	private AuthenticationVoterData createDummyAuthenticationVoterDataContents() {

		String electionRootCA = "test";
		String authoritiesCA = "test";
		String servicesCA = "test";
		String credentialsCA = "test";
		String authTokenSignerCert = "test";

		AuthenticationVoterData originalAuthenticationContents = new AuthenticationVoterData();
		originalAuthenticationContents.setElectionRootCA(electionRootCA);
		originalAuthenticationContents.setAuthoritiesCA(authoritiesCA);
		originalAuthenticationContents.setServicesCA(servicesCA);
		originalAuthenticationContents.setCredentialsCA(credentialsCA);
		originalAuthenticationContents.setAuthenticationTokenSignerCert(authTokenSignerCert);

		return originalAuthenticationContents;
	}

	private void compareTwoAuthenticationContents(final AuthenticationVoterData originalAuthenticationContents,
			final AuthenticationVoterData returnedAuthenticationContents) {

		assertEquals(originalAuthenticationContents.getElectionRootCA(), returnedAuthenticationContents.getElectionRootCA());
		assertEquals(originalAuthenticationContents.getAuthoritiesCA(), returnedAuthenticationContents.getAuthoritiesCA());
		assertEquals(originalAuthenticationContents.getServicesCA(), returnedAuthenticationContents.getServicesCA());
		assertEquals(originalAuthenticationContents.getCredentialsCA(), returnedAuthenticationContents.getCredentialsCA());
		assertEquals(originalAuthenticationContents.getAuthenticationTokenSignerCert(),
				returnedAuthenticationContents.getAuthenticationTokenSignerCert());
	}
}
