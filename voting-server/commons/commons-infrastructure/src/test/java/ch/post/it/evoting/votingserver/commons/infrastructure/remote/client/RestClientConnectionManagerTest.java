/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.remote.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.NodeIdentifier;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RestClientConnectionManagerTest {

	private static final String urlWithout_Interceptors = "/urlWithout_Interceptors";
	private static final String urlWith_Interceptors = "/urlWith_Interceptors";
	private static int httpPort;

	@BeforeAll
	public static void setUp() {
		setUpHttpServer();
	}

	private static int discoverFreePorts(int from, int to) throws IOException {
		int result = 0;
		ServerSocket tempServer = null;

		for (int i = from; i <= to; i++) {
			try {
				tempServer = new ServerSocket(i);
				result = tempServer.getLocalPort();
				break;

			} catch (IOException ex) {
				// try next port
			}
		}

		if (result == 0) {
			throw new IOException("no free port found");
		}

		tempServer.close();
		return result;
	}

	public static void setUpHttpServer() {
		try {

			httpPort = discoverFreePorts(50000, 60000);

			// setup the socket address
			InetSocketAddress address = new InetSocketAddress(httpPort);

			// initialise the HTTP server
			HttpServer httpServer = HttpServer.create(address, 0);
			httpServer.createContext(urlWithout_Interceptors + "/", new HandlerForUrlWithout_Interceptors());
			httpServer.createContext(urlWith_Interceptors + "/", new HandlerForUrlWith_Interceptors());

			httpServer.setExecutor(null); // creates a default executor
			httpServer.start();

		} catch (Exception exception) {
			throw new RuntimeException("Failed to create HTTP server on free port of localhost", exception);

		}
	}

	@Test
	void testClientWithoutInterceptor() throws RetrofitException {

		String url = "http://localhost:" + httpPort + urlWithout_Interceptors;

		RestClientConnectionManager restClientConnectionManager = RestClientConnectionManager.getInstance();

		Retrofit restAdapter = restClientConnectionManager.getRestClient(url);

		RetrofitTestResponse retrofitTestResponse = restAdapter.create(RetrofitTestResponse.class);

		Response<ResponseBody> response = RetrofitConsumer.executeCall(retrofitTestResponse.getResponse());

		assertEquals(200, response.code());
	}

	private String getSignatureFromResponseHeaders(Response<?> response) {
		String result = "";

		for (String header : response.headers().names()) {
			if (RestClientInterceptor.HEADER_SIGNATURE.equals(header)) {
				result = response.headers().get(header);
				break;
			}
		}
		return result;
	}

	@Test
	void testClientWithInterceptor() throws RetrofitException {

		String url = "http://localhost:" + httpPort + urlWith_Interceptors;

		RestClientConnectionManager restClientConnectionManager = RestClientConnectionManager.getInstance();

		AsymmetricService asymmetricService = new AsymmetricService();
		KeyPair keyPair = asymmetricService.getKeyPairForSigning();

		Retrofit restAdapter = restClientConnectionManager
				.getRestClientWithInterceptor(url, keyPair.getPrivate(), NodeIdentifier.SECURE_DATA_MANAGER);

		RetrofitTestResponse retrofitTestResponse = restAdapter.create(RetrofitTestResponse.class);

		Response<ResponseBody> response = RetrofitConsumer.executeCall(retrofitTestResponse.getResponse());

		String signature = getSignatureFromResponseHeaders(response);

		assertEquals(200, response.code());
		assertNotNull(signature);

	}

	@Test
	void testClientWithInterceptorAndQueryParams() throws RetrofitException {

		String url = "http://localhost:" + httpPort + urlWith_Interceptors;

		RestClientConnectionManager restClientConnectionManager = RestClientConnectionManager.getInstance();

		AsymmetricService asymmetricService = new AsymmetricService();
		KeyPair keyPair = asymmetricService.getKeyPairForSigning();

		Retrofit restAdapter = restClientConnectionManager
				.getRestClientWithInterceptor(url, keyPair.getPrivate(), NodeIdentifier.SECURE_DATA_MANAGER);

		RetrofitTestResponse retrofitTestResponse = restAdapter.create(RetrofitTestResponse.class);

		Response<ResponseBody> response = RetrofitConsumer.executeCall(retrofitTestResponse.getResponseWithQueryParams("p1", "p2"));

		String signature = getSignatureFromResponseHeaders(response);

		assertEquals(200, response.code());
		assertNotNull(signature);

	}

	@Test
	void testPostClientWithInterceptor() throws RetrofitException {

		String url = "http://localhost:" + httpPort + urlWith_Interceptors;

		RestClientConnectionManager restClientConnectionManager = RestClientConnectionManager.getInstance();

		AsymmetricService asymmetricService = new AsymmetricService();
		KeyPair keyPair = asymmetricService.getKeyPairForSigning();

		Retrofit restAdapter = restClientConnectionManager
				.getRestClientWithInterceptor(url, keyPair.getPrivate(), NodeIdentifier.SECURE_DATA_MANAGER);

		RetrofitTestResponse retrofitTestResponse = restAdapter.create(RetrofitTestResponse.class);

		RequestBody body = RequestBody.create(okhttp3.MediaType.parse("text"), ("this is a test body"));

		Response<ResponseBody> response = RetrofitConsumer.executeCall(retrofitTestResponse.postString(body));

		String signature = getSignatureFromResponseHeaders(response);

		assertEquals(200, response.code());
		assertNotNull(signature);

	}

	@Test
	void testPostPojo() throws RetrofitException, IOException {

		String url = "http://localhost:" + httpPort + urlWith_Interceptors;

		RestClientConnectionManager restClientConnectionManager = RestClientConnectionManager.getInstance();

		AsymmetricService asymmetricService = new AsymmetricService();
		KeyPair keyPair = asymmetricService.getKeyPairForSigning();

		Retrofit restAdapter = restClientConnectionManager
				.getRestClientWithInterceptorAndJacksonConverter(url, keyPair.getPrivate(), NodeIdentifier.SECURE_DATA_MANAGER);

		RetrofitTestResponse retrofitTestResponse = restAdapter.create(RetrofitTestResponse.class);

		TestPojo testPojo = new TestPojo();

		testPojo.setProperty1("simpleText");

		String complexText = "-----BEGIN CERTIFICATE-----\n" + "MIIDhTCCAm2gAwIBAgIUViDChuk4vZyQR7F9nqXXEb7kLUAwDQYJKoZIhvcNAQEL\n"
				+ "BQAwWDEWMBQGA1UEAwwNVGVuYW50IDEwMCBDQTEWMBQGA1UECwwNT25saW5lIFZv\n"
				+ "dGluZzEOMAwGA1UECgwFU2N5dGwxCTAHBgNVBAcMADELMAkGA1UEBhMCRVMwHhcN\n"
				+ "MTcwMTEwMTIwMDU2WhcNMTcwNTMxMjI1OTU5WjB/MT0wOwYDVQQDDDRBZG1pbmlz\n"
				+ "dHJhdGlvbkJvYXJkIGRiZmJlODhmNzI0MzQxYTZhMTQ5YjI2Zjc1MjVmMzhkMRYw\n"
				+ "FAYDVQQLDA1PbmxpbmUgVm90aW5nMQ4wDAYDVQQKDAVTY3l0bDEJMAcGA1UEBwwA\n"
				+ "MQswCQYDVQQGEwJFUzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAIHm\n"
				+ "RGpLMyqh6omBLry8642zQ9KRCA+fJUIietkY0LUeisDL/LU0rOUM9Mrzem2vtn7f\n"
				+ "zEV97rW9xaLLrLEmDGcXzuFC5DaXitl3wwEtmucO7ROrMNZCzgUYO8owMAhbboAh\n"
				+ "EWfsB07NgZ5UmnwQXjHDXVmQ0XhNpFVHQ78Oo+POdm3tJs94l2QgerOaWOPENOHu\n"
				+ "TmU/Dj3xHtK2MMchNtk+j5VWmHmQwHDOtpmNjARlrXQHpU9aC6TZS6N9q9oUJJ3Z\n"
				+ "r9IRg0j5GQMxl4nzxt+HkKLnsUMsYmzeLM7BS2gk8fo45i/OSl9pXk54t1NoWK2Y\n"
				+ "mari/eDInv4/5T/x3u8CAwEAAaMgMB4wDgYDVR0PAQH/BAQDAgbAMAwGA1UdEwEB\n"
				+ "/wQCMAAwDQYJKoZIhvcNAQELBQADggEBAA4FEqv2Z53obzoxeG9rPG9AGZBV3j66\n"
				+ "l5DKOswlkG3fPOghYhSJ8h5T5P4idP7acLcdz+AFnlMcfZItsY/wo5Xva4ZObR1J\n"
				+ "N1F67tMCM6FUSpgZECmhqlnuBMM4MrPVW5DIpQxQNBNZ+kALUx/iWZkMKqmXse6p\n"
				+ "MHBI0vJYnW5JXS4PddoYjxnHree7piXLia4uC30ON08/TayBuYNCy8K96k1YP83v\n"
				+ "dMs2oMK/mavopHXWG+DF9OBwuhbSdb1r5gXk4dhUdmxo7taSD9lN19fbWHAoDmDK\n" + "C2Mn7olmRrpYx+ir9oU93jU6xt34S8VIqeD2Xk4EuqTU5+F43oltsRY=\n"
				+ "-----END CERTIFICATE-----\n";

		String sanitizedText = complexText.replace("\n", " ");

		testPojo.setProperty2(sanitizedText);

		Response<ResponseBody> response = RetrofitConsumer.executeCall(retrofitTestResponse.postPojo(testPojo));

		String signature = getSignatureFromResponseHeaders(response);

		String responseBody = new String(response.body().bytes(), StandardCharsets.UTF_8);

		System.out.println(responseBody);

		assertEquals(200, response.code());
		assertNotNull(signature);
		assertFalse(responseBody.contains("\\\u003d"));
		assertFalse(responseBody.contains("\n"));

	}

	public static class HandlerForUrlWithout_Interceptors implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {

			String response = "This is the response from HandlerForUrlWithout_Interceptors.";

			t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes(StandardCharsets.UTF_8));
			os.close();
		}
	}

	public static class HandlerForUrlWith_Interceptors implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {

			String requestBody = IOUtils.toString(t.getRequestBody(), StandardCharsets.UTF_8);

			String response = "This is the response from HandlerForUrlWith_Interceptors. Request body: " + requestBody;

			System.out.println(response);

			if (t.getRequestHeaders().containsKey(RestClientInterceptor.HEADER_SIGNATURE)) {

				t.getResponseHeaders()
						.add(RestClientInterceptor.HEADER_SIGNATURE, t.getRequestHeaders().get(RestClientInterceptor.HEADER_SIGNATURE).toString());
			}

			t.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes(StandardCharsets.UTF_8));
			os.close();
		}
	}

}
