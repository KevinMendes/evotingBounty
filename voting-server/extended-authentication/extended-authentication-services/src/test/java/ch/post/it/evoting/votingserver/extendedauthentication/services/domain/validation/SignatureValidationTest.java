/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.domain.validation;

import static org.junit.Assert.assertThrows;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.cert.Certificate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.commons.serialization.JsonSignatureService;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdate;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdateRequest;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ExtendedAuthValidationException;
import ch.post.it.evoting.votingserver.commons.verify.JSONVerifier;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.validation.ExtendedAuthValidationServiceImpl;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.client.AuthenticationClient;

@RunWith(MockitoJUnitRunner.class)
public class SignatureValidationTest extends ValidationTest {

	public static final String COMMON_NAME = "1";

	@Mock
	AuthenticationClient auRestClient;

	@InjectMocks
	ExtendedAuthValidationServiceImpl extendendAuthValidationService = new ExtendedAuthValidationServiceImpl(auRestClient);

	@Mock
	Logger logger;

	@Test
	public void validateSignatureKO() throws GeneralCryptoLibException {

		final ExtendedAuthenticationUpdate update = new ExtendedAuthenticationUpdate();
		update.setNewAuthID("newAuthID");
		update.setOldAuthID("oldAuthID");
		update.setNewSVK("newSVK");
		final AuthenticationToken authenticationToken = generateToken("1", "1");
		update.setAuthenticationTokenSignature(authenticationToken.getSignature());
		final KeyPair keyPairForSigning = getKeyPairForSigning();
		signToken(keyPairForSigning.getPrivate(), authenticationToken);
		final CertificateParameters certificateParameters = createCertificateParameters(COMMON_NAME);

		final KeyPair wrongKeypair = getKeyPairForSigning();
		final CryptoAPIX509Certificate certificate = certificateGenerator
				.generate(certificateParameters, wrongKeypair.getPublic(), wrongKeypair.getPrivate());
		final String signature = JsonSignatureService.sign(keyPairForSigning.getPrivate(), update);
		final ExtendedAuthenticationUpdateRequest request = new ExtendedAuthenticationUpdateRequest();
		final String pemEncoded = new String(certificate.getPemEncoded(), StandardCharsets.UTF_8);
		request.setCertificate(pemEncoded);
		request.setSignature(signature);

		assertThrows(ExtendedAuthValidationException.class, () -> extendendAuthValidationService.verifySignature(request, authenticationToken));
	}

	@Test
	public void validateSignature() throws GeneralCryptoLibException {

		final String signature = "eyJhbGciOiJQUzI1NiIsICJjdHkiOiJKV1QifQ.eyJvbGRBdXRoSUQiOiIxYTg3YjM0ODhhMDg3ZjE2NDZjMDg5MjM1ZmUxYzNlOSIsIm5ld0F1dGhJRCI6ImE0ZWEyNDg5ZWUzZTFjNzFhNmQ3MTJkNjBhYjNhMmYyIiwibmV3U1ZLIjoiNmllOHpZZE5PQ2ZZSnZpWDYwVFd4eTdLOXhXeFY5cDRkNWJVWHN4MU96OVpzdnlUOWFuTkN2S01qZ3kyZENGNyIsImF1dGhlbnRpY2F0aW9uVG9rZW5TaWduYXR1cmUiOiJISXVZT0U3dG9EbkNOSldGbkd6L1ozSlZtenlpbkxoVWF1V1lIZG4vOCtxYjBraFc5T3RWT1lDTGpndlRsVTNQNDgvUzZuUVRGVHVtZE1SRUpjOTFGMDdmWWtmUTEzZ1hHRDVCUlN1MFhTTDVSMis3Z1Z4d0lFVURJZCsvcXNZdWhYR3VDTTVmdUJqK3dMV2NUbGprd0NwemVXai8wU1Evcmt0NWpzSm9qRmJNdlRkaURRaUNlazhXMnhOTm93UXVKV0VyQTlod3FubDFiZUh5eE1oV1R1aXgxN01ES0dPUjlFVTAwM09pVThaSHB5ZXBad21WWnJCQVYzRXZwM2FLZzUwcEhOcTBIQk1GTlk2UVdndnduTzhxd1IrYUNMN0cvTDBIeVN0aUVzcy9hbEY0dDAzMkhMZjVybHA4RDBZNWVyV3duUlAwT0JlMzF2RzJtSlRhUnc9PSJ9.fJf2rEVtTeXnM2LRqEXX9yVk61NMtKXAuh2AR3NLn9mRBQ6uau6urIpUvW-4Fthz_8prYdhedmsGIyYUKdJtSsk_9gryPx03WSx_DZU9FY9fdEwlX-4HY0-D-bGzNvUcMi5BAD5-P5kCIxDyF4iOwmDK_JTJH46XMGap1QrCFE99vC07IRBR8KkXxMMb_a7n6qlzenlD85Kok-gZeXQ51RprwQypsOXfIezsS83wfhpzQdAGUOqGGO1CT-UQB7AoVSN9HcEqZ3C50pdNLlIu8qrOkLkKDIMDMV_cJEI6Wzor9FxYUGzJ2q1TaUcbiT0n5LMTL6vM9o3fpqj1F9dyyw";
		final String certificate = "-----BEGIN CERTIFICATE-----\n" + "MIIDoDCCAoigAwIBAgIUWVqY4Lr0xNeOemlhuYJLYGhFvr4wDQYJKoZIhvcNAQEL\n"
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
				+ "Qy1JV7GnqCucdByVImiT7wRC0KbYSYx76QVj5Su5SyYX+ouwmDEtlAT0aQlwnuvl\n" + "wls4Hn5S02qqgxhv7AcapTvuvPU=\n"
				+ "-----END CERTIFICATE-----";

		final JSONVerifier verifier = new JSONVerifier();
		final Certificate certificate1 = PemUtils.certificateFromPem(certificate);
		verifier.verifyFromMap(certificate1.getPublicKey(), signature, ExtendedAuthenticationUpdate.class);
	}

}
