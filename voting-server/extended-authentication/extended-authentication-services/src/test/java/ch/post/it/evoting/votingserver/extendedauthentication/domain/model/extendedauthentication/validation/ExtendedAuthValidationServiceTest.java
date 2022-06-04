/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.validation;

import static ch.post.it.evoting.votingserver.extendedauthentication.domain.utils.BeanUtils.createAuthenticationToken;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.inject.Instance;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.domain.election.model.Information.VoterInformation;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdate;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdateRequest;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenRepositoryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ExtendedAuthValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthentication;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.validation.rules.CertificateValidation;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.validation.rules.CredentialIdValidation;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence.ExtendedAuthenticationService;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.client.AuthenticationClient;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.client.CertificateChainValidationRequest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

@RunWith(MockitoJUnitRunner.class)
public class ExtendedAuthValidationServiceTest {

	public static final String TENANT_ID = "tenant";

	public static final String ELECTION_EVENT_ID = "100";

	public static final String A_COMPLETELY_DIFFERENT_ID = "A_COMPLETELY_DIFFERENT_ID";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Rule
	public TestRule restoreSystemProperties = new RestoreSystemProperties();

	@Mock
	Instance<CertificateValidation> certificateValidationsMock;
	String mockJwtSignature = "eyJhbGciOiJQUzI1NiIsICJjdHkiOiJKV1QifQ.eyJvbGRBdXRoSUQiOiIxYTg3YjM0ODhhMDg3ZjE2NDZjMDg5MjM1ZmUxYzNlOSIsIm5ld0F1dGhJRCI6ImE0ZWEyNDg5ZWUzZTFjNzFhNmQ3MTJkNjBhYjNhMmYyIiwibmV3U1ZLIjoiNmllOHpZZE5PQ2ZZSnZpWDYwVFd4eTdLOXhXeFY5cDRkNWJVWHN4MU96OVpzdnlUOWFuTkN2S01qZ3kyZENGNyIsImF1dGhlbnRpY2F0aW9uVG9rZW5TaWduYXR1cmUiOiJISXVZT0U3dG9EbkNOSldGbkd6L1ozSlZtenlpbkxoVWF1V1lIZG4vOCtxYjBraFc5T3RWT1lDTGpndlRsVTNQNDgvUzZuUVRGVHVtZE1SRUpjOTFGMDdmWWtmUTEzZ1hHRDVCUlN1MFhTTDVSMis3Z1Z4d0lFVURJZCsvcXNZdWhYR3VDTTVmdUJqK3dMV2NUbGprd0NwemVXai8wU1Evcmt0NWpzSm9qRmJNdlRkaURRaUNlazhXMnhOTm93UXVKV0VyQTlod3FubDFiZUh5eE1oV1R1aXgxN01ES0dPUjlFVTAwM09pVThaSHB5ZXBad21WWnJCQVYzRXZwM2FLZzUwcEhOcTBIQk1GTlk2UVdndnduTzhxd1IrYUNMN0cvTDBIeVN0aUVzcy9hbEY0dDAzMkhMZjVybHA4RDBZNWVyV3duUlAwT0JlMzF2RzJtSlRhUnc9PSJ9.fJf2rEVtTeXnM2LRqEXX9yVk61NMtKXAuh2AR3NLn9mRBQ6uau6urIpUvW-4Fthz_8prYdhedmsGIyYUKdJtSsk_9gryPx03WSx_DZU9FY9fdEwlX-4HY0-D-bGzNvUcMi5BAD5-P5kCIxDyF4iOwmDK_JTJH46XMGap1QrCFE99vC07IRBR8KkXxMMb_a7n6qlzenlD85Kok-gZeXQ51RprwQypsOXfIezsS83wfhpzQdAGUOqGGO1CT-UQB7AoVSN9HcEqZ3C50pdNLlIu8qrOkLkKDIMDMV_cJEI6Wzor9FxYUGzJ2q1TaUcbiT0n5LMTL6vM9o3fpqj1F9dyyw";
	String mockAuthTokenSignature = "HIuYOE7toDnCNJWFnGz/Z3JVmzyinLhUauWYHdn/8+qb0khW9OtVOYCLjgvTlU3P48/S6nQTFTumdMREJc91F07fYkfQ13gXGD5BRSu0XSL5R2+7gVxwIEUDId+/qsYuhXGuCM5fuBj+wLWcTljkwCpzeWj/0SQ/rkt5jsJojFbMvTdiDQiCek8W2xNNowQuJWErA9hwqnl1beHyxMhWTuix17MDKGOR9EU003OiU8ZHpyepZwmVZrBAV3Evp3aKg50pHNq0HBMFNY6QWgvwnO8qwR+aCL7G/L0HyStiEss/alF4t032HLf5rlp8D0Y5erWwnRP0OBe31vG2mJTaRw==";
	String mockAuthTokenWrongSignature = "WrongE7toDnCNJWFnGz/Z3JVmzyinLhUauWYHdn/8+qb0khW9OtVOYCLjgvTlU3P48/S6nQTFTumdMREJc91F07fYkfQ13gXGD5BRSu0XSL5R2+7gVxwIEUDId+/qsYuhXGuCM5fuBj+wLWcTljkwCpzeWj/0SQ/rkt5jsJojFbMvTdiDQiCek8W2xNNowQuJWErA9hwqnl1beHyxMhWTuix17MDKGOR9EU003OiU8ZHpyepZwmVZrBAV3Evp3aKg50pHNq0HBMFNY6QWgvwnO8qwR+aCL7G/L0HyStiEss/alF4t032HLf5rlp8D0Y5erWwnRP0OBe31vG2mJTaRw==";
	String mockCertificate = "-----BEGIN CERTIFICATE-----\n" + "MIIDoDCCAoigAwIBAgIUWVqY4Lr0xNeOemlhuYJLYGhFvr4wDQYJKoZIhvcNAQEL\n"
			+ "BQAwfjE4MDYGA1UEAwwvQ3JlZGVudGlhbHMgQ0EgY2QxZjY5MmRhNzJjNGQxZGJj\n"
			+ "NmYzZjE4MDRiYzQ5NGExFjAUBgNVBAsMDU9ubGluZSBWb3RpbmcxEjAQBgNVBAoM\n"
			+ "CVN3aXNzUG9zdDEJMAcGA1UEBwwAMQswCQYDVQQGEwJDSDAeFw0xNjExMDkxMzIx\n"
			+ "MzNaFw0xODExMDEyMDAwMDBaMHQxLjAsBgNVBAMMJVNpZ24gYWU5MjU1NDhjMTM1\n"
			+ "MDRhODIyNThlODkzZGFmNDBjZjYxFjAUBgNVBAsMDU9ubGluZSBWb3RpbmcxEjAQ\n"
			+ "BgNVBAoMCVN3aXNzUG9zdDEJMAcGA1UEBwwAMQswCQYDVQQGEwJDSDCCASIwDQYJ\n"
			+ "KoZIhvcNAQEBBQADggEPADCCAQoCggEBAKiBJO0p1bDAbdFNoCFrSC3WNR6tN4Wv\n"
			+ "v9TP3BoE+RVu9riEdRstdU+2spUn731mIxcPgyyMlkUEUja/6CDZQ9NxiQEi/7Vl\n"
			+ "0ZHdicq7V8boG1QRTFoRv0Z3kooV2HddENOrXj4+ZkXMoAPTW9+7ksIctqjQTIwp\n"
			+ "6iG6LZ0VzKqgYsalzdS4NKZl+koN7ex+FAvdH7Q6Bea/7ZI0bzW+NEtLzCrO+koc\n"
			+ "8oRxmvpGoJT8Ea+/nJqWsKKdV0HcpJJg0wE4AK4SOCkiwqKMQcB5vy1T601Ho0v3\n"
			+ "Pjzxade/tzo7Qr47/9LbmANdmK4Shs4NF4U4/bC2/B/pJlX4Xn4AXM0CAwEAAaMg\n"
			+ "MB4wDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMCBsAwDQYJKoZIhvcNAQELBQAD\n"
			+ "ggEBAK9v+ZqogxwRHPKoYWZZPO+al3JCO/52Kgh1itBfNzMvIaygxKCqT/iyC4mY\n"
			+ "1+LM7oQ6Snzd9i2G5GZO1fQ7SKqh0Q4yrVkCQ+X4D9qqxM2xsLlyo5yZn3ffMSiF\n"
			+ "RoFxIoLOwubyy9MoVcDfViut27T2Nt7MgDVhgz+zU6XwKyMl2JlBgFfDKGujybI9\n"
			+ "wtqJNreApvnw87EdnrxcZUAuQ78sdQMHIU5MTWMqIcd3Gaj5qx/ljSojDAuNepMt\n"
			+ "Qy1JV7GnqCucdByVImiT7wRC0KbYSYx76QVj5Su5SyYX+ouwmDEtlAT0aQlwnuvl\n" + "wls4Hn5S02qqgxhv7AcapTvuvPU=\n" + "-----END CERTIFICATE-----";
	@Mock
	private ExtendedAuthenticationService extendedAuthenticationService;
	@Mock
	private ExtendedAuthentication extendedAuthentication;
	@Mock
	private AuthenticationClient authenticationClient;
	@InjectMocks
	private final ExtendedAuthValidationServiceImpl extendendAuthValidationService = new ExtendedAuthValidationServiceImpl(authenticationClient);
	@Mock
	private AuthenticationTokenService authenticationTokenService;
	@Mock
	private CredentialIdValidation credentialIdValidation;

