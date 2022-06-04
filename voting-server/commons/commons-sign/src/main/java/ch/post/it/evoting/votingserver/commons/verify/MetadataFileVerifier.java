/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.verify;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.votingserver.commons.sign.SignatureMetadata;
import ch.post.it.evoting.votingserver.commons.sign.beans.SignatureFieldsConstants;

/**
 * The Class MetadataFileVerifier.
 */
public class MetadataFileVerifier {

	/**
	 * The verifier.
	 */
	private final AsymmetricServiceAPI verifier;

	/**
	 * Instantiates a new metadata file verifier.
	 *
	 * @param verifier the verification service
	 */
	public MetadataFileVerifier(final AsymmetricServiceAPI verifier) {
		this.verifier = verifier;
	}

	/**
	 * Verify signature.
	 *
	 * @param publicKey      the public key
	 * @param metadataStream the metadata stream
	 * @param sourceStream   the source stream
	 * @return true, if successful
	 * @throws GeneralCryptoLibException if something goes wrong during the verification.
	 */
	public boolean verifySignature(final PublicKey publicKey, final InputStream metadataStream, final InputStream sourceStream)
			throws GeneralCryptoLibException {

		try (JsonReader jsonReader = Json.createReader(metadataStream)) {
			final JsonObject metadataSignatureJson = jsonReader.readObject();
			final SignatureMetadata signatureMetadata = SignatureMetadata.fromJsonObject(metadataSignatureJson);

			StringBuilder sb = new StringBuilder();
			signatureMetadata.getSignedFields().forEach((k, v) -> sb.append(v));
			String fieldsString = sb.toString();

			final byte[] bytes = fieldsString.getBytes(StandardCharsets.UTF_8);
			InputStream bs = new ByteArrayInputStream(bytes);
			InputStream seq = new SequenceInputStream(sourceStream, bs);

			byte[] signatureBytes = Base64.getDecoder().decode(metadataSignatureJson.getString(SignatureFieldsConstants.SIG_FIELD_SIGNATURE));
			return verifier.verifySignature(signatureBytes, publicKey, seq);
		}
	}
}
