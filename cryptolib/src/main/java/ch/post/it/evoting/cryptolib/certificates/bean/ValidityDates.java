/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.constants.X509CertificateConstants;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;

/**
 * Container class for dates of validity.
 */
public class ValidityDates {

	private static final String STARTING_DATE_OF_VALIDITY_LABEL = "Starting date of validity";
	private final Date startDate;
	private final Date endDate;

	/**
	 * Constructor which takes the starting and ending dates of validity as input. Both of these dates are included in the period of validity.
	 *
	 * @param startDate the starting date of validity, inclusive.
	 * @param endDate   the ending date of validity, inclusive.
	 * @throws GeneralCryptoLibException if the dates are invalid.
	 */
	public ValidityDates(final Date startDate, final Date endDate) throws GeneralCryptoLibException {

		validateInput(startDate, endDate);

		this.startDate = new Date(startDate.getTime());
		this.endDate = new Date(endDate.getTime());
	}

	private static void validateInput(final Date startDate, final Date endDate) throws GeneralCryptoLibException {

		Validate.notNull(startDate, STARTING_DATE_OF_VALIDITY_LABEL);
		Validate.notNull(endDate, "Ending date of validity");
		Validate.isBefore(startDate, endDate, STARTING_DATE_OF_VALIDITY_LABEL, "ending date of validity");

		final SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss", Locale.ENGLISH);
		final String maxDateAsString = "31-12-" + X509CertificateConstants.X509_CERTIFICATE_MAXIMUM_VALIDITY_DATE_YEAR + " 23:59:59";
		final Date maxDate;
		try {
			maxDate = formatter.parse(maxDateAsString);
		} catch (final ParseException e) {
			throw new GeneralCryptoLibException("Could not convert to Date object from String format", e);
		}

		Validate.isBefore(startDate, maxDate, STARTING_DATE_OF_VALIDITY_LABEL, "maximum date of validity");
		Validate.isBefore(endDate, maxDate, "Ending date of validity", "maximum date of validity");
	}

	/**
	 * Retrieves the starting date of validity.
	 *
	 * @return the starting date of validity.
	 */
	public Date getNotBefore() {

		return new Date(startDate.getTime());
	}

	/**
	 * Retrieves the ending date of validity.
	 *
	 * @return the ending date of validity.
	 */
	public Date getNotAfter() {

		return new Date(endDate.getTime());
	}
}
