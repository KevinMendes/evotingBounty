/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.elgamal.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalEncryptionParameters;
import ch.post.it.evoting.cryptolib.elgamal.bean.VerifiableElGamalEncryptionParameters;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;

/**
 * Utility to generate various types of ElGamal related data needed by tests.
 */
public class ElGamalTestDataGenerator {

	/**
	 * Retrieves the ElGamal encryption parameters for a pre-generated Zp subgroup.
	 *
	 * @param zpSubgroup the pre-generated Zp subgroup.
	 * @return the ElGamal encryption parameters.
	 * @throws GeneralCryptoLibException if the ElGamal encryption parameters cannot be retrieved.
	 */
	public static ElGamalEncryptionParameters getElGamalEncryptionParameters(final ZpSubgroup zpSubgroup) throws GeneralCryptoLibException {

		return new ElGamalEncryptionParameters(zpSubgroup.getP(), zpSubgroup.getQ(), zpSubgroup.getG());
	}

	/**
	 * Creates an instance of {@link VerifiableElGamalEncryptionParameters} from a JSON string.
	 *
	 * @return {@link VerifiableElGamalEncryptionParameters}
	 * @throws GeneralCryptoLibException
	 */
	public static VerifiableElGamalEncryptionParameters getElGamalVerifiableEncryptionParameters() throws GeneralCryptoLibException, IOException {

		final String str;

		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("verifiable-encryption-parameters.json")))) {
			str = reader.lines().collect(Collectors.joining());
		}

		return VerifiableElGamalEncryptionParameters.fromJson(str);
	}

}
