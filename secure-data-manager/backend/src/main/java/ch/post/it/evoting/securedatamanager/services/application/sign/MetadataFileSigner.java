/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.sign;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Map;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * The Class MetadataFileSigner.
 */
public class MetadataFileSigner {

	/**
	 * The signer.
	 */
	final AsymmetricServiceAPI signer;

	/**
	 * Instantiates a new metadata file signer.
	 *
	 * @param signer the signer
	 */
	public MetadataFileSigner(final AsymmetricServiceAPI signer) {
		this.signer = signer;
	}

	/**
	 * Creates the signature.
	 *
	 * @param signingKey     the signing key
	 * @param originalStream the original stream
	 * @param signedFields   the signed fields
	 * @return the signature metadata
	 * @throws IOException               Signals that an I/O exception has occurred.
	 * @throws GeneralCryptoLibException if something goes wrong during the signing process.
	 */
	public SignatureMetadata createSignature(final PrivateKey signingKey, final InputStream originalStream, final Map<String, String> signedFields)
			throws IOException, GeneralCryptoLibException {

		final StringBuilder fieldStringBuilder = new StringBuilder();
		final SignatureMetadata.SignatureMetadataBuilder builder = SignatureMetadata.builder();
		signedFields.forEach((k, v) -> {
			builder.addSignedField(k, v);
			fieldStringBuilder.append(v);
		});

		final byte[] bytes = fieldStringBuilder.toString().getBytes(StandardCharsets.UTF_8);

		final String encodedSignature;
		try (final InputStream bs = new ByteArrayInputStream(bytes);
				// original + fields
				final InputStream seq = new SequenceInputStream(originalStream, bs)) {

			final byte[] signatureBytes = signer.sign(signingKey, seq);
			encodedSignature = Base64.getEncoder().encodeToString(signatureBytes);
		}
		return builder.withSignature(encodedSignature).build();
	}
}
