/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.remote.client;

import java.security.PrivateKey;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.stores.keystore.configuration.KeystorePasswords;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.KeystorePasswordsReader;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.KeystorePasswordsReaderFactory;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.KeystoreReader;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.KeystoreReaderFactory;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.NodeIdentifier;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Converter.Factory;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Singleton class to centralize the creation of Pooled Rest client. This class is prepared for SSL calls.
 */
public class RestClientConnectionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestClientConnectionManager.class);
	private static final KeystorePasswordsReaderFactory KEYSTORE_PASSWORDS_READER_FACTORY = new KeystorePasswordsReaderFactory();
	private static final KeystoreReader KEYSTORE_READER = new KeystoreReaderFactory().getInstance();
	private static final OkHttpClient okHttpClient = null;
	private static final OkHttpClient okHttpClientWithInterceptor = null;
	private static RestClientConnectionManager instance = null;

	private RestClientConnectionManager() {
	}

	/**
	 * Returns a singleton instance of this class.
	 */
	public static RestClientConnectionManager getInstance() {
		if (instance == null) {

			instance = new RestClientConnectionManager();

			LOGGER.debug("New RestClientConnectionManager has been created.");
		}
		return instance;
	}

	/**
	 * Returns a HTTP Rest Client initialized with a base URL, which is ready to create a new Retrofit Service. Depending on the configured properties
	 * and url it will use SSL. It uses Retrofit connection pooling so the same client will be used for every call.
	 *
	 * @param url - the base url of the endpoint.
	 * @return a HTTP Rest Client for making rest calls to a server.
	 */
	public Retrofit getRestClient(String url) {
		return createRestClient(url, false, okHttpClient, null, null, false);
	}

	public Retrofit getRestClientWithJacksonConverter(String url) {
		return createRestClient(url, false, okHttpClient, null, null, true);
	}

	/**
	 * Returns a HTTP Rest Client initialized with a base URL, which is ready to create a new Retrofit Service. Depending on the configured properties
	 * and url it will use SSL. It uses Retrofit connection pooling so the same client will be used for every call. All requests will be intercepted
	 * in order to be signed. It uses a default GSON converter. <b>Should not be used with data streams, as it is inefficient to read the content
	 * multiple times. Currently does not support regular input streams as it does not reset the stream automatically</b>.
	 *
	 * @param url            - the base url of the endpoint.
	 * @param privateKey     - the private key to sign the request.
	 * @param nodeIdentifier - Identified request originator.
	 * @return a HTTP Rest Client for making rest calls to a server.
	 */
	public Retrofit getRestClientWithInterceptor(final String url, final PrivateKey privateKey, NodeIdentifier nodeIdentifier) {
		return createRestClient(url, true, okHttpClientWithInterceptor, privateKey, nodeIdentifier, false);
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
	public Retrofit getRestClientWithInterceptorAndJacksonConverter(final String url, final PrivateKey privateKey,
			final NodeIdentifier nodeIdentifier) {
		return createRestClient(url, true, okHttpClientWithInterceptor, privateKey, nodeIdentifier, true);
	}

	/**
	 * New factory method to obtain a configuration client with interceptor, whose signing private key is retrieved by the KeystoreReader inside the
	 * method rather than provided as a parameter to the method.
	 *
	 * @param url
	 * @param nodeIdentifier
	 * @return
	 */
	public Retrofit getConfigurationRestClientWithInterceptor(final String url, final NodeIdentifier nodeIdentifier) {
		final KeystorePasswordsReader keystorePasswordsReader = KEYSTORE_PASSWORDS_READER_FACTORY.getInstance(nodeIdentifier);
		final KeystorePasswords keystorePasswords = keystorePasswordsReader.read();
		final PrivateKey privateKey = KEYSTORE_READER.readSigningPrivateKey(nodeIdentifier, keystorePasswords);
		keystorePasswords.destroy();
		return this.getRestClientWithInterceptor(url, privateKey, nodeIdentifier);
	}

	private Retrofit createRestClient(String url, boolean withInterceptor, OkHttpClient client, PrivateKey privateKey, NodeIdentifier nodeIdentifier,
			final boolean useJacksonConverter) {

		// Check if the client is already created, if not, then initialize it.
		OkHttpClient restClient = client;

		if (restClient == null) {
			LOGGER.debug("A new RestClient has been created.");
			restClient = new OkHttpClient();

			if (withInterceptor) {
				LOGGER.debug("A signature will be included in all requests header.");
				restClient = new OkHttpClient.Builder().addInterceptor(new RestClientInterceptor(privateKey, nodeIdentifier)).build();
			}
		}

		LOGGER.debug("A non SSL RestClient has been served.");
		// Returns a client without SSL.
		Builder newBuilder = restClient.newBuilder();

		// Returns a client without SSL.
		Retrofit restAdapter = createRetrofitClient(url, newBuilder, useJacksonConverter);

		LOGGER.debug("A non SSL RestClient has been served.");
		return restAdapter;
	}

	private Retrofit createRetrofitClient(String url, Builder newBuilder, boolean useJacksonConverter) {
		HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
		logging.setLevel(Level.NONE);
		newBuilder.addInterceptor(logging);
		newBuilder.connectTimeout(0, TimeUnit.SECONDS);
		newBuilder.readTimeout(0, TimeUnit.SECONDS);
		newBuilder.writeTimeout(0, TimeUnit.SECONDS);
		String validUrl = url;
		String retrofit2CompatibleUrlEnding = "/";
		if (!url.endsWith(retrofit2CompatibleUrlEnding)) {
			validUrl = url.concat(retrofit2CompatibleUrlEnding);
		}
		Factory factory;
		if (useJacksonConverter) {
			factory = JacksonConverterFactory.create(DomainObjectMapper.getNewInstance());
		} else {
			factory = GsonConverterFactory.create();
		}

		return new Retrofit.Builder().addCallAdapterFactory(RxJavaCallAdapterFactory.create()).addConverterFactory(factory).baseUrl(validUrl)
				.client(newBuilder.build()).build();
	}

}
