/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.generators;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.bean.CredentialProperties;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGenerator;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.extendedkeystore.factory.CryptoExtendedKeyStoreWithPBKDF;
import ch.post.it.evoting.domain.election.helpers.ReplacementsHolder;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VotingCardCredentialInputDataPack;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig(VotingCardCredentialDataPackGeneratorTestSpringConfig.class)
class VotingCardCredentialDataPackGeneratorTest {

	private static final String CREDENTIAL_ID = "credentialId";
	private static final String ELECTION_EVENT_ID = "electionEventId";
	private static final String VOTING_CARD_SET_ID = "votingCardSetID";
	private static final char[] KEYSTORE_PASSWORD_TO_USE = "Random password with more than 16 characters".toCharArray();
	private static final String ENCODED_PEM = ("-----BEGIN CERTIFICATE-----\nMIIDgDCCAmigAwIBAgIUJrb" + "/jNipTnw8IIHjlqBbCh0CFoQwDQYJKoZIhvcNAQEL"
			+ "\nBQAwaTEjMCEGA1UEAwwaRWxlY3Rpb24gRXZlbnQgQ0EgMTAwMDAwMDAxFjAUBgNV"
			+ "\nBAsMDU9ubGluZSBWb3RpbmcxEjAQBgNVBAoMCVN3aXNzUG9zdDEJMAcGA1UEBwwA"
			+ "\nMQswCQYDVQQGEwJDSDAeFw0xNTA1MjcwNzAyNTdaFw0xNjA2MDExMDE1MzBaMGYx"
			+ "\nIDAeBgNVBAMMF0F1dGhvcml0aWVzIENBIDEwMDAwMDAwMRYwFAYDVQQLDA1Pbmxp"
			+ "\nbmUgVm90aW5nMRIwEAYDVQQKDAlTd2lzc1Bvc3QxCTAHBgNVBAcMADELMAkGA1UE"
			+ "\nBhMCQ0gwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCE9BuQ8QzGJom9\n4"
			+ "/O2Uvi9qpqtTMvNgFC8B7X96FNZ0wg7bdjTLQ0hzxTTpNJ4bRm9XRQS9P8Fh05B\naaqm5uEzh+Xb5Ark6c4EGSvQS/AsC5"
			+ "/tiDepjXZZe7PY8TAq+NjJIh8bdMc+R9QZ\n24BLL" + "+NHJKz42OPqMenlmiKO2V9UONx5mQnRuRzA6Joz14wSGvyMiFHjA2JsnTSx"
			+ "\nsPiEUQ9KFYhs8r1uPx7pVCFLudaXEwS0dmHMyrGBYKX6v+Doc0Ey2hWf6M2SdT63"
			+ "\nWMB0hiLG0MOADEffE1WNNvugfHtUs093dIiKl03aoL5PMLOFVXEB2KAi1g/MqnbT"
			+ "\nUXSZB1tpAgMBAAGjIzAhMA4GA1UdDwEB/wQEAwIBBjAPBgNVHRMBAf8EBTADAQH"
			+ "/\nMA0GCSqGSIb3DQEBCwUAA4IBAQBBUahcBSWEUvAqt9yVvLXQHAGsZEQm1K7ljhlq"
			+ "\nBGh590k99xIQyGdLKepMxR0ZzjrKPJOMTFpbEapOUBMsiy1lfHofLkmnxYhPmSfc\nnPQHo"
			+ "+jUsIgVpzrIk13G67iFUEAJtWYH5BhQGIUHf2fGveVPSdFtnVDd97en58xK\neACPMa0+0NxhsdV/NhFQTF7"
			+ "/X0tEQaggWUxJUXYDFoezx5fLVgp9Zt7v7f/P0Zhe\ns3/sY/u/nYf3K4CYyHCwUBUBDbQPhPV9mYt/ktoZ"
			+ "+1qiSxo5USnXJWnUSrhbOx+k\n+efy2K3m0WKcUz2ADszt08VkzdWEhPKBqfUqOB4Y4+3/q4Td\n-----END " + "CERTIFICATE-----\n");

	@Autowired
	private VotingCardCredentialDataPackGenerator votingCardCredentialDataPackGenerator;

	@Autowired
	private AsymmetricService asymmetricService;

	@Autowired
	private X509CertificateGenerator certificateGeneratorMock;

	@Autowired
	private KeyStoreService storesServiceMock;

	@Mock
	private CryptoAPIX509Certificate certificateMock;

	@Mock
	private CryptoExtendedKeyStoreWithPBKDF cryptoExtendedKeyStoreWithPBKDFMock;

	@Test
	void generateCredentialHappyPath() throws IOException, GeneralCryptoLibException {
		when(certificateMock.getCertificate()).thenReturn((X509Certificate) PemUtils.certificateFromPem(ENCODED_PEM));
		when(certificateMock.getSubjectDn()).thenReturn(new X509DistinguishedName.Builder("commonName", "CH").build());
		when(certificateMock.getSerialNumber()).thenReturn(new BigInteger("3232"));
		when(certificateGeneratorMock.generate(any(), any(), any())).thenReturn(certificateMock);
		when(cryptoExtendedKeyStoreWithPBKDFMock.toJSON(KEYSTORE_PASSWORD_TO_USE)).thenReturn("This is the keystore as a json");
		when(storesServiceMock.createKeyStore()).thenReturn(cryptoExtendedKeyStoreWithPBKDFMock);

		final Properties credentialsAuthCertificatePropertiesAsString = getCertificateParameters(
				"certificateProperties/credentialAuthX509Certificate.properties");
		final VotingCardCredentialInputDataPack votingCardCredentialInputDataPack = createInputDataPack();

		assertDoesNotThrow(() -> votingCardCredentialDataPackGenerator
				.generate(votingCardCredentialInputDataPack, votingCardCredentialInputDataPack.getReplacementsHolder(), KEYSTORE_PASSWORD_TO_USE,
						CREDENTIAL_ID, VOTING_CARD_SET_ID, credentialsAuthCertificatePropertiesAsString,
						certificateMock));
	}

	private Properties getCertificateParameters(String path) throws IOException {

		final Properties props = new Properties();

		try (final InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
			props.load(input);
		}

		return props;
	}

	private VotingCardCredentialInputDataPack createInputDataPack() {

		final CredentialProperties credentialAuthProperties = new CredentialProperties();
		credentialAuthProperties.setAlias(new LinkedHashMap<>(1));
		credentialAuthProperties.getAlias().put("privateKey", "auth_sign");
		credentialAuthProperties.setCredentialType(CertificateParameters.Type.SIGN);
		credentialAuthProperties.setName("credentialAuth");
		credentialAuthProperties.setParentName("credentialsca");
		credentialAuthProperties.setPropertiesFile("certificateProperties/credentialAuthX509Certificate.properties");

		final VotingCardCredentialInputDataPack inputDataPack = new VotingCardCredentialInputDataPack(credentialAuthProperties);
		inputDataPack.setParentKeyPair(asymmetricService.getKeyPairForSigning());
		inputDataPack.setEeid(ELECTION_EVENT_ID);
		inputDataPack.setReplacementsHolder(new ReplacementsHolder(ELECTION_EVENT_ID, CREDENTIAL_ID));

		final ZonedDateTime startValidityPeriod = ZonedDateTime.now(ZoneOffset.UTC);
		final ZonedDateTime endValidityPeriod = startValidityPeriod.plusYears(2);
		inputDataPack.setStartDate(startValidityPeriod);
		inputDataPack.setEndDate(endValidityPeriod);

		return inputDataPack;
	}
}
