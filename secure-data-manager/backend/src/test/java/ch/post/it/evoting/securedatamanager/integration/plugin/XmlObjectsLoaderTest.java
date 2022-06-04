/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.integration.plugin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

class XmlObjectsLoaderTest {

	@Test
	void testXmlDeserialization() throws URISyntaxException {
		final String resourcePath = this.getClass().getResource("/validPlugin.xml").toURI().getPath();
		assertDoesNotThrow(() -> XmlObjectsLoader.unmarshal(new File(resourcePath).toPath()));
	}
}
