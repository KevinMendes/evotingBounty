/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.remote.client;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.NodeIdentifier;
import ch.post.it.evoting.votingserver.commons.exception.OvCommonsSignException;
import ch.post.it.evoting.votingserver.commons.sign.RequestSigner;
import ch.post.it.evoting.votingserver.commons.sign.beans.SignedRequestContent;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

/**
 * Retrofit Interceptor class to include a Signature inside every request 'Signature' header.
 */
public class RestClientInterceptor implements Interceptor {

	public static final String HEADER_SIGNATURE = "Signature";
	public static final String HEADER_ORIGINATOR = "Originator";
	private static final Logger LOGGER = LoggerFactory.getLogger(RestClientInterceptor.class);
	private final PrivateKey privateKey;
	private final NodeIdentifier nodeIdentifier;

	public RestClientInterceptor(final PrivateKey privateKey, final NodeIdentifier nodeIdentifier) {
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
			LOGGER.error("GeneralCryptoLibException");
			throw new IOException(e);
		} catch (OvCommonsSignException e) {
			LOGGER.error("OvCommonsSignException");
			throw new IOException(e);
		}

		okhttp3.Response returnedResponse;

		Request.Builder requestBuilder = originalRequest.newBuilder().header(HEADER_SIGNATURE, signature)
				.header(HEADER_ORIGINATOR, nodeIdentifier.name());

		Request newRequest = requestBuilder.build();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("REQUEST HEADERS: {}", newRequest.headers());
		}

		returnedResponse = chain.proceed(newRequest);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("RESPONSE HEADERS: {}", returnedResponse.headers());
		}
		return returnedResponse;
	}

	private String getEncodedSignatureString(final Request request, final NodeIdentifier nodeIdentifier)
			throws IOException, GeneralCryptoLibException, OvCommonsSignException {
		RequestSigner requestSigner = new RequestSigner();

		String body = "";
		if (request.body() != null) {
			body = getRequestBodyToString(request.body());
		}

		SignedRequestContent signedRequestContent = new SignedRequestContent(request.url().toString(), request.method(), body, nodeIdentifier.name());

		byte[] signature = requestSigner.sign(signedRequestContent, privateKey);

		return Base64.getEncoder().encodeToString(signature);
	}

	private String getRequestBodyToString(final RequestBody request) {
		final RequestBody copy = request;
		try (final Buffer buffer = new Buffer()) {
			if (copy != null) {
				copy.writeTo(buffer);
			} else {
				return "";
			}
			return buffer.readUtf8();
		} catch (final IOException e) {
			final String errorMsg = "Exception occurred during process body to String";
			LOGGER.error(errorMsg, e);
			return "";
		}
	}

}
