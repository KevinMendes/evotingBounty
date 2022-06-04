/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import java.io.IOException;
import java.io.UncheckedIOException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

@Converter
public class EncryptionGroupConverter implements AttributeConverter<GqGroup, byte[]> {

	private final ObjectMapper objectMapper;

	public EncryptionGroupConverter(final ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public byte[] convertToDatabaseColumn(final GqGroup encryptionGroup) {
		try {
			return objectMapper.writeValueAsBytes(encryptionGroup);
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException("Failed to serialize the encryption group.", e);
		}
	}

	@Override
	public GqGroup convertToEntityAttribute(final byte[] bytes) {
		try {
			return objectMapper.readValue(bytes, GqGroup.class);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to deserialize the encryption group.", e);
		}
	}
}
