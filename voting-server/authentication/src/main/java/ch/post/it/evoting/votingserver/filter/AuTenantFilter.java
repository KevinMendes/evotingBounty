/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.authentication.services.infrastructure.persistence.AuTenantSystemKeys;
import ch.post.it.evoting.votingserver.commons.util.UriParser;

public final class AuTenantFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuTenantFilter.class);

	private static final String SEARCH_STRING = "/tenant/";

	private static final String PART_OF_LOGGING_REQUEST = "/platformdata/";

	private static final String PART_OF_ACTIVATE_TENANT_REQUEST = "/tenantdata/";

	private static final String PART_OF_CHECK_RESOURCE = "/check";

	private final UriParser uriParser = new UriParser(SEARCH_STRING);

	@Inject
	private AuTenantSystemKeys auTenantSystemKeys;

	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(final FilterConfig filterConfig) {

		LOGGER.info(String.format("Initializing filter - name: %s, path: %s, container: %s", filterConfig.getFilterName(),
				filterConfig.getServletContext().getContextPath(), filterConfig.getServletContext().getServerInfo()));

	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
			throws IOException, ServletException {

		String uri;
		if (request instanceof HttpServletRequest) {
			uri = ((HttpServletRequest) request).getRequestURI();
		} else {
			LOGGER.info("The request is not a HttpServletRequest. Aborting");
			return;
		}

		HttpServletResponse httpServletResponse;
		if (response instanceof HttpServletResponse) {
			httpServletResponse = (HttpServletResponse) response;
		} else {
			LOGGER.info("The response is not the expected type. Expected a HttpServletResponse. Aborting.");
			return;
		}

		String tenantID = uriParser.getValue(uri);

		if (auTenantSystemKeys.getInitialized(tenantID)) {

			filterChain.doFilter(request, response);

		} else if (uri.contains(PART_OF_ACTIVATE_TENANT_REQUEST)) {

			LOGGER.info("Forwarding tenant request");
			filterChain.doFilter(request, response);

		} else if (uri.contains(PART_OF_LOGGING_REQUEST)) {

			LOGGER.info("Forwarding platform request");
			filterChain.doFilter(request, response);

		} else if (uri.contains(PART_OF_CHECK_RESOURCE)) {

			LOGGER.debug("Forwarding to check health resource");
			filterChain.doFilter(request, response);

		} else if (StringUtils.isEmpty(tenantID)) {

			LOGGER.info("Request Ignored. Failed to find tenant ID in request");
			httpServletResponse.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());

		} else {

			LOGGER.info(String.format("Request Ignored. Tenant not activated. Tenant: %s", tenantID));
			httpServletResponse.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());

		}
	}

	@Override
	public void destroy() {
		LOGGER.info("AU - destroying filter");
	}
}
