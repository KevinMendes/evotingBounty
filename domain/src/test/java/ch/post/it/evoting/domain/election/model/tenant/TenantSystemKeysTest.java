/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.tenant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class TenantSystemKeysTest {

	public static final String EXPECTED_TRUE = "Expected true because tenant exists";
	public static final String EXPECTED_FALSE = "Expected false because tenant was invalidated";

	@Test
	void whenGetTenantThatDoesNotExistThenFalse() {

		TenantSystemKeys tenantSystemKeys = new TenantSystemKeys();

		String errorMsg = "Expected false because tenant does not exist";
		assertFalse(tenantSystemKeys.getInitialized("XXXXXXXXX"), errorMsg);
	}

	@Test
	void whenGetTenantThatExistsThenTrue() {

		TenantSystemKeys tenantSystemKeys = new TenantSystemKeys();

		String tenantID = "100";
		Map<String, PrivateKey> privateKeys100 = new HashMap<>();

		tenantSystemKeys.setInitialized(tenantID, privateKeys100);

		String errorMsg = EXPECTED_TRUE;
		assertTrue(tenantSystemKeys.getInitialized(tenantID), errorMsg);
	}

	@Test
	void whenGetTwosTenantThatExistThenTrue() {

		TenantSystemKeys tenantSystemKeys = new TenantSystemKeys();

		String tenant100ID = "100";
		Map<String, PrivateKey> privateKeys100 = new HashMap<>();

		String tenant200ID = "200";
		Map<String, PrivateKey> privateKeys200 = new HashMap<>();

		tenantSystemKeys.setInitialized(tenant100ID, privateKeys100);
		tenantSystemKeys.setInitialized(tenant200ID, privateKeys200);

		String errorMsg = EXPECTED_TRUE;
		assertTrue(tenantSystemKeys.getInitialized(tenant100ID), errorMsg);
		assertTrue(tenantSystemKeys.getInitialized(tenant200ID), errorMsg);
	}

	@Test
	void whenInvalidateTenantThenItNoLongerRemoved() {

		TenantSystemKeys tenantSystemKeys = new TenantSystemKeys();

		String tenantID = "100";
		Map<String, PrivateKey> privateKeys100 = new HashMap<>();

		tenantSystemKeys.setInitialized(tenantID, privateKeys100);

		String errorMsg = EXPECTED_TRUE;
		assertTrue(tenantSystemKeys.getInitialized(tenantID), errorMsg);

		tenantSystemKeys.invalidate(tenantID);
		errorMsg = EXPECTED_FALSE;
		assertFalse(tenantSystemKeys.getInitialized(tenantID), errorMsg);
	}
}
