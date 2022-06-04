/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.infrastructure.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.owasp.esapi.ESAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.json.JsonSanitizer;

/**
 * A wrapper for the sanitized data of http request to avoid Cross-Site Scripting XSS attacks.
 */
public class SanitizerDataHttpServletRequestWrapper extends HttpServletRequestWrapper {

	private static final String CONTENT_TYPE = "Content-Type";

	private static final String TEXT_CSV = "text/csv";

	private static final String WITH_CHARSET_UTF_8 = ";charset=UTF-8";

	private static final String QUERY_STRING = "QUERY STRING_";

	private static final String HEADER = "HEADER_";

	private static final String PARAMETER = "PARAMETER_";

	private static final String NO_HEADER = "No Header";

	private static final int LOG_MAX_STRING_WIDTH = 1024;

	private static final String SEPARATOR_LINE = "--------------------------------------------------------";

	private static final Logger LOGGER = LoggerFactory.getLogger(SanitizerDataHttpServletRequestWrapper.class);

	private final HttpServletRequest req;

	/**
	 * Constructor.
	 *
	 * @param request the HTTP request.
	 */
	public SanitizerDataHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		req = request;
	}

	/**
	 * Sanitize the input string to prevent injection and XSS attacks
	 *
	 * @param value the concrete string value to be evaluated.
	 * @return sanitized data.
	 */
	private static String sanitize(String value) {
		if (value == null) {
			return null;
		}

		String debugStatement = "Original value: " + value;
		LOGGER.debug(debugStatement);

		String sanitizedData = value;

		// Normalize
		sanitizedData = Normalizer.normalize(sanitizedData, Normalizer.Form.NFKC);

		// Use the ESAPI library to avoid encoded attacks.
		sanitizedData = ESAPI.encoder().canonicalize(sanitizedData);

		// Avoid null characters
		sanitizedData = StringUtils.replace(sanitizedData, "\0", "");

		// Clean out HTML
		sanitizedData = Jsoup.clean(sanitizedData, Safelist.none());

		debugStatement = "New value: " + sanitizedData;
		LOGGER.debug(debugStatement);
		LOGGER.debug(SEPARATOR_LINE);

		return sanitizedData;

	}

	// logging
	private static void checkIfDataIsSanitized(String where, String originalValue, String sanitizedValue) {
		if (!isDataSanitized(originalValue, sanitizedValue)) {
			LOGGER.error("String was sanitized at {}.", where);
			final String abbreviatedOriginalValue = StringUtils.abbreviate(originalValue, LOG_MAX_STRING_WIDTH);
			LOGGER.error("Original value was: \"{}\".", abbreviatedOriginalValue);
			final String abbreviatedSanitizedValue = StringUtils.abbreviate(sanitizedValue, LOG_MAX_STRING_WIDTH);
			LOGGER.error("Sanitized value is: \"{}\".", abbreviatedSanitizedValue);
			throw new IllegalArgumentException("Data is not valid at " + where);
		}
	}

	private static boolean isDataSanitized(String originalValue, String sanitizedValue) {
		return originalValue == null || sanitizedValue == null || originalValue.isEmpty() || originalValue.equals(sanitizedValue);
	}

	// CSV processing
	protected static void checkIfCSVIsSanitized(String input) {
		Stream.of(input.split("\\r?\\n")).map(String::trim).forEach(content -> {
			String sanitized = sanitize(content);
			checkIfDataIsSanitized("CSV element", content.replace("\\", ""), sanitized);
		});
	}

	// process HTTP request headers
	@Override
	public String getHeader(String name) {
		LOGGER.debug(SEPARATOR_LINE);
		LOGGER.debug("Header {}", name);
		String header = req.getHeader(name);
		String sanitizedHeader = sanitize(header);
		checkIfDataIsSanitized(HEADER + name, header, sanitizedHeader);
		return header;
	}

	// process input parameters in HTTP POST request
	@Override
	public ServletInputStream getInputStream() throws IOException {
		LOGGER.debug(SEPARATOR_LINE);
		LOGGER.debug("Input stream {}", req.getContentType());
		try {
			String header = req.getHeader(CONTENT_TYPE);
			ServletInputStream inputStream = req.getInputStream();

			String inputString;
			if (header != null) {
				switch (header) {
				case MediaType.APPLICATION_JSON + WITH_CHARSET_UTF_8:
				case MediaType.APPLICATION_JSON:
					inputString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
					String sanitizedJSON = JsonSanitizer.sanitize(inputString);
					checkIfDataIsSanitized(header, inputString, sanitizedJSON);
					return convertToServletInputStream(inputString);
				case TEXT_CSV:
					inputString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
					checkIfCSVIsSanitized(inputString);
					return convertToServletInputStream(inputString);
				default:
					inputString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
					String sanitizedIS = sanitize(inputString);
					checkIfDataIsSanitized(header, inputString, sanitizedIS);
					return convertToServletInputStream(inputString);
				}
			} else {
				inputString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
				String sanitizedIS = sanitize(inputString);
				checkIfDataIsSanitized(NO_HEADER, inputString, sanitizedIS);
				return convertToServletInputStream(inputString);
			}
		} catch (IOException e) {
			LOGGER.error("Error converting input stream to string in sanitizer data filter...");
			throw e;
		}
	}

	// converts string to stream
	public ServletInputStream convertToServletInputStream(String inputString) {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));
		return new ServletInputStream() {
			@Override
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}
		};
	}

	// process query string
	@Override
	public String getQueryString() {
		LOGGER.debug(SEPARATOR_LINE);
		LOGGER.debug("Query string");
		String queryString = req.getQueryString();
		Map<String, String[]> map;
		try {
			map = getQueryMap(queryString);
			checkIfMapValuesAreSanitized(QUERY_STRING, map);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Error getting the query string map. Something is bad with the encoding.");
			throw new IllegalArgumentException("Data is not valid at " + QUERY_STRING, e);
		}
		return queryString;
	}

	// check query string
	@VisibleForTesting
	Map<String, String[]> getQueryMap(String queryString) throws UnsupportedEncodingException {
		Map<String, String[]> queryMap = new HashMap<>();
		if (queryString != null) {
			String[] pairs = queryString.split("&");
			for (String pair : pairs) {
				int idx = pair.indexOf('=');
				// Minimum valid pair is "p="
				if (idx >= 1 && pair.length() >= 2) {
					queryMap.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
							new String[] { URLDecoder.decode(pair.substring(idx + 1), "UTF-8") });
				}
			}
		}
		return queryMap;
	}

	// process parameters
	@Override
	public String getParameter(String name) {
		LOGGER.debug(SEPARATOR_LINE);
		LOGGER.debug("Parameter {}", name);
		String parameter = req.getParameter(name);
		String sanitizedParameter = sanitize(parameter);
		checkIfDataIsSanitized(PARAMETER + parameter, parameter, sanitizedParameter);
		return parameter;
	}

	// process parameter values
	@Override
	public String[] getParameterValues(String name) {
		LOGGER.debug(SEPARATOR_LINE);
		LOGGER.debug("Parameter value {}", name);
		String[] values = req.getParameterValues(name);
		checkIfValuesAreSanitized(PARAMETER + name, values);
		return values;
	}

	// process parameter map
	@Override
	public Map<String, String[]> getParameterMap() {
		LOGGER.debug(SEPARATOR_LINE);
		LOGGER.debug("Parameter map");
		Map<String, String[]> map = req.getParameterMap();
		checkIfMapValuesAreSanitized(PARAMETER, map);
		return map;
	}

	// sanitize map values
	private void checkIfMapValuesAreSanitized(String name, Map<String, String[]> map) {
		for (Entry<String, String[]> entry : map.entrySet()) {
			String key = entry.getKey();
			LOGGER.debug("{} {}", name, key);
			String[] values = map.get(key);
			checkIfValuesAreSanitized(name + key, values);
		}
	}

	// sanitize array of values
	private String[] checkIfValuesAreSanitized(String name, String[] values) {
		String[] newValues = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			newValues[i] = sanitize(values[i]);
			checkIfDataIsSanitized(name, values[i], newValues[i]);
		}
		return newValues;
	}

}
