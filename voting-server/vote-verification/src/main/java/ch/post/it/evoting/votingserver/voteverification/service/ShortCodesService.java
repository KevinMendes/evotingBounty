/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonString;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIKDFDeriver;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.symmetric.SymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SymmetricKeyPolicyFromProperties;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.CodesMapping;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.CodesMappingRepository;

/**
 * Retrieve short Choice Return Codes based on the long Choice Return Codes or retrieve the short Vote Cast Return Code based on the long Vote Cast
 * Return Code
 */
@Stateless
public class ShortCodesService {

	// The repository where the codes mapping table is stored
	@Inject
	private CodesMappingRepository codesMappingRepository;

	// The service that will be used to decrypt the short codes
	@Inject
	private SymmetricServiceAPI symmetricService;

	// The service that provides the hash and the key derivation function (KDF)
	@Inject
	private PrimitivesServiceAPI primitivesService;

	/**
	 * Retrieves the short Choice Return Codes / short Vote Cast Code based on the long Choice Return Codes / long Vote Cast Return Code and the Codes
	 * Secret Key.
	 *
	 * @param tenantId           tenant identifier.
	 * @param eeid               election event identifier.
	 * @param verificationCardId verification card id
	 * @param longCodes          long Choice Return Codes or long Vote Cast Return Code
	 */
	public List<String> retrieveShortCodes(String tenantId, String eeid, String verificationCardId, List<byte[]> longCodes)
			throws ResourceNotFoundException, CryptographicOperationException {

		List<String> shortCodes = new ArrayList<>();

		CodesMapping codesMapping = codesMappingRepository.findByTenantIdElectionEventIdVerificationCardId(tenantId, eeid, verificationCardId);

		String mappingString = new String(Base64.getDecoder().decode(codesMapping.getJson()), StandardCharsets.UTF_8);
		JsonObject mappingForCard = JsonUtils.getJsonObject(mappingString);

		CryptoAPIKDFDeriver kdfDeriver = primitivesService.getKDFDeriver();
		try {
			for (byte[] longCode : longCodes) {
				byte[] hashedLongCodeBytes = primitivesService.getHash(longCode);
				String hashedLongCode = Base64.getEncoder().encodeToString(hashedLongCodeBytes);
				JsonString encryptedShortCodeJson = mappingForCard.getJsonString(hashedLongCode);
				if (encryptedShortCodeJson == null) {
					String errorMessage = String
							.format("Encrypted short code not found for tenant: %s, election event id: %s and verification card id: %s", tenantId,
									eeid, verificationCardId);
					throw new ResourceNotFoundException(errorMessage);
				}
				String encryptedShortCode = encryptedShortCodeJson.getString();
				byte[] encryptedShortCodeBytes = Base64.getDecoder().decode(encryptedShortCode);

				int derivedKeyLength = new SymmetricKeyPolicyFromProperties().getSecretKeyAlgorithmAndSpec().getKeyLength() / 8;
				CryptoAPIDerivedKey derivedKey = kdfDeriver.deriveKey(longCode, derivedKeyLength);
				SecretKey shortCodeKey = symmetricService.getSecretKeyForEncryptionFromDerivedKey(derivedKey);
				byte[] shortCodeBytes = symmetricService.decrypt(shortCodeKey, encryptedShortCodeBytes);
				String shortCodeString = new String(shortCodeBytes, StandardCharsets.UTF_8);
				if (shortCodes.contains(shortCodeString)) {
					throw new CryptographicOperationException("Duplicated short choice code retrieved");
				}
				shortCodes.add(shortCodeString);
			}

			return shortCodes;

		} catch (GeneralCryptoLibException e) {
			throw new CryptographicOperationException("Error calculating short choice codes:", e);
		}
	}

}
