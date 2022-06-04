/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.common;

import java.time.ZonedDateTime;
import java.util.Objects;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converts {@link ZonedDateTime} to {@link String} and back in support of JPA persistence. This is a portable conversion mechanism and makes no use
 * of vendor-specific <em>TimestampZ</em>-type types in the database. It has no reliance on a vendor-specific <cite>JDBC</cite> driver. This
 * implementation will not be suitable if you wish to make use of such types. Further, this implementation does not split out the
 * <code>TimeZone</code> information into a separate column.
 * <p>
 * The existence of this class in the classpath and it being known by the persistence unit is sufficient to allow you to use the as-of Java SE 8
 * {@link ZonedDateTime} class in an {@link javax.persistence.Entity} or in other persistable classes.
 * <p>
 * Important: the setting of <code>@Converter(autoApply = true)</code> in this class will make this conversion automatically effective for all
 * Entities that have one or more persistent {@link ZonedDateTime} properties.
 * <p>
 * The persistence provider must minimally support
 * <a href= "https://jcp.org/aboutJava/communityprocess/final/jsr338/index.html">JPA 2.1</a> for
 * this to work.
 */
@Converter(autoApply = true)
public class ZonedDateTimePersistenceConverter implements AttributeConverter<ZonedDateTime, String> {

	@Override
	public String convertToDatabaseColumn(final ZonedDateTime zonedDateTime) {
		return Objects.toString(zonedDateTime, null);
	}

	@Override
	public ZonedDateTime convertToEntityAttribute(final String dateTimeString) {
		return ZonedDateTime.parse(dateTimeString);
	}
}
