/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.utils;

import java.io.StringReader;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;

public class JsonUtils {

	/**
	 * Non-public constructor
	 */
	private JsonUtils() {

	}

	/**
	 * Convert json string to json object.
	 *
	 * @param json - the json in string format.
	 * @return the JsonObject corresponding to json string.
	 */
	public static JsonObject getJsonObject(final String json) {
		final JsonReader jsonReader = Json.createReader(new StringReader(json));
		final JsonObject jsonObject = jsonReader.readObject();
		jsonReader.close();
		return jsonObject;
	}

	/**
	 * Convert json string to json object.
	 *
	 * @param json - the json in string format.
	 * @return the JsonObject corresponding to json string.
	 */
	public static JsonArray getJsonArray(final String json) {
		final JsonReader jsonReader = Json.createReader(new StringReader(json));
		final JsonArray jsonObject = jsonReader.readArray();
		jsonReader.close();
		return jsonObject;
	}

	/**
	 * Returns a JsonObjectBuilder with the properties of a given json object. It allows to add new properties to the original json object.
	 *
	 * @param jo - json object.
	 * @return - json object builder with the properties of the original object
	 */
	public static JsonObjectBuilder jsonObjectToBuilder(final JsonObject jo) {
		final JsonObjectBuilder job = Json.createObjectBuilder();
		for (final Map.Entry<String, JsonValue> entry : jo.entrySet()) {
			job.add(entry.getKey(), entry.getValue());
		}
		return job;
	}
}