	@BeforeClass
	public static void setup() {
		MockitoAnnotations.initMocks(ExtendedAuthValidationServiceTest.class);
	}

	/**
	 * Test validate authentication with correct signature
	 */
	@Test
	public void validateHappyPath() {

		ExtendedAuthenticationUpdateRequest mockExtendedAuthenticationUpdateRequest = new ExtendedAuthenticationUpdateRequest();
		mockExtendedAuthenticationUpdateRequest.setCertificate(mockCertificate);
		mockExtendedAuthenticationUpdateRequest.setSignature(mockJwtSignature);

		AuthenticationToken mockAuthenticationToken = new AuthenticationToken();
		mockAuthenticationToken.setSignature(mockAuthTokenSignature);

		ExtendedAuthenticationUpdate mockExtendedAuthenticationUpdateCorrectSignature = new ExtendedAuthenticationUpdate();
		mockExtendedAuthenticationUpdateCorrectSignature.setAuthenticationTokenSignature(mockAuthTokenSignature);

		assertThat(extendendAuthValidationService.verifySignature(mockExtendedAuthenticationUpdateRequest, mockAuthenticationToken),
				instanceOf(ExtendedAuthenticationUpdate.class));

	}

	/**
	 * Test validate authentication wrong token signature
	 */
	@Test
	public void validateWrongSignature() {

		ExtendedAuthenticationUpdateRequest mockExtendedAuthenticationUpdateRequest = new ExtendedAuthenticationUpdateRequest();

		mockExtendedAuthenticationUpdateRequest.setCertificate(mockCertificate);
		mockExtendedAuthenticationUpdateRequest.setSignature(mockJwtSignature);

		VoterInformation mockVoterInformation = new VoterInformation();
		mockVoterInformation.setVotingCardId("votingCardId");
		mockVoterInformation.setElectionEventId("electionEventId");

		AuthenticationToken mockAuthenticationToken = new AuthenticationToken();
		mockAuthenticationToken.setSignature(mockAuthTokenWrongSignature);
		mockAuthenticationToken.setVoterInformation(mockVoterInformation);

		ExtendedAuthenticationUpdate mockExtendedAuthenticationUpdateCorrectSignature = new ExtendedAuthenticationUpdate();
		mockExtendedAuthenticationUpdateCorrectSignature.setAuthenticationTokenSignature(mockAuthTokenWrongSignature);

		ExtendedAuthValidationException caughtException = null;

		try {
			extendendAuthValidationService.verifySignature(mockExtendedAuthenticationUpdateRequest, mockAuthenticationToken);

		} catch (ExtendedAuthValidationException e) {
			caughtException = e;
		}
		assertThat(caughtException, instanceOf(ExtendedAuthValidationException.class));
		assertThat(caughtException.getErrorType(), is(ValidationErrorType.INVALID_SIGNATURE));

	}

