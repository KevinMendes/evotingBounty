/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.helpers;

import static ch.post.it.evoting.domain.election.helpers.CertificatePropertiesConstants.ISSUER_COMMON_NAME_PROPERTY_NAME;
import static ch.post.it.evoting.domain.election.helpers.CertificatePropertiesConstants.ISSUER_COUNTRY_PROPERTY_NAME;
import static ch.post.it.evoting.domain.election.helpers.CertificatePropertiesConstants.ISSUER_ORGANIZATIONAL_UNIT_PROPERTY_NAME;
import static ch.post.it.evoting.domain.election.helpers.CertificatePropertiesConstants.ISSUER_ORGANIZATION_PROPERTY_NAME;
import static ch.post.it.evoting.domain.election.helpers.CertificatePropertiesConstants.SUBJECT_COMMON_NAME_PROPERTY_NAME;
import static ch.post.it.evoting.domain.election.helpers.CertificatePropertiesConstants.SUBJECT_COUNTRY_PROPERTY_NAME;
import static ch.post.it.evoting.domain.election.helpers.CertificatePropertiesConstants.SUBJECT_ORGANIZATIONAL_UNIT_PROPERTY_NAME;
import static ch.post.it.evoting.domain.election.helpers.CertificatePropertiesConstants.SUBJECT_ORGANIZATION_PROPERTY_NAME;

import java.util.Properties;

import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;

/**
 * Retrieves certificate properties from a given properties input.
 */
public class CertificateParametersLoader {

	private final ReplacementsHolder replacements;

	public CertificateParametersLoader(final ReplacementsHolder replacements) {
		this.replacements = replacements;
	}

	public CertificateParameters load(final Properties properties, final CertificateParameters.Type type) {

		final CertificateParameters certificateParameters = new CertificateParameters();

		certificateParameters.setType(type);
		certificateParameters.setUserSubjectCn(getProperty(properties, SUBJECT_COMMON_NAME_PROPERTY_NAME));
		certificateParameters.setUserSubjectOrgUnit(getProperty(properties, SUBJECT_ORGANIZATIONAL_UNIT_PROPERTY_NAME));
		certificateParameters.setUserSubjectOrg(getProperty(properties, SUBJECT_ORGANIZATION_PROPERTY_NAME));
		certificateParameters.setUserSubjectCountry(getProperty(properties, SUBJECT_COUNTRY_PROPERTY_NAME));
		if (certificateParameters.getType() != CertificateParameters.Type.ROOT) {
			certificateParameters.setUserIssuerCn(getProperty(properties, ISSUER_COMMON_NAME_PROPERTY_NAME));
			certificateParameters.setUserIssuerOrgUnit(getProperty(properties, ISSUER_ORGANIZATIONAL_UNIT_PROPERTY_NAME));
			certificateParameters.setUserIssuerOrg(getProperty(properties, ISSUER_ORGANIZATION_PROPERTY_NAME));
			certificateParameters.setUserIssuerCountry(getProperty(properties, ISSUER_COUNTRY_PROPERTY_NAME));
		}
		return certificateParameters;
	}

	private String getProperty(final Properties properties, final String name) {
		final String value = properties.getProperty(name);
		return replacements.applyReplacements(value);
	}
}
