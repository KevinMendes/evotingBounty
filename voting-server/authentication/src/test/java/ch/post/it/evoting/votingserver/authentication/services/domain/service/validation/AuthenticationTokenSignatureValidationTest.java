/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.validation;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.CertificateException;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCerts;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCertsRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.authentication.services.domain.utils.BeanUtils;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.util.CryptoUtils;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationTokenSignatureValidationTest {

	public static final String COMMON_NAME = "commonName";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@InjectMocks
	AuthenticationTokenSignatureValidation validation = new AuthenticationTokenSignatureValidation();

	@Mock
	private AsymmetricServiceAPI asymmetricServiceMock;

	@Mock
	private AuthenticationCertsRepository authenticationCertsRepositoryMock;

	@Mock
	private AuthenticationCerts authenticationCertsMock;

	@Test
	public void givenAuthenticationTokenWhenValidSignatureThenValidationSuccess()
			throws ResourceNotFoundException, GeneralCryptoLibException, CertificateException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		final KeyPair KEY_PAIR_FOR_SIGNING = CryptoUtils.getKeyPairForSigning();
		final CryptoAPIX509Certificate certificate = CryptoUtils
				.createCryptoAPIx509Certificate(COMMON_NAME, CertificateParameters.Type.SIGN, KEY_PAIR_FOR_SIGNING);
		final String pem = new String(certificate.getPemEncoded(), StandardCharsets.UTF_8);

		final JsonObject json = Json.createObjectBuilder().add("authenticationTokenSignerCert", pem).build();

		when(authenticationCertsMock.getJson()).thenReturn(json.toString());
		when(asymmetricServiceMock.verifySignature(any(byte[].class), any(PublicKey.class), (byte[]) any())).thenReturn(true);

		final AuthenticationToken authenticationToken = BeanUtils.createAuthenticationToken();

		final ValidationResult execute = validation.execute(tenantId, electionEventId, votingCardId, authenticationToken);
		assertTrue(execute.isResult());

	}

	@Test
	public void givenAuthenticationTokenInvalidSignatureThenValidationFail()
			throws GeneralCryptoLibException, ResourceNotFoundException, CertificateException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		final KeyPair keyPairForSigning = CryptoUtils.getKeyPairForSigning();
		expectedException.expect(AuthTokenValidationException.class);
		final AuthenticationToken authenticationToken = BeanUtils.createAuthenticationToken();
		final CryptoAPIX509Certificate certificate = CryptoUtils
				.createCryptoAPIx509Certificate(COMMON_NAME, CertificateParameters.Type.SIGN, keyPairForSigning);
		final String pem = new String(certificate.getPemEncoded(), StandardCharsets.UTF_8);

		final JsonObject json = Json.createObjectBuilder().add("authenticationTokenSignerCert", pem).build();
		when(authenticationCertsMock.getJson()).thenReturn(json.toString());
		validation.execute(tenantId, electionEventId, votingCardId, authenticationToken);

	}

	@Before
	public void setUp() throws ResourceNotFoundException {

		// verify signature
		when(authenticationCertsMock.getJson()).thenReturn("{\"authenticationTokenSignerCert\":\"aafdasdfsfasdfasddfasdf}");

		when(authenticationCertsRepositoryMock.findByTenantIdElectionEventId(any(String.class), any(String.class)))
				.thenReturn(authenticationCertsMock);
	}

	@Test
	public void givenAuthenticationTokenCryptoLibExceptionWhenVerifying()
			throws GeneralCryptoLibException, ResourceNotFoundException, CertificateException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		final KeyPair keyPairForSigning = CryptoUtils.getKeyPairForSigning();
		expectedException.expect(AuthTokenValidationException.class);
		final AuthenticationToken authenticationToken = BeanUtils.createAuthenticationToken();
		final CryptoAPIX509Certificate certificate = CryptoUtils
				.createCryptoAPIx509Certificate(COMMON_NAME, CertificateParameters.Type.SIGN, keyPairForSigning);
		final String pem = new String(certificate.getPemEncoded(), StandardCharsets.UTF_8);

		final JsonObject json = Json.createObjectBuilder().add("authenticationTokenSignerCert", pem).build();
		when(authenticationCertsMock.getJson()).thenReturn(json.toString());
		validation.execute(tenantId, electionEventId, votingCardId, authenticationToken);

	}

}