	@Test
	public void validateInvalidCertificate() {

		ExtendedAuthenticationUpdateRequest mockExtendedAuthenticationUpdateRequest = new ExtendedAuthenticationUpdateRequest();

		String wrongMockCertificate = mockCertificate.replace("-----BEGIN CERTIFICATE-----", "");

		mockExtendedAuthenticationUpdateRequest.setCertificate(wrongMockCertificate);
		mockExtendedAuthenticationUpdateRequest.setSignature(mockJwtSignature);

		VoterInformation mockVoterInformation = new VoterInformation();
		mockVoterInformation.setVotingCardId("votingCardId");
		mockVoterInformation.setElectionEventId("electionEventId");

		AuthenticationToken mockAuthenticationToken = new AuthenticationToken();
		mockAuthenticationToken.setSignature(mockAuthTokenSignature);
		mockAuthenticationToken.setVoterInformation(mockVoterInformation);

		ExtendedAuthenticationUpdate mockExtendedAuthenticationUpdateCorrectSignature = new ExtendedAuthenticationUpdate();
		mockExtendedAuthenticationUpdateCorrectSignature.setAuthenticationTokenSignature(mockAuthTokenSignature);

		ExtendedAuthValidationException caughtException = null;

		try {
			extendendAuthValidationService.verifySignature(mockExtendedAuthenticationUpdateRequest, mockAuthenticationToken);

		} catch (ExtendedAuthValidationException e) {
			caughtException = e;
		}
		assertThat(caughtException, instanceOf(ExtendedAuthValidationException.class));
		assertThat(caughtException.getErrorType(), is(ValidationErrorType.INVALID_CERTIFICATE));

	}

