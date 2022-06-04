/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.remote.filter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.NodeIdentifier;
import ch.post.it.evoting.votingserver.commons.exception.OvCommonsSignException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientInterceptor;
import ch.post.it.evoting.votingserver.commons.sign.RequestSigner;
import ch.post.it.evoting.votingserver.commons.sign.beans.SignedRequestContent;
import ch.post.it.evoting.votingserver.commons.verify.RequestVerifier;

class SignedRequestFilterTest {

	@BeforeAll
	static void setUpAll() {
		// Make sure the SignedRequestKeyManager has not already been instantiated by another JUnit test.
		SignedRequestKeyManager.instance = null;
	}

	@Test
	void testFilterLetsTheRequestToContinue()
			throws IOException, ServletException, OvCommonsSignException, GeneralCryptoLibException {
		// create the objects to be mocked
		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
		FilterChain filterChain = mock(FilterChain.class);

		// Create the request content
		String url = "/test";
		String method = "GET";
		String body = "1..2..3, This is the request body!";
		NodeIdentifier nodeIdentifier = NodeIdentifier.CONFIG_PLATFORM_ROOT;
		String requestOriginatorName = nodeIdentifier.name();
		SignedRequestContent signedRequestContent = new SignedRequestContent(url, method, body, requestOriginatorName);

		// Generate keys to be used
		AsymmetricService asymmetricService = new AsymmetricService();
		KeyPair keyPair = asymmetricService.getKeyPairForSigning();
		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();

		// Generate request signature and verify it
		RequestSigner requestSigner = new RequestSigner();
		RequestVerifier requestVerifier = new RequestVerifier();
		byte[] signature = requestSigner.sign(signedRequestContent, privateKey);
		String signatureStrEnc = Base64.getEncoder().encodeToString(signature);
		boolean verified = requestVerifier.verifySignature(signedRequestContent, signature, publicKey);

		assertTrue(verified);

		when(httpServletRequest.getRequestURI()).thenReturn(url);
		when(httpServletRequest.getMethod()).thenReturn(method);
		when(httpServletRequest.getHeader(RestClientInterceptor.HEADER_SIGNATURE)).thenReturn(signatureStrEnc);
		when(httpServletRequest.getHeader(RestClientInterceptor.HEADER_ORIGINATOR)).thenReturn(requestOriginatorName);

		final SignedRequestFilter signedRequestFilter = Mockito.spy(SignedRequestFilter.class);
		doReturn(publicKey).when(signedRequestFilter).getPublicKey(any());
		when(signedRequestFilter.getRequestBodyToString(any())).thenReturn(body);
		when(signedRequestFilter.uriNeedsToBeFiltered(anyString())).thenReturn(true);

		signedRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

		// Verify if the response has no more interactions meaning passed the filter.
		verifyNoMoreInteractions(httpServletResponse);
	}

	@Test
	void testFilterRequest401()
			throws IOException, ServletException, OvCommonsSignException, GeneralCryptoLibException {
		// create the objects to be mocked
		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
		HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
		FilterChain filterChain = mock(FilterChain.class);

		String url = "/test";
		String method = "GET";
		String body = "1..2..3, This is the request body!";
		String originator = NodeIdentifier.CONFIG_PLATFORM_ROOT.name();

		AsymmetricService asymmetricService = new AsymmetricService();
		KeyPair keyPair = asymmetricService.getKeyPairForSigning();
		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();

		SignedRequestContent signedRequestContent = new SignedRequestContent("/wrongUrlToFail", method, body, originator);

		RequestSigner requestSigner = new RequestSigner();
		byte[] signature = requestSigner.sign(signedRequestContent, privateKey);
		String signatureStrEnc = Base64.getEncoder().encodeToString(signature);

		RequestVerifier requestVerifier = new RequestVerifier();
		boolean verified = requestVerifier.verifySignature(signedRequestContent, signature, publicKey);

		assertTrue(verified);

		when(httpServletRequest.getRequestURI()).thenReturn(url);
		when(httpServletRequest.getMethod()).thenReturn(method);
		when(httpServletRequest.getHeader(RestClientInterceptor.HEADER_SIGNATURE)).thenReturn(signatureStrEnc);
		when(httpServletRequest.getHeader(RestClientInterceptor.HEADER_ORIGINATOR)).thenReturn(NodeIdentifier.ADMIN_PORTAL.name());

		final SignedRequestFilter signedRequestFilter = Mockito.spy(SignedRequestFilter.class);
		doReturn(publicKey).when(signedRequestFilter).getPublicKey(any());
		when(signedRequestFilter.uriNeedsToBeFiltered(anyString())).thenReturn(true);

		signedRequestFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

		// verify if a setStatus() was performed with the expected code.
		verify(httpServletResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Test
	void testEnum() {

		NodeIdentifier nodeIdentifier;
		try {
			String originatorStr = NodeIdentifier.ADMIN_PORTAL.name();
			nodeIdentifier = NodeIdentifier.valueOf(originatorStr);

		} catch (IllegalArgumentException e) {
			return;
		}
		assertNotNull(nodeIdentifier);

		try {
			String originatorStr = "wrong-value";
			nodeIdentifier = NodeIdentifier.valueOf(originatorStr);

		} catch (IllegalArgumentException e) {
			nodeIdentifier = null;
		}

		assertNull(nodeIdentifier);

	}

	@Test
	void testRegularExpressions() {
		Pattern p = Pattern.compile(".*secured.*|.*platformdata.*");

		Matcher m = p.matcher("/whatever/secured");
		boolean b = m.matches();
		assertTrue(b);

	}

}
