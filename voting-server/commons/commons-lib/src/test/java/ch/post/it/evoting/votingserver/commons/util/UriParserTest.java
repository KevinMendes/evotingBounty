/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.BeforeClass;
import org.junit.Test;

public class UriParserTest {

	private static UriParser _target;

	@BeforeClass
	public static void setup() throws IOException, URISyntaxException {

		_target = new UriParser("/tenant/");
	}

	@Test
	public void whenEmptyUriThenEmptyString() {

		String uri = "";
		String tenantID = _target.getValue(uri);

		String errorMsg = "Expected an empty string when the URL is empty";
		assertEquals(errorMsg, tenantID, "");
	}

	@Test
	public void whenUriContainsNoSeperatorsThenEmptyString() {

		String uri = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		String tenantID = _target.getValue(uri);

		String errorMsg = "Expected an empty string when the URL contains no separators";
		assertEquals(errorMsg, tenantID, "");
	}

	@Test
	public void whenNoTenantIdParameterThenEmptyString() {

		String uri = "/au-ws-rest/tenantdata/activatetenant/";
		String tenantID = _target.getValue(uri);

		String errorMsg = "";
		assertEquals(errorMsg, tenantID, "");
	}

	@Test
	public void whenNoTenantIdParameterValueEmptyThenEmptyString() {

		String uri = "/au-ws-rest/tenant//electionevent/222222/votingcard/33333333";
		String tenantID = _target.getValue(uri);

		String errorMsg = "Expected an empty string when no tenenat parameter";
		assertEquals(errorMsg, tenantID, "");
	}

	@Test
	public void whenTenantFirstPartOfUriThenObtainId() {

		String uri = "/au-ws-rest/tenant/100/electionevent/222222/votingcard/33333333";
		String tenantID = _target.getValue(uri);

		String errorMsg = "Failed to parse expected tenant ID from URI";
		assertEquals(errorMsg, tenantID, "100");
	}

	@Test
	public void whenTenantSecondPartOfUriThenObtainId() {

		String uri = "/au-ws-rest/tokens/tenant/200/electionevent/222222/votingcard/33333333";
		String tenantID = _target.getValue(uri);

		String errorMsg = "Failed to parse expected tenant ID from URI";
		assertEquals(errorMsg, tenantID, "200");
	}

	@Test
	public void whenTenantLastPartOfUriAndTrailingSeparatorThenObtainId() {

		String uri = "/au-ws-rest/tokens/admin/request/tenant/300/";
		String tenantID = _target.getValue(uri);

		String errorMsg = "Failed to parse expected tenant ID from URI";
		assertEquals(errorMsg, tenantID, "300");
	}

	@Test
	public void whenTenantLastPartOfUriAndNoTrailingSeparatorThenObtainId() {

		String uri = "/au-ws-rest/tokens/admin/request/tenant/400";
		String tenantID = _target.getValue(uri);

		String errorMsg = "Failed to parse expected tenant ID from URI";
		assertEquals(errorMsg, tenantID, "400");
	}

	@Test
	public void whenTenantIdContains1Character() {

		String uri = "/au-ws-rest/tenant/6/electionevent/222222/votingcard/33333333";
		String tenantID = _target.getValue(uri);

		String errorMsg = "Failed to parse expected tenant ID from URI when contains 1 character";
		assertEquals(errorMsg, tenantID, "6");
	}

	@Test
	public void whenTenantIdContainsManyCharacters() {

		String uri = "/au-ws-rest/tenant/678912345566234589713451234523456/electionevent/222222/votingcard/33333333";
		String tenantID = _target.getValue(uri);

		String errorMsg = "Failed to parse expected tenant ID from URI when contains many characters";
		assertEquals(errorMsg, tenantID, "678912345566234589713451234523456");
	}

	@Test
	public void whenTenantIdOnlyContainsLetters() {

		String uri = "/au-ws-rest/tenant/AAAAAA/electionevent/222222/votingcard/33333333";
		String tenantID = _target.getValue(uri);

		String errorMsg = "Failed to parse expected tenant ID from URI when contains letters";
		assertEquals(errorMsg, tenantID, "AAAAAA");
	}
}
