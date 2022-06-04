/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.domain.validation;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.time.ZonedDateTime;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGenerator;
import ch.post.it.evoting.cryptolib.certificates.factory.builders.CertificateDataBuilder;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesService;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.domain.election.model.Information.VoterInformation;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.filter.SignedRequestKeyManager;
import ch.post.it.evoting.votingserver.commons.util.DateUtils;

/**
 * General class for creating cryptographic data to be tested
 */
public class ValidationTest {

	private static final String TOKEN_ID = "8f24520bc9e346cd951289b9676567f6";
	private static final String GENERIC_ID = "1";
	private static final String COUNTRY = "CH";
	private static final String SWISSPOST = "SWISSPOST";

	protected static CertificatesServiceAPI certificatesService;

	private static AsymmetricServiceAPI asymmetricService;

	protected final CertificateDataBuilder certificateDataBuilder = new CertificateDataBuilder();
	protected X509CertificateGenerator certificateGenerator = new X509CertificateGenerator(certificatesService, certificateDataBuilder);

	@BeforeClass
	public static void init() {
		asymmetricService = new AsymmetricService();

		certificatesService = new CertificatesService();
	}

	protected AuthTokenCryptoInfo getTokenKeys() throws GeneralCryptoLibException {
		final KeyPair keyPairForSigning = getKeyPairForSigning();

		final String certificateContentStr = "-----BEGIN CERTIFICATE-----\n" + "MIIDjzCCAnegAwIBAgIVALxc4pAxqTzXp83vQTNQKwHaERTvMA0GCSqGSIb3DQEB\n"
				+ "CwUAMFwxFjAUBgNVBAMMDVRlbmFudCAxMDAgQ0ExFjAUBgNVBAsMDU9ubGluZSBW\n"
				+ "b3RpbmcxEjAQBgNVBAoMCVN3aXNzUG9zdDEJMAcGA1UEBwwAMQswCQYDVQQGEwJD\n"
				+ "SDAeFw0xNjA5MDcxMTE2MThaFw0xNjEwMTkyMjU5NTlaMIGDMT0wOwYDVQQDDDRB\n"
				+ "ZG1pbmlzdHJhdGlvbkJvYXJkIDNhM2M1MWNkZDE0NDRjOGJhYjA2NjQwZTM3ZTA0\n"
				+ "NzA4MRYwFAYDVQQLDA1PbmxpbmUgVm90aW5nMRIwEAYDVQQKDAlTd2lzc1Bvc3Qx\n"
				+ "CTAHBgNVBAcMADELMAkGA1UEBhMCQ0gwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAw\n"
				+ "ggEKAoIBAQCLjRvSUWwAkNAvyGwnksccYJ0XMSa/LmYbE2caVaUTJgkhfkt7uMi2\n"
				+ "e+LjCqEVRbfvcqcuH2SF9dsYrfgCdm/FHQadeciu66BV6vBntc3dCw1GJa4LJQcp\n"
				+ "tTRJBL1ca7FVHl7u3onfIez/o9Jy07P8P2iv+ol8Xvvx4PBa6BvvJkIlukQy+Ayt\n"
				+ "/zggF9QFSzJ+jywse5MLEYwh4oT53uETYHP3pVDGa5crxSOuCfZEk73tyJmgH0ML\n"
				+ "enrbu2oR4yNVfm6qIhijiJ9on05uLXSuYjLBAvhNwTrgYJBVtS8RKzOb5oMqNMKa\n"
				+ "mib7MVuY57bELJe9hsUp2cWNKrF5D3IxAgMBAAGjIDAeMA4GA1UdDwEB/wQEAwIG\n"
				+ "wDAMBgNVHRMBAf8EAjAAMA0GCSqGSIb3DQEBCwUAA4IBAQB9vJEPCixxoDPZThUo\n"
				+ "b6t41iILWBkdAExvQlVyZ/VfQ6/eXSmyviuZLcO13SDa3zp8g1DEe5H9XIfVgyqF\n"
				+ "C7aZ4pV88aI8GNqxnkdO4GmXOZM5mQZ0c9eGPKr0RJ6jUIL6iJmBSSj/gt4I9XoD\n"
				+ "lKBL3xivNMJxw5BVhyqCB5vM1xO6j5fzyfwc7EiJZF4axMly8yg/Ip7tBnZvtZM3\n"
				+ "cFWi0kCM755AFfAMVWPqA7pak4HcTT6c2En4ok+1RB4mVwjirfJJOSPxUsPwFvw4\n"
				+ "vC5KMoy46gcZO/bx6e4wjSNWz9NqwbWxLYfESo55ZNREtPb9rTnlb4JN7GMmvd9y\n" + "QvCO\n" + "-----END CERTIFICATE-----\n";

		final Certificate certificateToValidate = PemUtils.certificateFromPem(certificateContentStr);

		final CryptoAPIX509Certificate cryptoAPIX509Certificate = SignedRequestKeyManager.getCryptoX509Certificate(certificateToValidate);

		final AuthTokenCryptoInfo info = new AuthTokenCryptoInfo();
		info.setCertificate(cryptoAPIX509Certificate);
		info.setPrivateKey(keyPairForSigning.getPrivate());
		return info;
	}

	/**
	 * Generates an AuthToken
	 */
	protected AuthenticationToken generateToken(final String tenantId, final String electionEventId) {
		final AuthenticationToken token = new AuthenticationToken();
		final VoterInformation voterInformation = new VoterInformation();
		voterInformation.setBallotBoxId(GENERIC_ID);
		voterInformation.setBallotId(GENERIC_ID);
		voterInformation.setCredentialId(GENERIC_ID);
		voterInformation.setElectionEventId(electionEventId);
		voterInformation.setVerificationCardId(GENERIC_ID);
		voterInformation.setVerificationCardSetId(GENERIC_ID);
		voterInformation.setVotingCardId(GENERIC_ID);
		voterInformation.setTenantId(tenantId);
		voterInformation.setVotingCardSetId(GENERIC_ID);
		token.setVoterInformation(voterInformation);
		token.setId(TOKEN_ID);
		return token;
	}

	/**
	 * Signs an authenticationToken
	 *
	 * @param privateKey
	 * @param token
	 * @throws GeneralCryptoLibException
	 */
	protected void signToken(final PrivateKey privateKey, final AuthenticationToken token) throws GeneralCryptoLibException {

		token.setTimestamp(DateUtils.getTimestamp());

		final byte[] bytesToSign = StringUtils.join(token.getFieldsAsStringArray()).getBytes(StandardCharsets.UTF_8);
		final byte[] signature = asymmetricService.sign(privateKey, bytesToSign);

		token.setSignature(Base64.getEncoder().encodeToString(signature));
	}

	protected KeyPair getKeyPairForSigning() {
		return asymmetricService.getKeyPairForSigning();
	}

	protected CertificateParameters createCertificateParameters(final String commonName) {
		final ZonedDateTime now = ZonedDateTime.now();
		final CertificateParameters params = new CertificateParameters();
		params.setType(CertificateParameters.Type.SIGN);
		params.setUserIssuerCn(commonName);
		params.setUserIssuerCountry(COUNTRY);
		params.setUserIssuerOrg(SWISSPOST);
		params.setUserIssuerOrgUnit(SWISSPOST);
		params.setUserNotAfter(now.plusYears(1));
		params.setUserNotBefore(now);
		params.setUserSubjectCn(commonName);
		params.setUserSubjectCountry(COUNTRY);
		params.setUserSubjectOrg(SWISSPOST);
		params.setUserSubjectOrgUnit(SWISSPOST);
		return params;
	}

}
