/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.tenant;

public class TenantActivationData {

	private String tenantID;

	private String systemKeystorePassword;

	public String getTenantID() {
		return tenantID;
	}

	public void setTenantID(final String tenantID) {
		this.tenantID = tenantID;
	}

	public String getSystemKeystorePassword() {
		return systemKeystorePassword;
	}

	public void setSystemKeystorePassword(final String systemKeystorePassword) {
		this.systemKeystorePassword = systemKeystorePassword;
	}
}
