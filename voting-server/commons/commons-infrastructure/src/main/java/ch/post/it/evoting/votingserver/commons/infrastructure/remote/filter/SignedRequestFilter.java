/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.remote.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.PublicKey;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.NodeIdentifier;
import ch.post.it.evoting.votingserver.commons.infrastructure.exception.OvCommonsInfrastructureException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientInterceptor;
import ch.post.it.evoting.votingserver.commons.sign.beans.SignedRequestContent;
import ch.post.it.evoting.votingserver.commons.verify.RequestVerifier;

/**
 * Filter to check Signature inside every request.
 */
public final class SignedRequestFilter implements Filter {

	public static final String URL_REGEX_INIT_PARAM_NAME = "urlRegularExpression";
	private static final Logger LOGGER = LoggerFactory.getLogger(SignedRequestFilter.class);
	private SignedRequestKeyManager signedRequestKeyManager;
	private String urlRegularExpression;

	/**
	 * @see Filter#init(FilterConfig)
	 */
	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {

		LOGGER.info("Initializing filter. [name: {}, path: {}, container: {}]", filterConfig.getFilterName(),
				filterConfig.getServletContext().getContextPath(), filterConfig.getServletContext().getServerInfo());

		this.urlRegularExpression = filterConfig.getInitParameter(URL_REGEX_INIT_PARAM_NAME);

		try {
			signedRequestKeyManager = SignedRequestKeyManager.getInstance();

		} catch (final OvCommonsInfrastructureException e) {
			throw new ServletException("Something went wrong when creating the filter: " + e, e);
		}
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
			throws IOException, ServletException {

		LOGGER.debug("Filtering request to verify Originator signature...");

		final String uri;

		if (request instanceof HttpServletRequest) {
			uri = ((HttpServletRequest) request).getRequestURI();
			LOGGER.debug("URL: {}", uri);
		} else {
			LOGGER.info("The request is not a HttpServletRequest. Aborting");
			return;
		}

		final HttpServletResponse httpServletResponse;
		if (response instanceof HttpServletResponse) {
			httpServletResponse = (HttpServletResponse) response;
		} else {
			LOGGER.info("The response is not the expected type. Expected a HttpServletResponse. Aborting.");
			return;
		}

		final String decodedUri = URLDecoder.decode(uri, "UTF-8");

		if (uriNeedsToBeFiltered(decodedUri)) {

			// Convert original request...
			/* wrap the request in order to read the inputStream multiple times */
			final MultiReadHttpServletRequest multiReadRequest = new MultiReadHttpServletRequest((HttpServletRequest) request);

			// Check 'Originator' header
			final NodeIdentifier nodeIdentifier;
			try {
				final String originatorStr = multiReadRequest.getHeader(RestClientInterceptor.HEADER_ORIGINATOR);
				nodeIdentifier = NodeIdentifier.valueOf(originatorStr);

			} catch (final IllegalArgumentException | NullPointerException e) {
				LOGGER.warn("The request has not identified Originator or it is invalid. Aborting", e);
				httpServletResponse.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
				return;
			}

			if (signatureIsValid(multiReadRequest, nodeIdentifier)) {
				LOGGER.info("Request signature in headers is VALID. Processing...");
				// PROCEED WITH THE REQUEST
				filterChain.doFilter(multiReadRequest, response);
			} else {
				LOGGER.warn("Request signature in headers is INVALID. Aborting...");
				httpServletResponse.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
			}
		} else {
			LOGGER.debug("URL not need to be filtered. Processing...");
			// PROCEED WITH THE REQUEST
			filterChain.doFilter(request, response);
		}
	}

	private boolean signatureIsValid(final MultiReadHttpServletRequest multiReadHttpServletRequest, final NodeIdentifier nodeIdentifier) {

		LOGGER.debug("Retrieving Originator public key... ");

		final PublicKey publicKey;
		publicKey = getPublicKey(nodeIdentifier);

		LOGGER.debug("Validating signature... ");

		final String signatureStr = multiReadHttpServletRequest.getHeader(RestClientInterceptor.HEADER_SIGNATURE);

		if (signatureStr == null) {
			LOGGER.warn("The Signature is empty in this request.");
			return false;
		}

		final byte[] signature;
		try {
			signature = Base64.getDecoder().decode(signatureStr);
		} catch (final IllegalArgumentException e) {
			LOGGER.warn("The Signature is malformed.", e);
			return false;
		}

		final RequestVerifier requestVerifier = new RequestVerifier();

		final String body = getRequestBodyToString(multiReadHttpServletRequest);

		final SignedRequestContent signedRequestContent = new SignedRequestContent(multiReadHttpServletRequest.getRequestURI(),
				multiReadHttpServletRequest.getMethod(), body, nodeIdentifier.name());

		boolean result;

		try {
			result = requestVerifier.verifySignature(signedRequestContent, signature, publicKey);

		} catch (final GeneralCryptoLibException e) {
			LOGGER.error("Error validating signature: " + e.getMessage(), e);
			return false;
		}
		return result;
	}

	@Override
	public void destroy() {
		LOGGER.info("destroying SignedRequestFilter");
	}

	@VisibleForTesting
	PublicKey getPublicKey(final NodeIdentifier nodeIdentifier) {

		return signedRequestKeyManager.getPublicKeyFromOriginator(nodeIdentifier);
	}

	/**
	 * Reads the request body from the request and returns it as a String.
	 *
	 * @param multiReadHttpServletRequest HttpServletRequest that contains the request body
	 * @return request body as a String or null
	 */
	@VisibleForTesting
	String getRequestBodyToString(final MultiReadHttpServletRequest multiReadHttpServletRequest) {
		try {
			// Read from request
			final StringBuilder buffer = new StringBuilder();
			final BufferedReader reader = multiReadHttpServletRequest.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			return buffer.toString();
		} catch (final Exception e) {
			LOGGER.error("Failed to read the request body from the request.", e);
		}
		return null;
	}

	/**
	 * Checks the URL against the regular expression to apply the filter.
	 */
	@VisibleForTesting
	boolean uriNeedsToBeFiltered(final String uri) {

		if (this.urlRegularExpression == null) {
			return false;
		}

		final Pattern p = Pattern.compile(this.urlRegularExpression);
		final Matcher m = p.matcher(uri);

		return m.matches();
	}
}
