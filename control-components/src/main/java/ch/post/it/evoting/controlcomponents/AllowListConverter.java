/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import javax.persistence.AttributeConverter;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AllowListConverter implements AttributeConverter<List<String>, byte[]> {

	final ObjectMapper objectMapper;

	public AllowListConverter(final ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public byte[] convertToDatabaseColumn(final List<String> allowList) {
		checkNotNull(allowList);

		try {
			return objectMapper.writeValueAsBytes(allowList);
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public List<String> convertToEntityAttribute(final byte[] allowList) {
		checkNotNull(allowList);

		try {
			return Arrays.asList(objectMapper.readValue(new String(allowList, StandardCharsets.UTF_8), String[].class));
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}
}
