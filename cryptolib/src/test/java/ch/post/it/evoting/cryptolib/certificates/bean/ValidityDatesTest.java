/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.constants.X509CertificateConstants;

class ValidityDatesTest {

	@Test
	void whenSetValidityDatesThenOK() throws GeneralCryptoLibException {
		final Date notBefore = new Date(System.currentTimeMillis());
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1);
		final Date notAfter = calendar.getTime();

		final ValidityDates validityDates = new ValidityDates(notBefore, notAfter);

		final boolean startingDateRetrieved = validityDates.getNotBefore().equals(notBefore);
		final boolean endingDateRetrieved = validityDates.getNotAfter().equals(notAfter);
		final boolean startingDateBeforeEndingDate = notBefore.compareTo(notAfter) < 0;

		assertTrue(startingDateRetrieved && endingDateRetrieved && startingDateBeforeEndingDate);
	}

	@Test
	void whenSetMaximumAllowedStartingDateThenOK() throws GeneralCryptoLibException {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(X509CertificateConstants.X509_CERTIFICATE_MAXIMUM_VALIDITY_DATE_YEAR, Calendar.JANUARY, 0);
		final Date notBefore = calendar.getTime();
		calendar.set(X509CertificateConstants.X509_CERTIFICATE_MAXIMUM_VALIDITY_DATE_YEAR, Calendar.FEBRUARY, 0);
		final Date notAfter = calendar.getTime();

		final ValidityDates validityDates = new ValidityDates(notBefore, notAfter);

		final boolean startingDateRetrieved = validityDates.getNotBefore().equals(notBefore);
		final boolean endingDateRetrieved = validityDates.getNotAfter().equals(notAfter);
		final boolean startingDateBeforeEndingDate = notBefore.compareTo(notAfter) < 0;

		assertTrue(startingDateRetrieved && endingDateRetrieved && startingDateBeforeEndingDate);
	}

	@Test
	void whenSetMaximumAllowedEndingDateThenOK() throws GeneralCryptoLibException {
		final Date notBefore = new Date(System.currentTimeMillis());
		final Calendar calendar = Calendar.getInstance();
		calendar.set(X509CertificateConstants.X509_CERTIFICATE_MAXIMUM_VALIDITY_DATE_YEAR, Calendar.JANUARY, 1);
		final Date notAfter = calendar.getTime();

		final ValidityDates validityDates = new ValidityDates(notBefore, notAfter);

		final boolean startingDateRetrieved = validityDates.getNotBefore().equals(notBefore);
		final boolean endingDateRetrieved = validityDates.getNotAfter().equals(notAfter);
		final boolean startingDateBeforeEndingDate = notBefore.compareTo(notAfter) < 0;

		assertTrue(startingDateRetrieved && endingDateRetrieved && startingDateBeforeEndingDate);
	}

	@Test
	void givenNullStartingDateThenException() {
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1);
		final Date notAfter = calendar.getTime();

		assertThrows(GeneralCryptoLibException.class, () -> new ValidityDates(null, notAfter));
	}

	@Test
	void givenNullEndingDateThenException() {
		final Date notBefore = new Date(System.currentTimeMillis());

		assertThrows(GeneralCryptoLibException.class, () -> new ValidityDates(notBefore, null));
	}

	@Test
	void givenStartingDateAfterEndingDateThenException() {
		final Date notAfter = new Date(System.currentTimeMillis());
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1);
		final Date notBefore = calendar.getTime();

		assertThrows(GeneralCryptoLibException.class, () -> new ValidityDates(notBefore, notAfter));
	}

	@Test
	void givenStartingDateEqualsEndingDateThenException() {

		final Date notBefore = new Date(System.currentTimeMillis());
		final Date notAfter = new Date(notBefore.getTime());

		assertThrows(GeneralCryptoLibException.class, () -> new ValidityDates(notBefore, notAfter));
	}

	@Test
	void givenTooLargeStartingDateThenException() {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(X509CertificateConstants.X509_CERTIFICATE_MAXIMUM_VALIDITY_DATE_YEAR + 1, Calendar.JANUARY, 1);
		final Date notBefore = calendar.getTime();
		calendar.set(X509CertificateConstants.X509_CERTIFICATE_MAXIMUM_VALIDITY_DATE_YEAR + 1, Calendar.FEBRUARY, 0);
		final Date notAfter = calendar.getTime();

		assertThrows(GeneralCryptoLibException.class, () -> new ValidityDates(notBefore, notAfter));
	}

	@Test
	void givenTooLargeEndingDateThenException() {
		final Date notBefore = new Date(System.currentTimeMillis());
		final Calendar calendar = Calendar.getInstance();
		calendar.set(X509CertificateConstants.X509_CERTIFICATE_MAXIMUM_VALIDITY_DATE_YEAR + 1, Calendar.JANUARY, 1);
		final Date notAfter = calendar.getTime();

		assertThrows(GeneralCryptoLibException.class, () -> new ValidityDates(notBefore, notAfter));
	}
}
