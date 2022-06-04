/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.sign;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.securedatamanager.services.application.service.AliasSignatureConstants;

@DisplayName("A FileSignature")
class FileSignatureTest {

	private static String json;
	private static ObjectMapper mapper;
	private static FileSignature fileSignature;

	@BeforeAll
	static void setUpAll() {
		mapper = DomainObjectMapper.getNewInstance();

		final Map<String, String> fieldsToSign = new LinkedHashMap<>();
		fieldsToSign.put(AliasSignatureConstants.TIMESTAMP, DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(1550000001)));
		fieldsToSign.put(AliasSignatureConstants.COMPONENT, "Signature Metadata");

		fileSignature = new FileSignature("1.0", fieldsToSign, "Signature".getBytes(StandardCharsets.UTF_8));
		json = "{\"version\":\"1.0\",\"signed\":{\"timestamp\":\"2019-02-12T19:33:21Z\",\"component\":\"Signature Metadata\"},\"signature\":\"U2lnbmF0dXJl\"}";
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializeSignatureMetadata() throws JsonProcessingException {
		final String serializedSignatureMetadata = mapper.writeValueAsString(fileSignature);

		assertEquals(json, serializedSignatureMetadata);
	}

	@Test
	@DisplayName("deserialized gives expected FileSignature")
	void deserializeSignatureMetadata() throws IOException {
		final FileSignature deserializedFileSignature = mapper.readValue(json, FileSignature.class);

		assertEquals(fileSignature, deserializedFileSignature);
	}

	@Test
	@DisplayName("serialized then deserialized gives original FileSignature")
	void cycle() throws IOException {
		final FileSignature result = mapper.readValue(mapper.writeValueAsString(fileSignature), FileSignature.class);

		assertEquals(fileSignature, result);
	}

}
