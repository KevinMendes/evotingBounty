/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.returncode;

import java.util.List;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpGroupElement;

public interface VoterCodesService {

	/**
	 * Generate a vote cast return code.
	 *
	 * @return the generated vote cast return code.
	 */
	String generateShortVoteCastReturnCode();

	/**
	 * Generate a short choice return code.
	 *
	 * @return the generated short choice return code.
	 */
	String generateShortChoiceReturnCode();

	/**
	 * Generate a long return code.
	 *
	 * @return the generated long return code.
	 */
	byte[] generateLongReturnCode(String eeid, String verificationCardId, ZpGroupElement preReturnCode, List<String> correctnessIDs);

	/**
	 * Generate a choices code mapping.
	 *
	 * @return the generated choices code mapping.
	 */
	CodesMappingTableEntry generateCodesMappingTableEntry(final byte[] shortReturnCodeBytes, final byte[] longReturnCodeBytes)
			throws GeneralCryptoLibException;
}
