/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.ws.application.operation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.IOException;
import java.security.cert.CertificateException;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.validation.AuthenticationTokenValidationService;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;

@RunWith(MockitoJUnitRunner.class)
public class ValidationAuthenticationTokenResourceTest {

	private static final String VALID_AUTH_TOKEN =
			"{\n" + "    \"id\": \"A+nlb4FbFY3u0qBH4jcUfA==\",\n" + "    \"voterInformation\": {\n" + "      \"tenantId\": \"100\",\n"
					+ "      \"electionEventId\": \"3a2434c5a1004d71ac53b55d3ccdbfb8\",\n"
					+ "      \"votingCardId\": \"1f5e2153c3ab4657b79d9d4b61a228b7\",\n"
					+ "      \"ballotId\": \"b7e28ca876364dfa9a9315d795f59172\",\n"
					+ "      \"credentialId\": \"a82638e3471b5c3d8d3581a98c53c644\",\n"
					+ "      \"verificationCardId\": \"afeed1c301a34b718a2b4a847344b5d1\",\n"
					+ "      \"ballotBoxId\": \"089378cdc15c480b85560b7f9adcea64\",\n"
					+ "      \"votingCardSetId\": \"4ae2b45680974a9da06ebfc5b13a8685\",\n"
					+ "      \"verificationCardSetId\": \"07f2c3b8ac5b42ddae107f1f0fa3ddeb\"\n" + "    },\n"
					+ "    \"timestamp\": \"1508419659591\",\n"
					+ "    \"signature\": \"TVRB62Zxh+FcJkenvunbT4vglRoXxXyWZXuY8LrTBobl6qADdHIIgdtpNBwkGib2SqquWImIQhqUzzEP3Kicizk31ctR6af7a5z9OO71vBeBns2tfe08nZsV4Niz1WNVa09mjV8skxVEyIqAkd3QIPk76RkCgqsei/rbYs248LGgN8VGHoafcn8hop0Vzrk1+PcL0a6DvAr6wCeDLT1DIEA6af8LTi76lnNR4J+EB4GeUmyRazx6TeqaNzxlZV9J1ep/QYCf6oZcQYYk5eEtIGaDbqD23RBLqhS4wNye/3TEhFpfJXZ2SgWFyN+ii81Opk1kGdnomIBTcqJJuRTNBQ==\"\n"
					+ "  }";
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Mock
	private TrackIdInstance trackIdInstance;
	@Mock
	private AuthenticationTokenValidationService authenticationTokenValidationService;
	@Mock
	@Inject
	private Logger LOGGER;
	@InjectMocks
	private ValidationAuthenticationTokenResource sut;

	@Test
	public void testPassValidation() throws CertificateException, ApplicationException, ResourceNotFoundException, IOException {
		ValidationResult correct = new ValidationResult(true);
		Mockito.when(authenticationTokenValidationService.validate(anyString(), anyString(), anyString(), any())).thenReturn(correct);
		Response validateAuthenticationToken = sut.validateAuthenticationToken("test", "test", "test", "test", VALID_AUTH_TOKEN, null);
		ValidationResult readEntity = (ValidationResult) validateAuthenticationToken.getEntity();
		Assert.assertEquals(correct.isResult(), readEntity.isResult());
	}

	@Test
	public void testExtraFieldValidationFail() throws CertificateException, ApplicationException, ResourceNotFoundException, IOException {
		ValidationResult correct = new ValidationResult(true);
		thrown.expect(ApplicationException.class);
		thrown.expectMessage(ApplicationExceptionMessages.EXCEPTION_MESSAGE_AUTH_TOKEN_INVALID_FORMAT);
		String invalid = VALID_AUTH_TOKEN.concat("invalid content in the authentication header");
		sut.validateAuthenticationToken("test", "test", "test", "test", invalid, null);
		Assert.fail();
	}
}