	@Test
	public void validateWrongJwt() {

		Key incorrectKey = new SecretKeySpec("WrongKey".getBytes(StandardCharsets.UTF_8), "WrongAlgorithm");

		LocalDateTime localDateTime = LocalDateTime.now();
		Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
		Date expirationDate = Date.from(instant);

		String wrongJwtSignature = Jwts.builder().setIssuedAt(new Date()).setExpiration(expirationDate)
				.signWith(SignatureAlgorithm.HS512, incorrectKey).compressWith(CompressionCodecs.DEFLATE).compact();

		ExtendedAuthenticationUpdateRequest mockExtendedAuthenticationUpdateRequest = new ExtendedAuthenticationUpdateRequest();

		mockExtendedAuthenticationUpdateRequest.setCertificate(mockCertificate);
		mockExtendedAuthenticationUpdateRequest.setSignature(wrongJwtSignature);

		VoterInformation mockVoterInformation = new VoterInformation();
		mockVoterInformation.setVotingCardId("votingCardId");
		mockVoterInformation.setElectionEventId("electionEventId");

		AuthenticationToken mockAuthenticationToken = new AuthenticationToken();
		mockAuthenticationToken.setSignature(wrongJwtSignature);
		mockAuthenticationToken.setVoterInformation(mockVoterInformation);

		ExtendedAuthenticationUpdate mockExtendedAuthenticationUpdateCorrectSignature = new ExtendedAuthenticationUpdate();
		mockExtendedAuthenticationUpdateCorrectSignature.setAuthenticationTokenSignature(wrongJwtSignature);

		ExtendedAuthValidationException caughtException = null;

		try {
			extendendAuthValidationService.verifySignature(mockExtendedAuthenticationUpdateRequest, mockAuthenticationToken);

		} catch (ExtendedAuthValidationException e) {
			caughtException = e;
		}

		assertThat(caughtException, instanceOf(ExtendedAuthValidationException.class));
		assertThat(caughtException.getErrorType(), is(ValidationErrorType.INVALID_SIGNATURE));

	}

	@Test
	public void validateToken() throws AuthTokenRepositoryException {
		when(authenticationTokenService.validateToken(anyString(), anyString(), any(AuthenticationToken.class)))
				.thenReturn(new ValidationResult(true));
		final boolean result = extendendAuthValidationService.validateToken(TENANT_ID, ELECTION_EVENT_ID, createAuthenticationToken());

		assertThat(result, is(true));
	}

	@Test
	public void validateTokenAndFail() throws AuthTokenRepositoryException {

		expectedException.expect(AuthTokenValidationException.class);
		when(authenticationTokenService.validateToken(anyString(), anyString(), any(AuthenticationToken.class)))
				.thenReturn(new ValidationResult(false));

		extendendAuthValidationService.validateToken(TENANT_ID, ELECTION_EVENT_ID, createAuthenticationToken());

	}

