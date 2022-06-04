/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.infrastructure.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.mockito.Mockito;

public class SanitizerDataHttpServletRequestWrapperTest {

	HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

	@Test
	public void checkSanitizedCSV() {
		try {
			SanitizerDataHttpServletRequestWrapper.checkIfCSVIsSanitized("a ,b ,c,d,e,f,g,h,i");
			SanitizerDataHttpServletRequestWrapper.checkIfCSVIsSanitized("a;b;c;d ; e;f");
			SanitizerDataHttpServletRequestWrapper.checkIfCSVIsSanitized("baba;[\"{\\\"publicKey\\\":{\\\"zpSubgroup\\\":{\\\"g\\\":\\\"Aw==\\\"");
		} catch (final Exception e) {
			fail("Error occurred");
		}
	}

	@Test
	public void checkNotSanitizedCSV() {
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> SanitizerDataHttpServletRequestWrapper.checkIfCSVIsSanitized("a, <script src='toto.js> , c"));
		assertEquals("Data is not valid at CSV element", exception.getMessage());
	}

	@Test
	public void nullQueryStringIsOK() throws UnsupportedEncodingException {
		final SanitizerDataHttpServletRequestWrapper sanitizer = new SanitizerDataHttpServletRequestWrapper(req);

		final Map<String, String[]> map = sanitizer.getQueryMap(null);
		assertTrue(map.isEmpty());
	}

	@Test
	public void emptyQueryStringIsOK() throws UnsupportedEncodingException {
		final SanitizerDataHttpServletRequestWrapper sanitizer = new SanitizerDataHttpServletRequestWrapper(req);

		final Map<String, String[]> map = sanitizer.getQueryMap("");
		assertTrue(map.isEmpty());
	}

	@Test
	public void notValidQueryStringThenEmptyQueryMap() throws UnsupportedEncodingException {
		final SanitizerDataHttpServletRequestWrapper sanitizer = new SanitizerDataHttpServletRequestWrapper(req);

		final Map<String, String[]> notValid1 = sanitizer.getQueryMap("p");
		assertTrue(notValid1.isEmpty());

		final Map<String, String[]> notValid2 = sanitizer.getQueryMap("=");
		assertTrue(notValid2.isEmpty());

		final Map<String, String[]> notValid3 = sanitizer.getQueryMap("=x");
		assertTrue(notValid3.isEmpty());

		final Map<String, String[]> notValid4 = sanitizer.getQueryMap("=&p");
		assertTrue(notValid4.isEmpty());
	}

	@Test
	public void notValidAndValidQueryStringThenPopulatedQueryMap() throws UnsupportedEncodingException {
		final SanitizerDataHttpServletRequestWrapper sanitizer = new SanitizerDataHttpServletRequestWrapper(req);

		final Map<String, String[]> notValidAndValid = sanitizer.getQueryMap("p&p2=v2");
		assertEquals(1, notValidAndValid.size());
		final String[] values = notValidAndValid.get("p2");
		assertEquals(1, values.length);
		assertEquals("v2", values[0]);

		final Map<String, String[]> notValidAndValid2 = sanitizer.getQueryMap("=&p==");
		assertEquals(1, notValidAndValid2.size());
		final String[] values2 = notValidAndValid2.get("p");
		assertEquals(1, values2.length);
		assertEquals("=", values2[0]);
	}

	@Test
	public void validQueryStringThenPopulatedQueryMap() throws UnsupportedEncodingException {
		final SanitizerDataHttpServletRequestWrapper sanitizer = new SanitizerDataHttpServletRequestWrapper(req);

		final Map<String, String[]> valid1 = sanitizer.getQueryMap("p=");
		assertEquals(1, valid1.size());
		final String[] values1 = valid1.get("p");
		assertEquals(1, values1.length);
		assertTrue(values1[0].isEmpty());

		final Map<String, String[]> valid2 = sanitizer.getQueryMap("p=v");
		assertEquals(1, valid2.size());
		final String[] values2 = valid2.get("p");
		assertEquals(1, values2.length);
		assertEquals("v", values2[0]);

		final Map<String, String[]> valid3 = sanitizer.getQueryMap("p=v&p2=v2");
		assertEquals(2, valid3.size());
		final String[] values3 = valid3.get("p2");
		assertEquals(1, values3.length);
		assertEquals("v2", values3[0]);

		final Map<String, String[]> valid4 = sanitizer.getQueryMap("p==");
		assertEquals(1, valid4.size());
		final String[] values4 = valid4.get("p");
		assertEquals(1, values4.length);
		assertEquals("=", values4[0]);

		final Map<String, String[]> valid5 = sanitizer.getQueryMap("p=v&=v2");
		assertEquals(1, valid5.size());
		final String[] values5 = valid5.get("p");
		assertEquals(1, values5.length);
		assertEquals("v", values5[0]);
	}

}
