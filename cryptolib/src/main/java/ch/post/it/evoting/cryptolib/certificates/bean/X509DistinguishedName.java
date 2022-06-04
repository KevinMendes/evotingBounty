/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.constants.X509CertificateConstants;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;

/**
 * Container class for the attributes of a distinguished name. The attributes are added to the distinguished name by using an internally defined
 * builder class.
 */
public final class X509DistinguishedName {

	private final String commonName;

	private final String organizationalUnit;

	private final String organization;

	private final String locality;

	private final String country;

	/**
	 * Creates a new object using the attributes provided by {@code builder}.
	 *
	 * @param builder distinguished name builder containing all attributes to be included in distinguished name.
	 * @throws GeneralCryptoLibException if the attributes are invalid.
	 */
	private X509DistinguishedName(final Builder builder) {

		commonName = builder.commonName;
		organizationalUnit = builder.organizationalUnit;
		organization = builder.organization;
		locality = builder.locality;
		country = builder.country;
	}

	public String getCommonName() {

		return commonName;
	}

	public String getOrganizationalUnit() {

		return organizationalUnit;
	}

	public String getOrganization() {

		return organization;
	}

	public String getLocality() {

		return locality;
	}

	public String getCountry() {

		return country;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((commonName == null) ? 0 : commonName.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((locality == null) ? 0 : locality.hashCode());
		result = prime * result + ((organization == null) ? 0 : organization.hashCode());
		result = prime * result + ((organizationalUnit == null) ? 0 : organizationalUnit.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final X509DistinguishedName other = (X509DistinguishedName) obj;
		if (commonName == null) {
			if (other.commonName != null) {
				return false;
			}
		} else if (!commonName.equals(other.commonName)) {
			return false;
		}
		if (country == null) {
			if (other.country != null) {
				return false;
			}
		} else if (!country.equals(other.country)) {
			return false;
		}
		if (locality == null) {
			if (other.locality != null) {
				return false;
			}
		} else if (!locality.equals(other.locality)) {
			return false;
		}
		if (organization == null) {
			if (other.organization != null) {
				return false;
			}
		} else if (!organization.equals(other.organization)) {
			return false;
		}
		if (organizationalUnit == null) {
			return other.organizationalUnit == null;
		} else {
			return organizationalUnit.equals(other.organizationalUnit);
		}
	}

	/**
	 * Builder class for the distinguished name.
	 */
	public static class Builder {

		// Required attributes.
		private final String commonName;

		private final String country;

		// Optional attributes.
		private String organizationalUnit = "";

		private String organization = "";

		private String locality = "";

		/**
		 * Creates a new object using the arguments provided.
		 *
		 * @param commonName common name to add to builder.
		 * @param country    country to add to builder.
		 * @throws GeneralCryptoLibException if common name or country argument is null or blank or if it exceeds the maximum required length.
		 */
		public Builder(final String commonName, final String country) throws GeneralCryptoLibException {

			Validate.notNullOrBlank(commonName, "Common name");
			Validate.notGreaterThan(commonName.length(), X509CertificateConstants.X509_DISTINGUISHED_NAME_ATTRIBUTE_MAX_SIZE, "Common name length",
					"");
			Validate.isAsciiPrintable(commonName, "Common name");
			Validate.notNullOrBlank(country, "Country");
			Validate.notGreaterThan(country.length(), 2, "Country length", "");
			Validate.onlyContains(country, SecureRandomConstants.ALPHABET_BASE64, "Country");

			this.commonName = commonName;
			this.country = country;
		}

		/**
		 * Adds an organizational unit to the builder.
		 *
		 * @param organizationalUnit organizational unit to builder.
		 * @return updated builder.
		 * @throws GeneralCryptoLibException if the organizational unit argument is null or blank or if it exceeds the maximum required length.
		 */
		public Builder addOrganizationalUnit(final String organizationalUnit) throws GeneralCryptoLibException {

			Validate.notNull(organizationalUnit, "Organizational unit");
			Validate.notGreaterThan(organizationalUnit.length(), X509CertificateConstants.X509_DISTINGUISHED_NAME_ATTRIBUTE_MAX_SIZE,
					"Organizational unit length", "");
			Validate.isAsciiPrintable(organizationalUnit, "Organizational unit");

			this.organizationalUnit = organizationalUnit;

			return this;
		}

		/**
		 * Adds an organization to the builder.
		 *
		 * @param organization organization to add to builder.
		 * @return updated builder.
		 * @throws GeneralCryptoLibException if the organization argument is null or blank or if it exceeds the maximum required length.
		 */
		public Builder addOrganization(final String organization) throws GeneralCryptoLibException {

			Validate.notNull(organization, "Organization");
			Validate.notGreaterThan(organization.length(), X509CertificateConstants.X509_DISTINGUISHED_NAME_ATTRIBUTE_MAX_SIZE, "Organization length",
					"");
			Validate.isAsciiPrintable(organization, "Organization");

			this.organization = organization;

			return this;
		}

		/**
		 * Adds a locality to the builder.
		 *
		 * @param locality locality to add to builder.
		 * @return updated builder.
		 * @throws GeneralCryptoLibException if the locality argument is null or blank or if it exceeds the maximum required length.
		 */
		public Builder addLocality(final String locality) throws GeneralCryptoLibException {

			Validate.notNull(locality, "Locality");
			Validate.notGreaterThan(locality.length(), X509CertificateConstants.X509_DISTINGUISHED_NAME_ATTRIBUTE_MAX_SIZE, "Locality length", "");
			Validate.isAsciiPrintable(locality, "Locality");

			this.locality = locality;

			return this;
		}

		/**
		 * Builds the distinguished name based on the attributes that have been added to the builder.
		 *
		 * @return distinguished name with added attributes.
		 * @throws GeneralCryptoLibException if the distinguished name build process fails.
		 */
		public X509DistinguishedName build() throws GeneralCryptoLibException {

			return new X509DistinguishedName(this);
		}
	}
}
