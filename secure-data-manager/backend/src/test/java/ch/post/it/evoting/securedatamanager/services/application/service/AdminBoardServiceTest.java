/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PrefixPathResolver;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SharesException;
import ch.post.it.evoting.securedatamanager.config.shares.shares.handler.CreateSharesHandler;
import ch.post.it.evoting.securedatamanager.config.shares.shares.handler.StatelessReadSharesHandler;
import ch.post.it.evoting.securedatamanager.config.shares.shares.keys.rsa.RSAKeyPairGenerator;
import ch.post.it.evoting.securedatamanager.config.shares.shares.service.SmartCardService;
import ch.post.it.evoting.securedatamanager.services.application.config.SmartCardConfig;
import ch.post.it.evoting.securedatamanager.services.domain.model.administrationauthority.ActivateOutputData;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.SmartCardStatus;
import ch.post.it.evoting.securedatamanager.services.infrastructure.administrationauthority.AdministrationAuthorityRepository;

@ExtendWith(MockitoExtension.class)
class AdminBoardServiceTest {

	@InjectMocks
	private static final AdminBoardService adminBoardService = new AdminBoardService();

	private static final String ADMIN_BOARD_ID = "db033b3f729c45719db8aba15d24043c";
	private static final String PIN = "222222";
	private static final String PUK = PIN;
	private static final char[] PWD = "2222222222222222".toCharArray();
	private static final String TEST_MEMBER_0 = "testMember0";
	private static final String TEST_MEMBER_1 = "testMember1";

	private static final String ADMINISTRATION_AUTHORITY_JSON =
			"{\n" + "            \"id\": \"04d7e562547646c2aec2ef12628dc126\",\n" + "            \"defaultTitle\": \"Adminboard\",\n"
					+ "            \"defaultDescription\": \"AsampleAB\",\n" + "            \"alias\": \"AB1\",\n"
					+ "            \"minimumThreshold\": \"1\",\n" + "            \"status\": \"LOCKED\",\n"
					+ "            \"administrationBoard\": [\n" + "                \"" + TEST_MEMBER_0 + "\",\n" + "                \""
					+ TEST_MEMBER_1 + "\"\n" + "            ]\n" + "        }";

	private static final String PUBLIC_KEY_FOR_ENCRYPTION_PEM = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA53It4Iqf"
			+ "/5vd3EDjfFa3yIA2pxvBYE6sSPhQUHoxNXZFmhhj9RCbknUekEE/WdgsC/+5zti1r7pubLmuwtIbkHyHPIVg5e/Xu"
			+ "/hoRtmbTZHWAELDjvRqsD3prNba+t+W0piYA99Wn4Fb1M/Uo6zyqTBgK3Rejtp2bE1w2s/Lzuk"
			+ "/GzAKIXzmZk589YMl0hm8JxN2JprPemK0LVXogijR4AmCgzT9fPpZx1QNIzmFisQzYc3EpnlnQ7mRxMjr60laUM5s"
			+ "+G00lCfnc2a8ZKOYuJPJjO/8hnNfaEDSNbOto3K92RtE2O+xjhzo1oELTovnPjJKinbZA2deojtSjiaJxQIDAQAB" + "-----END PUBLIC KEY-----";

