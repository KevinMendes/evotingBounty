/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import okio.Buffer;
import retrofit2.Converter.Factory;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Singleton class to centralize the creation of Pooled Rest client. This class is prepared for SSL calls.
 */
public class RestClientService {

	private static final Logger LOG = LoggerFactory.getLogger(RestClientService.class);
	private static final OkHttpClient okHttpClient = null;
	private static final OkHttpClient okHttpClientWithInterceptor = null;
	private static RestClientService instance = null;
	private static InfrastructureConfig infrastructureConfig;

	private RestClientService() {
	}

	/**
	 * Returns a singleton instance of this class.
	 */
	public static RestClientService getInstance() {
		if (instance == null) {

			infrastructureConfig = InfrastructureConfig.getInstance();

			instance = new RestClientService();

			LOG.debug("New RestClientService has been created.");
		}
		return instance;
	}

	public Retrofit getRestClientWithJacksonConverter(String url) {
		return createRestClient(url, false, okHttpClient, null, null);
	}

	/**
	 * Returns a HTTP Rest Client initialized with a base URL, which is ready to create a new Retrofit Service. Depending on configured properties and
	 * url it will use SSL. It uses Retrofit connection pooling so the same client will be used for every call. All requests will be intercepted in
	 * order to be signed. It uses a Jackson converter instead of the GSON default converter.<b>Should not be used with data streams, as it is
	 * inefficient to read the content multiple times. Currently does not support regular input streams as it does not reset the stream
	 * automatically</b>
	 *
	 * @param url            - the base url of the endpoint.
	 * @param privateKey     - the private key to sign request.
	 * @param nodeIdentifier - Identified request originator.
	 * @return a HTTP Rest Client for making rest calls to a server.
	 */
	public Retrofit getRestClientWithInterceptorAndJacksonConverter(final String url, final PrivateKey privateKey, final String nodeIdentifier) {
		return createRestClient(url, true, okHttpClientWithInterceptor, privateKey, nodeIdentifier);
	}

	private Retrofit createRestClient(String url, boolean withInterceptor, OkHttpClient client, PrivateKey privateKey, String nodeIdentifier) {

		// Check if the client is already created, if not, then initialize it.
		OkHttpClient restClient = client;

		if (restClient == null) {
			LOG.debug("A new RestClient has been created.");
			restClient = new OkHttpClient();

			if (withInterceptor) {
				LOG.debug("A signature will be included in all requests header.");
				restClient = new Builder().addInterceptor(new RestClientInterceptor(privateKey, nodeIdentifier)).build();
			}
		}

		LOG.debug("A non SSL RestClient has been served.");
		// Returns a client without SSL.
		Builder newBuilder = restClient.newBuilder();

		// Returns a client without SSL.
		Retrofit restAdapter = createRetrofitClient(url, newBuilder);

		LOG.debug("A non SSL RestClient has been served.");
		return restAdapter;
	}

	private Retrofit createRetrofitClient(String url, Builder newBuilder) {
		HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
		logging.setLevel(Level.NONE);
		newBuilder.addInterceptor(logging);
		newBuilder.connectTimeout(infrastructureConfig.getSystemConnectionTimeOut(), TimeUnit.SECONDS);
		newBuilder.readTimeout(infrastructureConfig.getSystemReadTimeOut(), TimeUnit.SECONDS);
		newBuilder.writeTimeout(infrastructureConfig.getSystemWriteTimeOut(), TimeUnit.SECONDS);
		String validUrl = url;
		String retrofit2CompatibleUrlEnding = "/";
		if (!url.endsWith(retrofit2CompatibleUrlEnding)) {
			validUrl = url.concat(retrofit2CompatibleUrlEnding);
		}
		Factory factory;
		factory = JacksonConverterFactory.create(DomainObjectMapper.getNewInstance());

		return new Retrofit.Builder().addCallAdapterFactory(RxJavaCallAdapterFactory.create()).addConverterFactory(factory).baseUrl(validUrl)
				.client(newBuilder.build()).build();
	}

	/**
	 * Retrofit Interceptor class to include a Signature inside every request 'Signature' header.
	 */
	public static class RestClientInterceptor implements Interceptor {

		public static final String HEADER_SIGNATURE = "Signature";
		public static final String HEADER_ORIGINATOR = "Originator";
		private static final Logger LOG = LoggerFactory.getLogger(RestClientInterceptor.class);
		private final PrivateKey privateKey;
		private final String nodeIdentifier;

		public RestClientInterceptor(final PrivateKey privateKey, final String nodeIdentifier) {
			this.privateKey = privateKey;
			this.nodeIdentifier = nodeIdentifier;
		}