	@Test
	public void validateCertificates() {

		expectedException.expect(ExtendedAuthValidationException.class);
		doThrow(ExtendedAuthValidationException.class).when(credentialIdValidation).validateCertificate(anyString(), any(AuthenticationToken.class));
		List<CertificateValidation> validations = Collections.singletonList(credentialIdValidation);
		when(certificateValidationsMock.iterator()).thenReturn(validations.iterator());

		extendendAuthValidationService.setValidations(certificateValidationsMock);
		extendendAuthValidationService.validateCertificate(mockCertificate, createAuthenticationToken());

	}

	@Test
	public void validateTokenWithCredentialId() throws ResourceNotFoundException, ApplicationException {

		final AuthenticationToken authenticationToken = createAuthenticationToken();
		final ExtendedAuthenticationUpdate extendedAuthenticationUpdate = new ExtendedAuthenticationUpdate();
		when(extendedAuthenticationService.retrieveExistingExtendedAuthenticationForRead(anyString(), any(), anyString()))
				.thenReturn(extendedAuthentication);
		when(extendedAuthentication.getCredentialId()).thenReturn(authenticationToken.getVoterInformation().getCredentialId());

		extendendAuthValidationService
				.validateTokenWithAuthIdAndCredentialId(authenticationToken, extendedAuthenticationUpdate, TENANT_ID, ELECTION_EVENT_ID);

	}

	@Test
	public void validateTokenWithCredentialIdFails() throws ResourceNotFoundException, ApplicationException {

		expectedException.expect(ExtendedAuthValidationException.class);
		final AuthenticationToken authenticationToken = createAuthenticationToken();
		final ExtendedAuthenticationUpdate extendedAuthenticationUpdate = new ExtendedAuthenticationUpdate();
		when(extendedAuthenticationService.retrieveExistingExtendedAuthenticationForRead(anyString(), any(), anyString()))
				.thenReturn(extendedAuthentication);
		when(extendedAuthentication.getCredentialId()).thenReturn(A_COMPLETELY_DIFFERENT_ID);

		extendendAuthValidationService
				.validateTokenWithAuthIdAndCredentialId(authenticationToken, extendedAuthenticationUpdate, TENANT_ID, ELECTION_EVENT_ID);

	}

	@Test
	public void validateCertificateChain() throws IOException {

		final AuthenticationToken authenticationToken = createAuthenticationToken();

		@SuppressWarnings("unchecked")
		Call<ValidationResult> callMock = (Call<ValidationResult>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(new ValidationResult(true)));

		when(authenticationClient.validateCertificateChain(anyString(), any(), any(CertificateChainValidationRequest.class))).thenReturn(callMock);
		extendendAuthValidationService.validateCertificateChain(TENANT_ID, ELECTION_EVENT_ID, mockCertificate, authenticationToken);

	}

	@Test
	public void validateCertificateChainFails() throws IOException {

		expectedException.expect(ExtendedAuthValidationException.class);
		final AuthenticationToken authenticationToken = createAuthenticationToken();

		@SuppressWarnings("unchecked")
		Call<ValidationResult> callMock = (Call<ValidationResult>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(new ValidationResult(false)));

		when(authenticationClient.validateCertificateChain(anyString(), any(), any(CertificateChainValidationRequest.class))).thenReturn(callMock);
		extendendAuthValidationService.validateCertificateChain(TENANT_ID, ELECTION_EVENT_ID, mockCertificate, authenticationToken);

	}

	@Test
	public void validateCertificateChainRetrofitError() throws IOException {

		expectedException.expect(ExtendedAuthValidationException.class);
		final AuthenticationToken authenticationToken = createAuthenticationToken();

		@SuppressWarnings("unchecked")
		Call<ValidationResult> callMock = (Call<ValidationResult>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.error(404, ResponseBody.create(okhttp3.MediaType.parse("error"), "error")));

		when(authenticationClient.validateCertificateChain(anyString(), any(), any(CertificateChainValidationRequest.class))).thenReturn(callMock);
		extendendAuthValidationService.validateCertificateChain(TENANT_ID, ELECTION_EVENT_ID, mockCertificate, authenticationToken);

	}
}
