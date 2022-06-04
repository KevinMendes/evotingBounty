/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.groups.utils;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;
import ch.post.it.evoting.cryptolib.test.tools.configuration.GroupLoader;

/**
 * Utility to generate various types of mathematical data needed by tests.
 */
public class MathematicalTestDataGenerator {

	/**
	 * Retrieves a pre-generated quadratic residue subgroup.
	 *
	 * @return the pre-generated quadratic residue subgroup.
	 * @throws GeneralCryptoLibException if the quadratic residue subgroup cannot be retrieved.
	 */
	public static ZpSubgroup getQrSubgroup() throws GeneralCryptoLibException {

		final GroupLoader qrGroupLoader = new GroupLoader();

		return new ZpSubgroup(qrGroupLoader.getG(), qrGroupLoader.getP(), qrGroupLoader.getQ());
	}

}