	private static final String PRIVATE_KEY_FOR_ENCRYPTION_PEM =
			"-----BEGIN RSA PRIVATE " + "KEY-----MIIEpAIBAAKCAQEA53It4Iqf/5vd3EDjfFa3yIA2pxvBYE6sSPhQUHoxNXZFmhhj9RCbknUekEE/WdgsC"
					+ "/+5zti1r7pubLmuwtIbkHyHPIVg5e/Xu/hoRtmbTZHWAELDjvRqsD3prNba+t+W0piYA99Wn4Fb1M" + "/Uo6zyqTBgK3Rejtp2bE1w2s/Lzuk"
					+ "/GzAKIXzmZk589YMl0hm8JxN2JprPemK0LVXogijR4AmCgzT9fPpZx1QNIzmFisQzYc3EpnlnQ7mRxMjr60laUM5s"
					+ "+G00lCfnc2a8ZKOYuJPJjO/8hnNfaEDSNbOto3K92RtE2O" + "+xjhzo1oELTovnPjJKinbZA2deojtSjiaJxQIDAQABAoIBAQCcmi1gmWvZUGW1"
					+ "+lHyd9qy184jFCysNY9tcFcnnQZe3kAKHCbGUw5w8r5TbVKoQBTNqaLXytpkpQjCmIEfYXs1MI1w7e66pqaakWI9TlA"
					+ "/FEZwtrwLpmXqCnpqcJaK2W774DQ7qoq6MpUoUdfXR9aJlCn+PSceEcO/VEbgR2nn"
					+ "/bEpRycgX2ueJ4HgLt25Vlkirpf2O5IwUkTdWcSTRhOy40sPubOnLupUWJGyDOaa8Xd"
					+ "+bn0KS1jH18PMo3C0qvAohA0BtcwpLX5ac/oFCfmk8/IwPXwYhePwxNLLfBgG0lfLQl629gE"
					+ "+Avkq57G8vRBYcOD6kwdonDIRxFs+PmQBAoGBAPnWWWU2V+qeYLMmBkJLV9GwRNSNdUvBn4jedApV2bBNKw5PY31B6kvC"
					+ "/EoB10FYIYQdat9dSOWncRbzX1jAXvcUqNn0RbLy/B3D4"
					+ "/4vV39UeVWtK8OgLkxbBPSVFGwK3Nivuy0rcG2p0hT4yaxUGj1H2Y1hyrI9fAzPPAkL8ILhAoGBAO0nsb2Vb0VpsddAfyn01mhPgXzyNz81NBsxJb9ubgjQNSdhX/XPn36kTwtwpPPuJMWe9yxzlaHf/HxFP0kKhPBaviK1cKDm/gWWR9UEdRKEB9hkr+BIfhYbi+3KSWzmcE8fV0KHdcvV28JINmW95zhRI1dw4bbBR6xD1bE5RsdlAoGAaD9KmfLtCFcBnn8VSYBKqpJUhiRody3ZtbCs1ssvxGLOvm/d4ZwpeWdpAjB2cyulAI2N1JoGGt2dUKhIdq3+cjbKpfdJRfwhuwHMFnoGlnjXECrsAfrKls276ZpzJQn7UOcywQxJI1ki8eFFtYR6VmuumVHe1DTXmDi4okW7G8ECgYEAiQ1xiHB9t42XeyAI3URjTDD2UjDggKTMkhJbEEBPUsSQk0uQ20u7jsKB88iLa3Tqx1JQ4d2CUeRR07dpFVsA7K5kR0a36iTUFIJ+zLogtiybJBE8Gs+KHliZCzjmKgsaSH+CPC5wgvX6ZFK7LR0MLN2nIPdZWfZk50bkjeDd6IkCgYA7yqQhMsen+PWjf2MPh4tTml3zE1MHEPw0JeLnWaheviGFXtaX6U1eLjS/2EIPQShR+YgMeuk6+C31YiUuCKfvVzMtc1L0DQCyqIPE3B1QLnijH8nZvDnJXBZWyVZE57WRnSaa7JshRMC8llkL0Z+jZlDc7C+qgjYfgiDUIpd/eQ==-----END RSA PRIVATE KEY-----";

	@Mock
	private AdministrationAuthorityRepository administrationAuthorityRepositoryMock;

	@Mock
	private CreateSharesHandler createSharesHandler;

	@Mock
	private StatelessReadSharesHandler statelessReadSharesHandlerMock;

	@Mock
	private SmartCardService smartCardServiceMock;

