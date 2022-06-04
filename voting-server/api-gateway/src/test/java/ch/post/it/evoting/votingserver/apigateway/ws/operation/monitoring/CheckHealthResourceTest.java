/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.monitoring;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CheckHealthResourceTest extends JerseyTest {

	private static final String URL_HEALTH_CHECK_CONTEXT_DATA = CheckHealthResource.RESOURCE_PATH;

	private CheckHealthResource sut;

	@Test
	public void getStatus() {

		int expectedStatus = 200;

		Response response = target(URL_HEALTH_CHECK_CONTEXT_DATA).request().get();

		Assert.assertEquals(expectedStatus, response.getStatus());
	}

	@Override
	protected Application configure() {
		sut = new CheckHealthResource();
		forceSet(TestProperties.CONTAINER_PORT, "0");
		return new ResourceConfig().register(sut);
	}
}
