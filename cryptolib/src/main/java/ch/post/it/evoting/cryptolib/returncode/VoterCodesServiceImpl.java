/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.returncode;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.StringUtils;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.symmetric.SymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpGroupElement;
import ch.post.it.evoting.cryptolib.returncode.constants.VoterCodesConstants;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SymmetricKeyPolicyFromProperties;

public class VoterCodesServiceImpl implements VoterCodesService {

	private static final int RADIX_TEN = 10;

	private final PrimitivesServiceAPI primitivesService;
	private final SymmetricServiceAPI symmetricService;

	public VoterCodesServiceImpl(final PrimitivesServiceAPI primitivesService, final SymmetricServiceAPI symmetricService) {
		this.primitivesService = primitivesService;
		this.symmetricService = symmetricService;
	}

	@Override
	public String generateShortVoteCastReturnCode() {
		return genRandomCode(VoterCodesConstants.NUM_DIGITS_VOTE_CASTING_CODE);
	}

	@Override
	public String generateShortChoiceReturnCode() {
		return genRandomCode(VoterCodesConstants.NUM_DIGITS_SHORT_CHOICE_CODE);
	}

	@Override
	public byte[] generateLongReturnCode(final String eeid, final String verificationCardId, final ZpGroupElement preReturnCode, final List<String> correctnessIDs) {
		Validate.validateUUID(eeid);
		Validate.validateUUID(verificationCardId);
		checkNotNull(preReturnCode, "The provided pre return code is null.");
		checkNotNull(correctnessIDs, "The provided correctnessId list is null.");
		correctnessIDs.forEach(Validate::validateUUID);

		final List<Object> objectsToHash = new ArrayList<>();
		objectsToHash.add(preReturnCode.getValue());
		objectsToHash.add(verificationCardId);
		objectsToHash.add(eeid);
		objectsToHash.addAll(correctnessIDs);

		return primitivesService.getHashOfObjects(objectsToHash.stream(), StandardCharsets.UTF_8);
	}

	@Override
	public CodesMappingTableEntry generateCodesMappingTableEntry(final byte[] shortReturnCodeBytes, final byte[] longReturnCodeBytes)
			throws GeneralCryptoLibException {

		// derive the return code encryption symmetric key (either the choice return code encryption symmetric key or the vote cast return code
		// encryption symmetric key)
		final int derivationKeyLength = new SymmetricKeyPolicyFromProperties().getSecretKeyAlgorithmAndSpec().getKeyLength() / Byte.SIZE;
		final CryptoAPIDerivedKey returnCodeEncryptionSymmetricKeyBytes = primitivesService.getKDFDeriver()
				.deriveKey(longReturnCodeBytes, derivationKeyLength);

		//  Can represent the choice return code encryption symmetric key or the vote cast return code encryption symmetric key
		//  returnCodeEncryptionSymmetricKey
		final SecretKey returnCodeEncryptionSymmetricKey = symmetricService
				.getSecretKeyForEncryptionFromDerivedKey(returnCodeEncryptionSymmetricKeyBytes);

		final byte[] encryptedShortReturnCode = symmetricService.encrypt(returnCodeEncryptionSymmetricKey, shortReturnCodeBytes);
		final byte[] hashedLongReturnCodeBytes = primitivesService.getHash(longReturnCodeBytes);

		return new CodesMappingTableEntry(hashedLongReturnCodeBytes, encryptedShortReturnCode);
	}

	/**
	 * Generates a code of random digits of the specified length. If the generated random number has less digits than the specified length, we left
	 * pad the string with zeroes.
	 *
	 * @param length the desired length of the code.
	 * @return a code of random digits of the specified length left padded with zeros if needed.
	 */
	private String genRandomCode(final int length) {
		checkArgument(length > 0, "Length of the code should be positive. Provided length %s", length);

		final BigInteger x = primitivesService.getCryptoRandomInteger().genRandomIntegerByDigits(length);

		return StringUtils.leftPad(x.toString(RADIX_TEN), length, '0');
	}
}
