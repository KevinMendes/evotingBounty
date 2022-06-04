/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class that contains the results of the validation of a {@link java.security.cert.X509Certificate}.
 */
public class X509CertificateValidationResult {

	private final boolean isValidated;

	private final Set<X509CertificateValidationType> failedValidationTypes;

	/**
	 * Create a validation result based on previous validation taken from arguments.
	 *
	 * @param isValidated           indicates whether {@link java.security.cert.X509Certificate} is valid.
	 * @param failedValidationTypes array of {@link X509CertificateValidationType} that failed.
	 */
	public X509CertificateValidationResult(final boolean isValidated, final X509CertificateValidationType... failedValidationTypes) {

		this.isValidated = isValidated;

		this.failedValidationTypes = new HashSet<>();
		this.failedValidationTypes.addAll(Arrays.asList(failedValidationTypes));
	}

	/**
	 * Checks if the {@link java.security.cert.X509Certificate} is valid.
	 *
	 * @return true if {@link java.security.cert.X509Certificate} was validated successfully and false otherwise.
	 */
	public boolean isValidated() {

		return isValidated;
	}

	/**
	 * Returns a list of {@link X509CertificateValidationType} that failed the {@link java.security.cert.X509Certificate} validation.
	 *
	 * @return a list of the failed {@link X509CertificateValidationType}.
	 */
	public List<X509CertificateValidationType> getFailedValidationTypes() {

		return new ArrayList<>(failedValidationTypes);
	}
}
