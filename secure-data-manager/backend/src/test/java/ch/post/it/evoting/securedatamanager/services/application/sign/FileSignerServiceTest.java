/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.sign;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.securedatamanager.services.application.service.AliasSignatureConstants;

@DisplayName("Use MetadataFileSigner to")
class FileSignerServiceTest {

	private final static String SIGNATURE_METADATA_JSON = "{\"version\":\"1.0\",\"signed\":{\"timestamp\":\"2019-02-12T19:33:21Z\",\"component\":\"Secure Data Manager\"},\"signature\":\"WtZQh2UOR9Vr7LV+/0rYbAGjDNj8JgrBSGuNzvptTbVXTI2dPmDZFcRRZLIXrMsV8hLae8PsEAAILi/pXX+V8GFLXXlHkzov0oRGE7ZscRNK5wW6RancOYmi8I0pDjsIdc1idxEF0nNO0B8iE7uSHbYDcvDLYwcQEWfXQ3r/Wa4Ir4wNxSzKs+kSV4RBwk6UZORGBrfB+KxjJlHEmuU71+1AFTaDMnbxRJ2/djkgaq746EHKrZBXch6RDpvQqHgaM37cmI7wSdtD8bLeCWq0oDu9PA8rCl5D/2Z/Btp+Tnp8J+tZn11aL3NPMc1KMFq3DI7Tjt+Oo0XoQZy2nBr0mQ==\"}";
	private static final Map<String, String> SIGNED_FIELDS = new LinkedHashMap<>();
	private final static byte[] SOURCE_BYTES = "File content".getBytes(StandardCharsets.UTF_8);
	private final static String PRIVATE_KEY_MODULUS = "31325383663114987886555945669902095165153296018940884809517697064833223729752042394665593938916577842409045949249466700817639463506391262364242478529492802769737889735349761283637094388810983857596127010720238439686352809011444776902785504926147429834835070485501860552571618741343761346920542219674399423638261239507126346842229625997022472855667163465446949883802541336828469328006991078666201613000319280507488950233194814155701901921360170971053521490432289326121469786104128803498811981823736894255895142551855870632669610547496980900159178253589938574364930957601862070218554114668331649240228369625200713538163";
	private final static String PRIVATE_KEY_EXPONENT = "26966204893754036217645403159419358575040870065773347853862091876446831303865312354545505794465059006899784020001024808276679823803959226830698199661235568516415193328947426375008834004204484127958263233956447381658986014404056944604184656475250004562184570114295107600201916696375955321562113468840053592373465551231206509951858845076128995511367362111338495011731930904383347803634652926203896491095377900992310750389480004323623950943895924853024815015805332938827701716688430320022897547584080401888605521172005990441602738292866726450705187854196771964489558822838203127525847467304231811240284167883370001298993";
	private final static String PUBLIC_KEY_MODULUS = "31325383663114987886555945669902095165153296018940884809517697064833223729752042394665593938916577842409045949249466700817639463506391262364242478529492802769737889735349761283637094388810983857596127010720238439686352809011444776902785504926147429834835070485501860552571618741343761346920542219674399423638261239507126346842229625997022472855667163465446949883802541336828469328006991078666201613000319280507488950233194814155701901921360170971053521490432289326121469786104128803498811981823736894255895142551855870632669610547496980900159178253589938574364930957601862070218554114668331649240228369625200713538163";
	private final static String PUBLIC_KEY_EXPONENT = "65537";

	private final AsymmetricService asymmetricService = new AsymmetricService();
	private final HashService hashService = HashService.getInstance();

	private PrivateKey privateKey;
	private PublicKey publicKey;
	private FileSignerService fileSignerService;

	@BeforeEach
	void setUp() throws NoSuchAlgorithmException, InvalidKeySpecException {
		fileSignerService = new FileSignerService(asymmetricService, hashService);

		SIGNED_FIELDS.put(AliasSignatureConstants.TIMESTAMP, DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(1550000001)));
		SIGNED_FIELDS.put(AliasSignatureConstants.COMPONENT, "Secure Data Manager");

		final KeyFactory fact = KeyFactory.getInstance("RSA");
		final RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(new BigInteger(PRIVATE_KEY_MODULUS), new BigInteger(PRIVATE_KEY_EXPONENT));
		privateKey = fact.generatePrivate(privateKeySpec);
		final RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(PUBLIC_KEY_MODULUS), new BigInteger(PUBLIC_KEY_EXPONENT));
		publicKey = fact.generatePublic(publicKeySpec);
	}

	@Test
	@DisplayName("create a file signature")
	void createSignatureTest(
			@TempDir
			final Path tempDir) throws IOException {

		final Path sourceFile = Files.write(tempDir.resolve("sourceFile"), SOURCE_BYTES);

		assertDoesNotThrow(() -> fileSignerService.createSignature(privateKey, sourceFile, SIGNED_FIELDS));
	}

	@Test
	@DisplayName("create a file signature with null or invalid parameters")
	void createSignatureWithNullParametersTest(
			@TempDir
			final Path tempDir) throws IOException {

		final Path sourceFile = Files.write(tempDir.resolve("sourceFile"), SOURCE_BYTES);
		final Map<String, String> emptyMap = new LinkedHashMap<>();

		assertAll(() -> assertThrows(NullPointerException.class, () -> fileSignerService.createSignature(null, sourceFile, SIGNED_FIELDS)),
				() -> assertThrows(NullPointerException.class, () -> fileSignerService.createSignature(privateKey, null, SIGNED_FIELDS)),
				() -> assertThrows(IllegalArgumentException.class, () -> fileSignerService.createSignature(privateKey, sourceFile, emptyMap)));
	}

	@Test
	@DisplayName("verify a file signature")
	void verifySignatureTest(
			@TempDir
			final Path tempDir) throws IOException, GeneralCryptoLibException {

		final Path sourceFile = Files.write(tempDir.resolve("sourceFile"), SOURCE_BYTES);
		final FileSignature fileSignature = new ObjectMapper().readValue(SIGNATURE_METADATA_JSON, FileSignature.class);

		assertTrue(fileSignerService.verifySignature(publicKey, fileSignature, sourceFile), "The signature is invalid");
	}

	@Test
	@DisplayName("verify a file signature with null or invalid parameters")
	void verifySignatureWithNullParametersTest(
			@TempDir
			final Path tempDir) throws IOException {

		final Path sourceFile = Files.write(tempDir.resolve("sourceFile"), SOURCE_BYTES);
		final FileSignature fileSignature = new ObjectMapper().readValue(SIGNATURE_METADATA_JSON, FileSignature.class);

		assertAll(() -> assertThrows(NullPointerException.class, () -> fileSignerService.verifySignature(null, fileSignature, sourceFile)),
				() -> assertThrows(NullPointerException.class, () -> fileSignerService.verifySignature(publicKey, null, sourceFile)),
				() -> assertThrows(NullPointerException.class, () -> fileSignerService.verifySignature(publicKey, fileSignature, null)));
	}

}
