/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.extendedkeystore.configuration.ExtendedKeyStoreP12PolicyFromProperties;
import ch.post.it.evoting.cryptolib.extendedkeystore.configuration.ExtendedKeyStorePolicyFromProperties;
import ch.post.it.evoting.cryptolib.extendedkeystore.tests.BaseExtendedKeyStoreTests;

class ExtendedKeyStoreWithPBKDFGeneratorFactoryTest extends BaseExtendedKeyStoreTests {

	ExtendedKeyStoreWithPBKDFGeneratorFactory _target;

	@BeforeEach
	public void setup() throws GeneralCryptoLibException {
		final ExtendedKeyStorePolicyFromProperties storePolicy = new ExtendedKeyStoreP12PolicyFromProperties();
		_target = new ExtendedKeyStoreWithPBKDFGeneratorFactory(storePolicy);
	}

	@Test
	void createTest() {

		final ExtendedKeyStoreWithPBKDFGenerator obj = _target.create();
		Assertions.assertNotNull(obj, "CryptoStore created should not be null");
	}
}
