/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.infrastructure.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter to sanitize the request data to avoid Cross-Site Scripting XSS attacks.
 */
public class SanitizerDataFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SanitizerDataFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// This method is intentionally left blank
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		LOGGER.trace("SanitizerDataFilter starting");
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		SanitizerDataHttpServletRequestWrapper sanitizedRequest = new SanitizerDataHttpServletRequestWrapper(httpRequest);
		filterChain.doFilter(sanitizedRequest, response);
		LOGGER.trace("SanitizerDataFilter ending");
	}

	@Override
	public void destroy() {
		// This method is intentionally left blank
	}
}