		@Override
		public okhttp3.Response intercept(Chain chain) throws IOException {

			Request originalRequest = chain.request();

			// Ask for a new signature
			String signature;
			try {
				signature = getEncodedSignatureString(originalRequest, nodeIdentifier);

			} catch (GeneralCryptoLibException e) {
				LOG.error("GeneralCryptoLibException");
				throw new IOException(e);
			}

			okhttp3.Response returnedResponse;

			Request.Builder requestBuilder = originalRequest.newBuilder().header(HEADER_SIGNATURE, signature)
					.header(HEADER_ORIGINATOR, nodeIdentifier);

			Request newRequest = requestBuilder.build();

			if (LOG.isDebugEnabled()) {
				LOG.debug("REQUEST HEADERS: {}", newRequest.headers());
			}

			returnedResponse = chain.proceed(newRequest);

			if (LOG.isDebugEnabled()) {
				LOG.debug("RESPONSE HEADERS: {}", returnedResponse.headers());
			}
			return returnedResponse;
		}

		private String getEncodedSignatureString(final Request request, final String nodeIdentifier) throws GeneralCryptoLibException {
			String body = "";
			if (request.body() != null) {
				body = getRequestBodyToString(request.body());
			}

			byte[] signature = sign(request.method(), body, nodeIdentifier, privateKey);

			return Base64.getEncoder().encodeToString(signature);
		}

		private String getRequestBodyToString(final RequestBody request) {
			try (final Buffer buffer = new Buffer()) {
				if (request != null) {
					request.writeTo(buffer);
				} else {
					return "";
				}
				return buffer.readUtf8();
			} catch (final IOException e) {
				final String errorMsg = "Exception occurred during process body to String";
				LOG.error(errorMsg, e);
				return "";
			}
		}

		private byte[] sign(final String method, final String body, final String originator, final PrivateKey privateKey)
				throws GeneralCryptoLibException {
			checkNotNull(method);
			checkNotNull(body);
			checkNotNull(originator);
			checkNotNull(privateKey);

			AsymmetricService asymmetricService = new AsymmetricService();

			StringBuilder sb = new StringBuilder();
			byte[] objectBytes;
			// If the body is empty it will not be included in signature.
			if (StringUtils.isEmpty(body)) {
				objectBytes = sb.append(method).append(originator).toString().getBytes(StandardCharsets.UTF_8);
			} else {
				objectBytes = sb.append(method).append(body).append(originator).toString().getBytes(StandardCharsets.UTF_8);
			}

			PrimitivesServiceAPI primitivesService = new PrimitivesService();
			byte[] objectHash = primitivesService.getHash(objectBytes);

			return asymmetricService.sign(privateKey, objectHash);
		}

		public static class SignedRequestContent {

			private final String method;
			private final String body;
			private final String originator;

			public SignedRequestContent(final String method, final String body, final String originator) {
				this.method = method;
				this.body = body;
				this.originator = originator;
			}

			public byte[] getBytes() {
				StringBuilder sb = new StringBuilder();

				// If the body is empty it will not be included in signature.
				if (StringUtils.isEmpty(body)) {
					return sb.append(method).append(originator).toString().getBytes(StandardCharsets.UTF_8);
				} else {
					return sb.append(method).append(body).append(originator).toString().getBytes(StandardCharsets.UTF_8);
				}
			}

		}

	}

	/**
	 * A Singleton class to deal with module configuration properties.
	 */
	public static class InfrastructureConfig {

		private static final InfrastructureConfig INSTANCE = new InfrastructureConfig();

		private final String systemReadTimeOut;
		private final String systemWriteTimeOut;
		private final String systemConnectionTimeOut;

		private InfrastructureConfig() {
			systemReadTimeOut = System.getenv("READ_TIME_OUT");
			systemWriteTimeOut = System.getenv("WRITE_TIME_OUT");
			systemConnectionTimeOut = System.getenv("CONNECTION_TIME_OUT");
		}

		public static InfrastructureConfig getInstance() {
			return INSTANCE;
		}

		public long getSystemReadTimeOut() {
			return systemReadTimeOut != null ? Long.parseLong(systemReadTimeOut) : 60L;
		}

		public long getSystemWriteTimeOut() {
			return systemWriteTimeOut != null ? Long.parseLong(systemWriteTimeOut) : 60L;
		}

		public long getSystemConnectionTimeOut() {
			return systemConnectionTimeOut != null ? Long.parseLong(systemConnectionTimeOut) : 60L;
		}

	}

}
