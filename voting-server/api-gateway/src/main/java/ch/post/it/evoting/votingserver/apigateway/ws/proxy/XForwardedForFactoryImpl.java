/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.proxy;

import javax.servlet.http.HttpServletRequest;

/**
 * Implementation of {@link XForwardedForFactory}.
 */
public final class XForwardedForFactoryImpl implements XForwardedForFactory {
	public static final String HEADER = "X-Forwarded-For";

	private static final XForwardedForFactoryImpl INSTANCE = new XForwardedForFactoryImpl();

	private XForwardedForFactoryImpl() {
	}

	/**
	 * Returns the instance.
	 *
	 * @return the instance.
	 */
	public static XForwardedForFactoryImpl getInstance() {
		return INSTANCE;
	}

	@Override
	public String newXForwardedFor(final HttpServletRequest request) {
		String remoteAddresses = request.getHeader(HEADER);
		if (remoteAddresses == null) {
			remoteAddresses = request.getRemoteAddr();
		}
		String localAddress = request.getLocalAddr();
		return remoteAddresses + ',' + localAddress;
	}
}
