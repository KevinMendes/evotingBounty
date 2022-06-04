/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

import ch.post.it.evoting.cryptolib.CryptolibService;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIKDFDeriver;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIPBKDFDeriver;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomInteger;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomString;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.DerivationPolicy;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.DerivationPolicyFromProperties;
import ch.post.it.evoting.cryptolib.primitives.derivation.factory.CryptoKeyDeriverFactory;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration.MessageDigestPolicy;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration.MessageDigestPolicyFromProperties;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.factory.MessageDigestFactory;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.SecureRandomPolicy;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.SecureRandomPolicyFromProperties;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.CryptoRandomInteger;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.CryptoRandomString;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.SecureRandomFactory;

/**
 * Class which implements {@link PrimitivesServiceAPI}. Instances of this class are immutable.
 */
public class PrimitivesService extends CryptolibService implements PrimitivesServiceAPI {
	/**
	 * Separator character that is not part of the base64 alphabet.
	 */
	protected static final byte[] HASH_OBJECT_SEPARATOR = { ':' };

	private final SecureRandom secureRandom;

	private final CryptoRandomInteger cryptoRandomInteger;
	private final CryptoRandomString stringRandom32;
	private final CryptoRandomString stringRandom64;
	private final MessageDigestFactory messageDigestFactory;
	private final CryptoKeyDeriverFactory cryptoKeyDeriverFactory;

	/**
	 * Initializes all properties to default values. These default values are obtained from the path indicated by {@link
	 * ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper#CRYPTOLIB_POLICY_PROPERTIES_FILE_PATH}.
	 */
	public PrimitivesService() {
		final MessageDigestPolicy messageDigestPolicy = new MessageDigestPolicyFromProperties();
		final SecureRandomPolicy secureRandomPolicy = new SecureRandomPolicyFromProperties();
		final DerivationPolicy derivationPolicy = new DerivationPolicyFromProperties();

		messageDigestFactory = new MessageDigestFactory(messageDigestPolicy);

		secureRandom = new SecureRandom();

		final SecureRandomFactory secureRandomFactory = new SecureRandomFactory(secureRandomPolicy);
		cryptoRandomInteger = secureRandomFactory.createIntegerRandom();
		stringRandom32 = secureRandomFactory.createStringRandom(SecureRandomConstants.ALPHABET_BASE32);
		stringRandom64 = secureRandomFactory.createStringRandom(SecureRandomConstants.ALPHABET_BASE64);

		cryptoKeyDeriverFactory = new CryptoKeyDeriverFactory(derivationPolicy);
	}

	/**
	 * Constructor which initializes its internal state using the specified properties.
	 *
	 * @param properties The properties to be used to configure the service.
	 */
	public PrimitivesService(final Properties properties) {
		final MessageDigestPolicy messageDigestPolicy = new MessageDigestPolicyFromProperties(properties);
		final SecureRandomPolicy secureRandomPolicy = new SecureRandomPolicyFromProperties(properties);
		final DerivationPolicy derivationPolicy = new DerivationPolicyFromProperties(properties);

		messageDigestFactory = new MessageDigestFactory(messageDigestPolicy);

		secureRandom = new SecureRandom();

		final SecureRandomFactory secureRandomFactory = new SecureRandomFactory(secureRandomPolicy);
		cryptoRandomInteger = secureRandomFactory.createIntegerRandom();
		stringRandom32 = secureRandomFactory.createStringRandom(SecureRandomConstants.ALPHABET_BASE32);
		stringRandom64 = secureRandomFactory.createStringRandom(SecureRandomConstants.ALPHABET_BASE64);

		cryptoKeyDeriverFactory = new CryptoKeyDeriverFactory(derivationPolicy);
	}

	@Override
	public CryptoAPIRandomInteger getCryptoRandomInteger() {
		return cryptoRandomInteger;
	}

	@Override
	public CryptoAPIRandomString get32CharAlphabetCryptoRandomString() {
		return stringRandom32;
	}

	@Override
	public CryptoAPIRandomString get64CharAlphabetCryptoRandomString() {
		return stringRandom64;
	}

	@Override
	public CryptoAPIKDFDeriver getKDFDeriver() {
		Objects.requireNonNull(cryptoKeyDeriverFactory, "The derivation policy is missing");
		return cryptoKeyDeriverFactory.createKDFDeriver();
	}

	@Override
	public CryptoAPIPBKDFDeriver getPBKDFDeriver() {
		Objects.requireNonNull(cryptoKeyDeriverFactory, "The derivation policy is missing");
		return cryptoKeyDeriverFactory.createPBKDFDeriver();
	}

	@Override
	public byte[] genRandomBytes(final int lengthInBytes) {

		checkArgument(lengthInBytes > 0, "Length in bytes must be greater than or equal to : 1; Found %s", lengthInBytes);
		checkArgument(lengthInBytes <= SecureRandomConstants.MAXIMUM_GENERATED_BYTE_ARRAY_LENGTH,
				"Length in bytes must be less than or equal to maximum allowed value for secure random byte arrays: %s; Found %s",
				SecureRandomConstants.MAXIMUM_GENERATED_BYTE_ARRAY_LENGTH, lengthInBytes);

		final byte[] bytes = new byte[lengthInBytes];
		secureRandom.nextBytes(bytes);
		return bytes;
	}

	@Override
	public byte[] getHash(final byte[] data) throws GeneralCryptoLibException {

		Validate.notNullOrEmpty(data, "Data");

		return messageDigestFactory.create().generate(data);
	}

	@Override
	public byte[] getHash(final InputStream in) throws GeneralCryptoLibException {

		Validate.notNull(in, "Data input stream");

		return messageDigestFactory.create().generate(in);
	}

	@Override
	public byte[] getHashOfObjects(final Stream<?> objectsToHash, final Charset charset) {
		final Base64.Encoder encoder = Base64.getEncoder();
		final MessageDigest md = messageDigestFactory.create().getRawMessageDigest();

		objectsToHash
				// Convert the object's toString representation to a Base64 byte
				// array
				.map(o -> encoder.encode(o.toString().getBytes(charset)))
				// Add a separator before each element
				.flatMap(i -> Stream.of(HASH_OBJECT_SEPARATOR, i))
				// Skip the first separator
				.skip(1)
				// Add the element to the digest
				.forEachOrdered(md::update);

		// Return the full digest.
		return md.digest();
	}

	@Override
	public MessageDigest getRawMessageDigest() {

		return messageDigestFactory.create().getRawMessageDigest();
	}

}
