/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class BooleanConverter implements AttributeConverter<Boolean, Character> {

	private static final char TRUE = 'Y';
	private static final char FALSE = 'N';

	@Override
	public Character convertToDatabaseColumn(final Boolean aBoolean) {
		if (aBoolean != null) {
			return aBoolean ? TRUE : FALSE;
		}
		return null;
	}

	@Override
	public Boolean convertToEntityAttribute(final Character character) {
		return character != null ? character.equals(TRUE) : null;
	}

}
