/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.sign;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;

@Service
public class FileSignerService {

	private final AsymmetricServiceAPI signer;
	private final HashService hashService;

	@Autowired
	public FileSignerService(final AsymmetricServiceAPI signer,
			@Qualifier("cryptoPrimitivesHashService")
			final HashService hashService) {
		this.signer = signer;
		this.hashService = hashService;
	}

	/**
	 * Creates a signature {@link FileSignature} from a source file {@code originalPath} and a PrivateKey {@code signingKey}
	 *
	 * @param signingKey   the {@link java.security.PrivateKey} signing key. Must be non-null.
	 * @param originalPath the path of the file to sign. Must be non-null.
	 * @param fieldsToSign the signed fields. Must be non-empty.
	 * @return the signature metadata.
	 * @throws IOException               if an I/O error occurs when opening an InputStream from the originalPath.
	 * @throws GeneralCryptoLibException if the signing process does not successfully complete.
	 */
	public FileSignature createSignature(final PrivateKey signingKey, final Path originalPath, final Map<String, String> fieldsToSign)
			throws IOException, GeneralCryptoLibException {

		checkNotNull(signingKey);
		checkNotNull(originalPath);
		checkArgument(!fieldsToSign.isEmpty());

		// Build a hash of the fields to sign
		final List<HashableString> hashableFields = fieldsToSign.values().stream().map(HashableString::from).collect(Collectors.toList());
		final byte[] fieldsBytes = hashService.recursiveHash(HashableList.from(hashableFields));

		final byte[] signature;
		try (final InputStream originalStream = Files.newInputStream(originalPath)) {
			final InputStream fieldsStream = new ByteArrayInputStream(fieldsBytes);
			final InputStream streamToSign = new SequenceInputStream(originalStream, fieldsStream);
			signature = signer.sign(signingKey, streamToSign);
		}

		return new FileSignature(fieldsToSign, signature);
	}

	/**
	 * Verifies the provided signature {@code signatureMetadata} build from a source file {@code sourcePath} and a PublicKey {@code signingKey}.
	 *
	 * @param publicKey     the {@link java.security.PublicKey} used to verify the signature.
	 * @param fileSignature the {@link FileSignature} to verify.
	 * @param sourcePath    the source file path that was signed.
	 * @return true, if signature is valid.
	 * @throws IOException               Occurs when cannot open an InputStream from the sourcePath.
	 * @throws GeneralCryptoLibException Occurs when the signature verification process goes wrong.
	 */
	public boolean verifySignature(final PublicKey publicKey, final FileSignature fileSignature, final Path sourcePath)
			throws IOException, GeneralCryptoLibException {

		checkNotNull(publicKey);
		checkNotNull(fileSignature);
		checkNotNull(sourcePath);

		// Build a hash of the fields to sign
		final List<HashableString> hashableFields = fileSignature.getSigned().values().stream().map(HashableString::from)
				.collect(Collectors.toList());
		final byte[] fieldsBytes = hashService.recursiveHash(HashableList.from(hashableFields));

		try (final InputStream sourceStream = Files.newInputStream(sourcePath)) {
			final InputStream fieldsStream = new ByteArrayInputStream(fieldsBytes);
			final InputStream streamToVerify = new SequenceInputStream(sourceStream, fieldsStream);

			return signer.verifySignature(fileSignature.getSignature(), publicKey, streamToVerify);
		}
	}

}