	@BeforeAll
	static void setUp() throws GeneralCryptoLibException {

		ReflectionTestUtils.setField(adminBoardService, "pathResolver", new PrefixPathResolver("target/"));
		ReflectionTestUtils.setField(adminBoardService, "rsaKeyPairGenerator", new RSAKeyPairGenerator(new AsymmetricService()));
		ReflectionTestUtils.setField(adminBoardService, "smartCardConfig", SmartCardConfig.SMART_CARD);
		ReflectionTestUtils.setField(adminBoardService, "hashService", new HashService());

		adminBoardService.init();
	}

	@Test
	void constituteTest() throws SharesException, IOException, URISyntaxException {

		String hashMember = getHashValueForMember(TEST_MEMBER_0);
		final String label0 = hashMember.substring(0, Math.min(hashMember.length(), Constants.SMART_CARD_LABEL_MAX_LENGTH));

		hashMember = getHashValueForMember(TEST_MEMBER_1);
		final String label1 = hashMember.substring(0, Math.min(hashMember.length(), Constants.SMART_CARD_LABEL_MAX_LENGTH));

		when(administrationAuthorityRepositoryMock.find(anyString())).thenReturn(ADMINISTRATION_AUTHORITY_JSON);
		when(statelessReadSharesHandlerMock.getSmartcardLabel()).thenReturn(label0, label1);

		ReflectionTestUtils.setField(adminBoardService, "puk", PUK);

		final InputStream input = Files.newInputStream(Paths.get(getClass().getResource("/tenant.sks").toURI()).toAbsolutePath());

		assertDoesNotThrow(() -> adminBoardService.constitute(ADMIN_BOARD_ID, input, PWD));

		final Map<String, CreateSharesHandler> createSharesHandlerRSAMap = new HashMap<>();
		createSharesHandlerRSAMap.put(ADMIN_BOARD_ID, createSharesHandler);

		doNothing().when(createSharesHandler).writeShareAndSelfSign(anyInt(), anyString(), anyString(), anyString(), any());
		ReflectionTestUtils.setField(adminBoardService, "createSharesHandlerRSAMap", createSharesHandlerRSAMap);

		assertAll(() -> assertDoesNotThrow(() -> adminBoardService.writeShare(ADMIN_BOARD_ID, 0, PIN)),
				() -> assertDoesNotThrow(() -> adminBoardService.writeShare(ADMIN_BOARD_ID, 1, PIN)));
	}

	@Test
	void getSmartCardReaderStatusTest() {

		when(smartCardServiceMock.isSmartcardOk()).thenReturn(true);

		assertAll(() -> assertEquals(SmartCardStatus.INSERTED, adminBoardService.getSmartCardReaderStatus()),
				() -> verify(smartCardServiceMock).isSmartcardOk());
	}

	@Test
	void writeShareTest() throws SharesException {

		final String hashMember = getHashValueForMember(TEST_MEMBER_0);
		final String label = hashMember.substring(0, Math.min(hashMember.length(), Constants.SMART_CARD_LABEL_MAX_LENGTH));

		final Map<String, CreateSharesHandler> createSharesHandlerRSAMap = new HashMap<>();
		createSharesHandlerRSAMap.put(ADMIN_BOARD_ID, createSharesHandler);

		when(statelessReadSharesHandlerMock.getSmartcardLabel()).thenReturn(label);
		when(administrationAuthorityRepositoryMock.find(anyString())).thenReturn(ADMINISTRATION_AUTHORITY_JSON);

		ReflectionTestUtils.setField(adminBoardService, "createSharesHandlerRSAMap", createSharesHandlerRSAMap);
		ReflectionTestUtils.setField(adminBoardService, "puk", PUK);

		assertDoesNotThrow(() -> adminBoardService.writeShare(ADMIN_BOARD_ID, 0, PIN));
	}

