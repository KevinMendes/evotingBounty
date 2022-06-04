/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.sign;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import ch.post.it.evoting.votingserver.commons.sign.beans.SignatureFieldsConstants;

public class SignatureMetadata {

	private static final String FIELD = "field";

	private static final String VALUE = "value";

	private final String version;

	private final String signature;

	private final Map<String, String> signedFields;

	private SignatureMetadata(final String version, final Map<String, String> signedFields, final String signature) {
		this.version = version;
		this.signedFields = signedFields;
		this.signature = signature;
	}

	public static SignatureMetadata fromJsonObject(final JsonObject jsonObject) {

		final String version = jsonObject.getString(SignatureFieldsConstants.SIG_FIELD_VERSION);
		final String signature = jsonObject.getString(SignatureFieldsConstants.SIG_FIELD_SIGNATURE);

		final Map<String, String> signed = new LinkedHashMap<>();
		jsonObject.getJsonArray(SignatureFieldsConstants.SIG_FIELD_SIGNED).getValuesAs(JsonObject.class)
				.forEach(obj -> signed.put(obj.getString(FIELD), obj.getString(VALUE)));

		return new SignatureMetadata(version, signed, signature);
	}

	public static SignatureMetadataBuilder builder() {
		return new SignatureMetadataBuilder();
	}

	public JsonObject toJsonObject() {
		JsonObjectBuilder mainObjectBuilder = Json.createObjectBuilder();
		mainObjectBuilder.add(SignatureFieldsConstants.SIG_FIELD_VERSION, version);

		final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		signedFields.forEach((k, v) -> arrayBuilder.add(Json.createObjectBuilder().add(FIELD, k).add(VALUE, v)));
		mainObjectBuilder.add(SignatureFieldsConstants.SIG_FIELD_SIGNED, arrayBuilder);
		mainObjectBuilder.add(SignatureFieldsConstants.SIG_FIELD_SIGNATURE, signature);

		return mainObjectBuilder.build();
	}

	public String getVersion() {
		return version;
	}

	public String getSignature() {
		return signature;
	}

	public Map<String, String> getSignedFields() {
		return Collections.unmodifiableMap(signedFields);
	}

	public static class SignatureMetadataBuilder {

		private final String version;

		private final Map<String, String> signedFields;

		private String signature;

		public SignatureMetadataBuilder() {
			version = "1.0";
			signedFields = new LinkedHashMap<>();
		}

		public SignatureMetadataBuilder withSignature(final String signature) {
			this.signature = signature;
			return this;
		}

		public SignatureMetadataBuilder addSignedField(final String name, final String value) {
			Objects.requireNonNull(name, "field name cannot be null");
			Objects.requireNonNull(value, "field value cannot be null");
			signedFields.put(name, value);
			return this;
		}

		public SignatureMetadata build() {
			Objects.requireNonNull(signature, "field 'signature' cannot be null");
			return new SignatureMetadata(version, signedFields, signature);
		}

	}
}
