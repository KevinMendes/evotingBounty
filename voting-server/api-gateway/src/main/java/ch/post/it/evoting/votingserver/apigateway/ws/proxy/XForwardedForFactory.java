/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.proxy;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * This factory creates {@code X-Forwarded-For} headers for the incoming requests which should be forwarded to the internal services.
 * </p>
 * <p>
 * Implementation must be thread-safe.
 * </p>
 */
public interface XForwardedForFactory {
	/**
	 * Creates a {@code X-Forwarded-For} header for a given incoming request.
	 *
	 * @param request the request
	 * @return the header.
	 */
	String newXForwardedFor(HttpServletRequest request);
}
