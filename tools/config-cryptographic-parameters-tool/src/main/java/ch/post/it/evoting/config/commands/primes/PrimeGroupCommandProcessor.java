/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.primes;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bouncycastle.cms.CMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.config.Parameters;
import ch.post.it.evoting.config.commands.CertificateUtils;
import ch.post.it.evoting.config.commands.ChainValidator;
import ch.post.it.evoting.config.commands.KeyStoreUtils;
import ch.post.it.evoting.config.commands.PasswordReaderUtils;
import ch.post.it.evoting.config.commands.SignedObject;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.elgamal.bean.VerifiableElGamalEncryptionParameters;
import ch.post.it.evoting.cryptolib.mathematical.groups.MathematicalGroup;

import io.jsonwebtoken.Jwts;

@Service
public class PrimeGroupCommandProcessor implements Consumer<Parameters> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrimeGroupCommandProcessor.class);

	private static final Pattern pattern = Pattern.compile("[0-9]{23}");

	private final PrimesParametersAdapter primesParametersAdapter;
	private final PrimeGroupMembersProvider primeGroupMembersProvider;
	private final PrimesSerializer primesSerializer;
	private final KeyStoreUtils keyStoreUtils;
	private final CertificateUtils certificateUtils;

	public PrimeGroupCommandProcessor(final PrimesParametersAdapter primesParametersAdapter,
			final PrimeGroupMembersProvider primeGroupMembersProvider, PrimesSerializer primesSerializer,
			final KeyStoreUtils keyStoreUtils,
			CertificateUtils certificateUtils) {
		this.primesParametersAdapter = primesParametersAdapter;
		this.primeGroupMembersProvider = primeGroupMembersProvider;
		this.primesSerializer = primesSerializer;
		this.keyStoreUtils = keyStoreUtils;
		this.certificateUtils = certificateUtils;
	}

	@Override
	public void accept(final Parameters parameters) {

		try {
			LOGGER.info("Starting the generation of prime group members");

			final PrimesParametersContainer holder = primesParametersAdapter.adapt(parameters);

			LOGGER.info("Parameters collected");

			char[] password = PasswordReaderUtils.readPasswordFromConsole();

			LOGGER.info("Password red");

			KeyStore store = keyStoreUtils.decodeKeyStore(holder.getP12Path(), password);
			PrivateKey privateKey = keyStoreUtils.loadPrivateKeyFromKeyStore(store, password);
			Certificate[] fullChain = loadCertificateChainFromKeyStore(store);
			Certificate signer = fullChain[0];
			Certificate[] innerChain = Arrays.copyOfRange(fullChain, 1, fullChain.length);

			LOGGER.info("KeyStore red");

			clean(password);

			LOGGER.info("Password deleted from memory");

			Certificate trustedCA = certificateUtils.readTrustedCA(Paths.get(holder.getTrustedCAPath()));


			ChainValidator.validateChain(trustedCA, innerChain, signer, X509CertificateType.SIGN);

			String timeStamp = getTimeStampFromFile(holder.getEncryptionParametersPath());

			LOGGER.info("Chain from keyStore and trusted CA validated");

			final SignedObject signedObject = readSignedObject(Paths.get(holder.getEncryptionParametersPath()));

			// Verify signature.
			final Map<String, Object> claimMapRecovered = Jwts.parser().setSigningKey(signer.getPublicKey())
					.parseClaimsJws(signedObject.getSignature()).getBody();
			final ObjectMapper mapper = new ObjectMapper();
			final Object recoveredSignedObject = claimMapRecovered.get("objectToSign");
			final VerifiableElGamalEncryptionParameters params = mapper.convertValue(recoveredSignedObject,
					VerifiableElGamalEncryptionParameters.class);

			LOGGER.info("Inputted parameters retrieved and verified");

			MathematicalGroup<?> group = params.getGroup();

			List<BigInteger> primesGroupMembers = primeGroupMembersProvider.generateVotingOptionRepresentations(group.getP(), group.getQ(),
					group.getG());

			LOGGER.info("Prime group members generated");

			primesSerializer.serializePrimes(primesGroupMembers, timeStamp, holder.getOutputPath(), privateKey, Arrays.asList(innerChain), signer);

			LOGGER.info("Output processed");

			LOGGER.info("The prime group members were generated. They can be found in: {}", holder.getOutputPath());
		} catch (IOException e) {
			LOGGER.error("Could not open encryptionParameters file.");
			throw new CreatePrimeGroupMembersException(e);
		} catch (CMSException e) {
			LOGGER.error("Could not generate primes file signature.");
			throw new CreatePrimeGroupMembersException(e);
		} catch (GeneralCryptoLibException e) {
			LOGGER.error("Error validating certificate chain.");
			throw new CreatePrimeGroupMembersException(e);
		}
	}

	private Certificate[] loadCertificateChainFromKeyStore(KeyStore keyStore) {
		try {
			return keyStore.getCertificateChain(keyStoreUtils.getAlias(keyStore));
		} catch (KeyStoreException e) {
			throw new IllegalArgumentException("Failed to decode private key entry.", e);
		}
	}

	private String getTimeStampFromFile(String encryptionParametersPath) {
		Matcher matcher = pattern.matcher(encryptionParametersPath);

		if (!matcher.find()) {
			throw new IllegalArgumentException("Encryption parameters file, does not contain a valid timestamp.");
		}

		return matcher.group();
	}

	private SignedObject readSignedObject(Path path) throws IOException {
		try (InputStream stream = Files.newInputStream(path)) {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(stream, SignedObject.class);
		}
	}

	private void clean(final char[] password) {
		Arrays.fill(password, '\u0000');
	}
}
