/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;

@Converter
public class CombinedCorrectnessInformationConverter implements AttributeConverter<CombinedCorrectnessInformation, byte[]> {

	private final ObjectMapper objectMapper;

	public CombinedCorrectnessInformationConverter(final ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public byte[] convertToDatabaseColumn(final CombinedCorrectnessInformation combinedCorrectnessInformation) {
		checkNotNull(combinedCorrectnessInformation);

		try {
			return objectMapper.writeValueAsString(combinedCorrectnessInformation).getBytes(StandardCharsets.UTF_8);
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException("Could not map the provided combined correctness information into the dedicated database byte[] type.", e);
		}
	}

	@Override
	public CombinedCorrectnessInformation convertToEntityAttribute(final byte[] blob) {
		checkNotNull(blob);

		try {
			return objectMapper.readValue(new String(blob, StandardCharsets.UTF_8), CombinedCorrectnessInformation.class);
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException("Could not map the read byte[] from database into a combined correctness information.", e);
		}
	}

}
