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
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter to prevent the browser from unnecessarily caching resources.
 */
public class CacheControlFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheControlFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// This method is intentionally left blank
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		LOGGER.trace("CacheControlFilter starting");
		HttpServletResponse resp = (HttpServletResponse) response;
		resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		filterChain.doFilter(request, response);
		LOGGER.trace("CacheControlFilter ending");
	}

	@Override
	public void destroy() {
		// This method is intentionally left blank
	}
}
