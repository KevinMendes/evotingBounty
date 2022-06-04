/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.sign;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "version", "signed", "signature" })
public class FileSignature {

	private static final String BASE_VERSION = "1.0";

	@JsonProperty("version")
	private final String version;

	@JsonProperty("signed")
	private final Map<String, String> signed;

	@JsonProperty("signature")
	private final byte[] signature;

	@JsonCreator
	public FileSignature(
			@JsonProperty(value = "version", required = true)
			final String version,
			@JsonProperty(value = "signed", required = true)
			final Map<String, String> signedFields,
			@JsonProperty(value = "signature", required = true)
			final byte[] signature) {

		checkNotNull(version);
		checkNotNull(signedFields);
		checkNotNull(signature);

		this.version = version;
		this.signed = signedFields;
		this.signature = signature;
	}

	public FileSignature(final Map<String, String> signedFields, final byte[] signature) {

		checkNotNull(signedFields);
		checkNotNull(signature);

		this.version = BASE_VERSION;
		this.signed = signedFields;
		this.signature = signature;
	}

	public String getVersion() {
		return version;
	}

	public byte[] getSignature() {
		return signature;
	}

	public Map<String, String> getSigned() {
		return Collections.unmodifiableMap(signed);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final FileSignature that = (FileSignature) o;
		return version.equals(that.version) && signed.equals(that.signed) && Arrays.equals(signature, that.signature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(version, signed, signature);
	}
}
