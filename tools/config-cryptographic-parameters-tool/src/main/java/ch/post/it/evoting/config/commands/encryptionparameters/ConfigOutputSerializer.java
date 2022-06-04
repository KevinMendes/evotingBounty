/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.encryptionparameters;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.config.commands.SignedObject;
import ch.post.it.evoting.cryptolib.elgamal.bean.VerifiableElGamalEncryptionParameters;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Serializes pre-configuration data to file.
 */
@Service
public final class ConfigOutputSerializer {

	private static final DateTimeFormatter FORMATTING_PATTERN = DateTimeFormatter.ofPattern("yyyyMMddHHmmssnnnnnnnnn");
	private static final String ENCRYPTION_PARAMS_FILENAME = "encryptionParameters";
	private static final String JSON_EXTENSION = ".json";
	private static final String SIGN_EXTENSION = ".sign";
	private static final String OUTPUT_FOLDER = "output";
	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Serialize the received encryption parameters as a JWT.
	 *
	 * @param verifiableElGamalEncryptionParameters the encryption parameters to be serialized.
	 * @param privateKey                            the private key to sign the encryption parameters.
	 * @param optionalOutputPath                    an optional output path. Defaults to "output".
	 * @return the path to the file containing the serialized encryption parameters.
	 * @throws IOException if an error occurrs during serialization.
	 */
	public Path serialize(final VerifiableElGamalEncryptionParameters verifiableElGamalEncryptionParameters, final PrivateKey privateKey,
			final Path optionalOutputPath) throws IOException {

		checkNotNull(verifiableElGamalEncryptionParameters);

		final Path outputPath;
		if (optionalOutputPath == null) {
			outputPath = Paths.get(OUTPUT_FOLDER);
		} else {
			outputPath = optionalOutputPath;
		}

		final String timeStamp = LocalDateTime.now().format(FORMATTING_PATTERN);
		serializeEncryptionParameters(verifiableElGamalEncryptionParameters, outputPath, timeStamp);
		signJSONs(privateKey, outputPath, timeStamp, verifiableElGamalEncryptionParameters);

		return outputPath;
	}

	private void serializeEncryptionParameters(final VerifiableElGamalEncryptionParameters params, final Path outputPath, final String timeStamp)
			throws IOException {

		final Path encryptionParamsPath = outputPath.resolve(ENCRYPTION_PARAMS_FILENAME + timeStamp + JSON_EXTENSION);
		Files.createDirectories(outputPath);
		mapper.writerWithDefaultPrettyPrinter().writeValue(encryptionParamsPath.toFile(), params);
	}

	private void signJSONs(final PrivateKey privateKey, final Path outputPath, final String timeStamp,
			final VerifiableElGamalEncryptionParameters params) throws IOException {

		final Path signedJsonPath = outputPath.resolve(ENCRYPTION_PARAMS_FILENAME + timeStamp + JSON_EXTENSION + SIGN_EXTENSION).toAbsolutePath();

		final Map<String, Object> claimMap = new HashMap<>();
		claimMap.put("objectToSign", params);
		final String signedJsonData = Jwts.builder().setClaims(claimMap).signWith(SignatureAlgorithm.PS256, privateKey).compact();

		final SignedObject signedDataObject = new SignedObject();

		signedDataObject.setSignature(signedJsonData);
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File(signedJsonPath.toString()), signedDataObject);
	}
}
