/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.domain.validation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.cert.CertificateException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ExtendedAuthValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.validation.rules.CredentialIdValidation;

@RunWith(MockitoJUnitRunner.class)
public class CertificateValidationTest extends ValidationTest {

	public static final String TENANT_ID = "1";

	public static final String ELECTION_EVENT_ID = "1";

	public static final String OTHER = "lafjdlsjfd";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@InjectMocks
	CredentialIdValidation validation = new CredentialIdValidation();

	@Test
	public void testCertificateOK() throws IOException, GeneralCryptoLibException, ResourceNotFoundException, CertificateException {

		final AuthenticationToken token = generateToken(TENANT_ID, ELECTION_EVENT_ID);
		final AuthTokenCryptoInfo tokenKeys = getTokenKeys();
		signToken(tokenKeys.getPrivateKey(), token);
		final CertificateParameters certificateParameters = createCertificateParameters(token.getVoterInformation().getCredentialId());
		final KeyPair keyPairForSigning = getKeyPairForSigning();
		final CryptoAPIX509Certificate certificate = certificateGenerator
				.generate(certificateParameters, keyPairForSigning.getPublic(), keyPairForSigning.getPrivate());
		final String pemEncoded = new String(certificate.getPemEncoded(), StandardCharsets.UTF_8);
		validation.validateCertificate(pemEncoded, token);

	}

	@Test
	public void testInvalidCredentialInCertificate() throws IOException, GeneralCryptoLibException {
		expectedException.expect(ExtendedAuthValidationException.class);
		final AuthenticationToken token = generateToken(TENANT_ID, ELECTION_EVENT_ID);
		final AuthTokenCryptoInfo tokenKeys = getTokenKeys();
		signToken(tokenKeys.getPrivateKey(), token);
		final CertificateParameters certificateParameters = createCertificateParameters(token.getVoterInformation().getCredentialId());
		token.getVoterInformation().setCredentialId(OTHER);
		final KeyPair keyPairForSigning = getKeyPairForSigning();
		final CryptoAPIX509Certificate certificate = certificateGenerator
				.generate(certificateParameters, keyPairForSigning.getPublic(), keyPairForSigning.getPrivate());
		final String pemEncoded = new String(certificate.getPemEncoded(), StandardCharsets.UTF_8);
		validation.validateCertificate(pemEncoded, token);

	}

	@Test
	public void testThrowingCryptoLibException() throws IOException, GeneralCryptoLibException {
		expectedException.expect(ExtendedAuthValidationException.class);
		final AuthenticationToken token = generateToken(TENANT_ID, ELECTION_EVENT_ID);
		final String wronCertificate = "THIS_IS_NOT_A_CERTICATE_BUT_OTHER_THING";
		validation.validateCertificate(wronCertificate, token);

	}

}
