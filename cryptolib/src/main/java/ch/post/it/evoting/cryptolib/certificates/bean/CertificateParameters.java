/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.certificates.bean;

import java.time.ZonedDateTime;

public class CertificateParameters {
	private Type type;
	private String userSubjectCn;
	private String userSubjectOrgUnit;
	private String userSubjectOrg;
	private String userSubjectCountry;
	private X509DistinguishedName userSubjectDn;
	private String userIssuerCn;
	private String userIssuerOrgUnit;
	private String userIssuerOrg;
	private String userIssuerCountry;
	private ZonedDateTime userNotBefore;
	private ZonedDateTime userNotAfter;

	public Type getType() {
		return type;
	}

	public void setType(final Type type) {
		this.type = type;
	}

	public String getUserSubjectCn() {
		return userSubjectCn;
	}

	public void setUserSubjectCn(final String userSubjectCn) {
		this.userSubjectCn = userSubjectCn;
	}

	public String getUserSubjectOrgUnit() {
		return userSubjectOrgUnit;
	}

	public void setUserSubjectOrgUnit(final String userSubjectOrgUnit) {
		this.userSubjectOrgUnit = userSubjectOrgUnit;
	}

	public String getUserSubjectOrg() {
		return userSubjectOrg;
	}

	public void setUserSubjectOrg(final String userSubjectOrg) {
		this.userSubjectOrg = userSubjectOrg;
	}

	public String getUserSubjectCountry() {
		return userSubjectCountry;
	}

	public void setUserSubjectCountry(final String userSubjectCountry) {
		this.userSubjectCountry = userSubjectCountry;
	}

	public X509DistinguishedName getUserSubjectDn() {
		return userSubjectDn;
	}

	public void setUserSubjectDn(final X509DistinguishedName userSubjectDn) {
		this.userSubjectDn = userSubjectDn;
	}

	public String getUserIssuerCn() {
		return userIssuerCn;
	}

	public void setUserIssuerCn(final String userIssuerCn) {
		this.userIssuerCn = userIssuerCn;
	}

	public String getUserIssuerOrgUnit() {
		return userIssuerOrgUnit;
	}

	public void setUserIssuerOrgUnit(final String userIssuerOrgUnit) {
		this.userIssuerOrgUnit = userIssuerOrgUnit;
	}

	public String getUserIssuerOrg() {
		return userIssuerOrg;
	}

	public void setUserIssuerOrg(final String userIssuerOrg) {
		this.userIssuerOrg = userIssuerOrg;
	}

	public String getUserIssuerCountry() {
		return userIssuerCountry;
	}

	public void setUserIssuerCountry(final String userIssuerCountry) {
		this.userIssuerCountry = userIssuerCountry;
	}

	public ZonedDateTime getUserNotBefore() {
		return userNotBefore;
	}

	public void setUserNotBefore(final ZonedDateTime userNotBefore) {
		this.userNotBefore = userNotBefore;
	}

	public ZonedDateTime getUserNotAfter() {
		return userNotAfter;
	}

	public void setUserNotAfter(final ZonedDateTime userNotAfter) {
		this.userNotAfter = userNotAfter;
	}

	public enum Type {
		ROOT,
		INTERMEDIATE,
		SIGN,
		ENCRYPTION
	}
}
