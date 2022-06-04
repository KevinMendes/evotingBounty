/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cms;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.ConfigDigitalSignerAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;

/**
 * Tests of {@link DigitalSignerPolicyImpl}.
 */
public class DigitalSignerPolicyImplTest {
	@Test
	public void testGetDigitalSignerAlgorithmAndSpec() throws IOException {
		Properties properties = new Properties();
		try (InputStream stream = getClass().getResourceAsStream('/' + PolicyFromPropertiesHelper.CRYPTOLIB_POLICY_PROPERTIES_FILE_PATH)) {
			properties.load(stream);
		}
		String property = properties.getProperty("asymmetric.cms.signer");
		ConfigDigitalSignerAlgorithmAndSpec expected = ConfigDigitalSignerAlgorithmAndSpec.valueOf(property);
		ConfigDigitalSignerAlgorithmAndSpec actual = DigitalSignerPolicyImpl.newInstance().getDigitalSignerAlgorithmAndSpec();
		assertEquals(expected, actual);
	}
}