	@Test
	void activateAdminBoard() throws GeneralCryptoLibException {

		ReflectionTestUtils
				.setField(adminBoardService, "pathResolver", new PrefixPathResolver(Paths.get("src/test/resources/").toAbsolutePath().toString()));

		final ActivateOutputData output = adminBoardService.activate("dc742be1d49b42ee83cfe1652a8170ac");

		assertAll(() -> assertFalse(output.getIssuerPublicKeyPEM().isEmpty()),
				() -> assertNotNull(PemUtils.publicKeyFromPem(output.getIssuerPublicKeyPEM())));
	}

	@Test
	void activateAdminBoardShouldThrowExceptionWhenCertificateDoesNotCorrespondToTheID() {

		ReflectionTestUtils
				.setField(adminBoardService, "pathResolver", new PrefixPathResolver(Paths.get("src/test/resources/").toAbsolutePath().toString()));

		assertThrows(RuntimeException.class, () -> adminBoardService.activate(ADMIN_BOARD_ID));
	}

	@Test
	void readShareTest() throws SharesException {

		final String hashMember = getHashValueForMember(TEST_MEMBER_0);
		final String truncatedHashMember = hashMember.substring(0, Math.min(hashMember.length(), Constants.SMART_CARD_LABEL_MAX_LENGTH));

		when(statelessReadSharesHandlerMock.getSmartcardLabel()).thenReturn(truncatedHashMember);
		when(statelessReadSharesHandlerMock.readShareAndStringify(anyString(), any())).thenReturn("");
		when(administrationAuthorityRepositoryMock.find(anyString())).thenReturn(ADMINISTRATION_AUTHORITY_JSON);

		assertAll(() -> assertDoesNotThrow(() -> adminBoardService.readShare(ADMIN_BOARD_ID, 0, PIN, PUBLIC_KEY_FOR_ENCRYPTION_PEM)),
				() -> verify(statelessReadSharesHandlerMock).readShareAndStringify(PIN, PemUtils.publicKeyFromPem(PUBLIC_KEY_FOR_ENCRYPTION_PEM)));
	}

	@Test
	void readShareShouldThrowExceptionWithSmartCardFromAnotherMember() throws SharesException {

		when(administrationAuthorityRepositoryMock.find(anyString())).thenReturn(ADMINISTRATION_AUTHORITY_JSON);
		when(statelessReadSharesHandlerMock.getSmartcardLabel()).thenReturn("anotherMember");

		assertAll(
				() -> assertThrows(RuntimeException.class, () -> adminBoardService.readShare(ADMIN_BOARD_ID, 0, PIN, PUBLIC_KEY_FOR_ENCRYPTION_PEM)),
				() -> verify(statelessReadSharesHandlerMock, times(0))
						.readShareAndStringify(PIN, PemUtils.publicKeyFromPem(PUBLIC_KEY_FOR_ENCRYPTION_PEM)));
	}

	@Test
	void reconstructPrivateKey() throws GeneralCryptoLibException, SharesException {

		when(statelessReadSharesHandlerMock.getPrivateKeyWithSerializedShares(any(), any()))
				.thenReturn(PemUtils.privateKeyFromPem(PRIVATE_KEY_FOR_ENCRYPTION_PEM));

		final String privateKeyPEM = adminBoardService.reconstruct(ADMIN_BOARD_ID, new ArrayList<>(), PUBLIC_KEY_FOR_ENCRYPTION_PEM);

		assertEquals(PRIVATE_KEY_FOR_ENCRYPTION_PEM, privateKeyPEM.replace("\r", "").replace("\n", ""));
	}

	private String getHashValueForMember(final String member) {

		try {
			final MessageDigest mdEnc = MessageDigest.getInstance(Constants.MESSAGE_DIGEST_ALGORITHM);

			final byte[] memberByteArray = member.getBytes(StandardCharsets.UTF_8);
			mdEnc.update(memberByteArray, 0, memberByteArray.length);

			return Base64.getEncoder().encodeToString(mdEnc.digest());
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException("No such algorithm exception when getting hash for member: " + member, e);
		}
	}

}
