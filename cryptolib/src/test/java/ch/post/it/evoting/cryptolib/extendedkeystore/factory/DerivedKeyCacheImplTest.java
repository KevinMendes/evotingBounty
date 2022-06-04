/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.factory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DerivedKeyCacheImplTest {

	private static final String ALIAS1 = "alias1";
	private static final String ALIAS2 = "alias2";
	private static final char[] PASSWORD1 = "password1".toCharArray();
	private static final char[] PASSWORD2 = "password2".toCharArray();
	private static final byte[] KEY1 = "key1".getBytes(StandardCharsets.UTF_8);
	private static final byte[] KEY2 = "key2".getBytes(StandardCharsets.UTF_8);

	private DerivedKeyCache cache;

	@BeforeEach
	void setUp() {
		cache = new DerivedKeyCacheImpl();
	}

	@Test
	void testGetEmpty() {
		assertNull(cache.get(PASSWORD1));
	}

	@Test
	void testPutForElGamalPrivatelKeyNew() {
		cache.putForElGamalPrivateKey(ALIAS1, PASSWORD1, KEY1);

		assertArrayEquals(KEY1, cache.get(PASSWORD1));
	}

	@Test
	void testPutForElGamalPrivatelKeyTheSame() {
		cache.putForElGamalPrivateKey(ALIAS1, PASSWORD1, KEY1);
		cache.putForElGamalPrivateKey(ALIAS1, PASSWORD1, KEY1);

		assertArrayEquals(KEY1, cache.get(PASSWORD1));
	}

	@Test
	void testPutForElGamalPrivatelKeyDifferent() {
		cache.putForElGamalPrivateKey(ALIAS1, PASSWORD1, KEY1);
		cache.putForElGamalPrivateKey(ALIAS1, PASSWORD2, KEY2);

		assertArrayEquals(KEY2, cache.get(PASSWORD2));
		assertNull(cache.get(PASSWORD1));
	}

	@Test
	void testPutForElGamalPrivatelKeyTheShared() {
		cache.putForElGamalPrivateKey(ALIAS1, PASSWORD1, KEY1);
		cache.putForElGamalPrivateKey(ALIAS2, PASSWORD1, KEY1);
		cache.putForElGamalPrivateKey(ALIAS1, PASSWORD2, KEY2);

		assertArrayEquals(KEY1, cache.get(PASSWORD1));
	}

	@Test
	void testPutForKeyStoreNew() {
		cache.putForKeyStore(PASSWORD1, KEY1);

		assertArrayEquals(KEY1, cache.get(PASSWORD1));
	}

	@Test
	void testPutForKeyStoreShared() {
		cache.putForPrivateKey(ALIAS1, PASSWORD1, KEY1);
		cache.putForKeyStore(PASSWORD1, KEY1);
		cache.putForPrivateKey(ALIAS1, PASSWORD2, KEY2);

		assertArrayEquals(KEY1, cache.get(PASSWORD1));
	}

	@Test
	void testPutForKeyStoreSame() {
		cache.putForKeyStore(PASSWORD1, KEY1);
		cache.putForKeyStore(PASSWORD1, KEY1);

		assertArrayEquals(KEY1, cache.get(PASSWORD1));
	}

	@Test
	void testPutForKeyStoreDifferent() {
		cache.putForKeyStore(PASSWORD1, KEY1);
		cache.putForKeyStore(PASSWORD2, KEY2);

		assertArrayEquals(KEY2, cache.get(PASSWORD2));
		assertNull(cache.get(PASSWORD1));
	}

	@Test
	void testPutForPrivateKeyNew() {
		cache.putForPrivateKey(ALIAS1, PASSWORD1, KEY1);

		assertArrayEquals(KEY1, cache.get(PASSWORD1));
	}

	@Test
	void testPutForPrivateKeyShared() {
		cache.putForPrivateKey(ALIAS1, PASSWORD1, KEY1);
		cache.putForPrivateKey(ALIAS2, PASSWORD1, KEY1);
		cache.putForPrivateKey(ALIAS1, PASSWORD2, KEY2);

		assertArrayEquals(KEY1, cache.get(PASSWORD1));
	}

	@Test
	void testPutForPrivateKeySame() {
		cache.putForPrivateKey(ALIAS1, PASSWORD1, KEY1);
		cache.putForPrivateKey(ALIAS1, PASSWORD1, KEY1);

		assertArrayEquals(KEY1, cache.get(PASSWORD1));
	}

	@Test
	void testPutForPrivateKeyDifferent() {
		cache.putForPrivateKey(ALIAS1, PASSWORD1, KEY1);
		cache.putForPrivateKey(ALIAS1, PASSWORD2, KEY2);

		assertArrayEquals(KEY2, cache.get(PASSWORD2));
		assertNull(cache.get(PASSWORD1));
	}

	@Test
	void testPutForSecretKeyNew() {
		cache.putForSecretKey(ALIAS1, PASSWORD1, KEY1);

		assertArrayEquals(KEY1, cache.get(PASSWORD1));
	}

	@Test
	void testPutForSecretKeyShared() {
		cache.putForSecretKey(ALIAS2, PASSWORD1, KEY1);
		cache.putForSecretKey(ALIAS1, PASSWORD1, KEY1);
		cache.putForSecretKey(ALIAS2, PASSWORD2, KEY2);

		assertArrayEquals(KEY1, cache.get(PASSWORD1));
	}

	@Test
	void testPutForSecretKeyDifferent() {
		cache.putForSecretKey(ALIAS1, PASSWORD1, KEY1);
		cache.putForSecretKey(ALIAS1, PASSWORD2, KEY2);

		assertArrayEquals(KEY2, cache.get(PASSWORD2));
		assertNull(cache.get(PASSWORD1));
	}

	@Test
	void testPutForSecretKeySame() {
		cache.putForSecretKey(ALIAS1, PASSWORD1, KEY1);
		cache.putForSecretKey(ALIAS1, PASSWORD1, KEY1);

		assertArrayEquals(KEY1, cache.get(PASSWORD1));
	}

	@Test
	void testRemoveForElGamalPrivateKeyMissing() {
		assertDoesNotThrow(() -> cache.removeForElGamalPrivateKey(ALIAS1));
	}

	@Test
	void testRemoveForElGamalPrivateKeyExisting() {
		cache.putForElGamalPrivateKey(ALIAS1, PASSWORD1, KEY1);
		cache.removeForElGamalPrivateKey(ALIAS1);

		assertNull(cache.get(PASSWORD1));
	}

	@Test
	void testRemoveForElGamalPrivateKeyShared() {
		cache.putForElGamalPrivateKey(ALIAS1, PASSWORD1, KEY1);
		cache.putForElGamalPrivateKey(ALIAS2, PASSWORD1, KEY1);
		cache.removeForElGamalPrivateKey(ALIAS1);

		assertArrayEquals(KEY1, cache.get(PASSWORD1));
	}

	@Test
	void testRemoveForPrivateKeyMissing() {
		assertDoesNotThrow(() -> cache.removeForPrivateKey(ALIAS1));
	}

	@Test
	void testRemoveForPrivateKeyExisting() {
		cache.putForPrivateKey(ALIAS1, PASSWORD1, KEY1);
		cache.removeForPrivateKey(ALIAS1);

		assertNull(cache.get(PASSWORD1));
	}

	@Test
	void testRemoveForPrivateKeyShared() {
		cache.putForPrivateKey(ALIAS1, PASSWORD1, KEY1);
		cache.putForPrivateKey(ALIAS2, PASSWORD1, KEY1);
		cache.removeForPrivateKey(ALIAS1);

		assertArrayEquals(KEY1, cache.get(PASSWORD1));
	}

	@Test
	void testRemoveForSecretKeyMissing() {
		assertDoesNotThrow(() -> cache.removeForSecretKey(ALIAS1));
	}

	@Test
	void testRemoveForSecretKeyExisting() {
		cache.putForSecretKey(ALIAS1, PASSWORD1, KEY1);
		cache.removeForSecretKey(ALIAS1);

		assertNull(cache.get(PASSWORD1));
	}

	@Test
	void testRemoveForSecretKeyShared() {
		cache.putForSecretKey(ALIAS1, PASSWORD1, KEY1);
		cache.putForSecretKey(ALIAS2, PASSWORD1, KEY1);
		cache.removeForSecretKey(ALIAS1);

		assertArrayEquals(KEY1, cache.get(PASSWORD1));
	}
}
