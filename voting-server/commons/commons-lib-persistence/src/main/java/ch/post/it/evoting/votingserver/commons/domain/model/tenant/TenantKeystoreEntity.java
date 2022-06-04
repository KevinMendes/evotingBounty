/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.domain.model.tenant;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * class for defining an entity representing a logging keystore
 */
@MappedSuperclass
public class TenantKeystoreEntity {

	/**
	 * The column length 100.
	 */
	public static final int COLUMN_LENGTH_100 = 100;

	/**
	 * the tenantId name
	 **/
	@Column(name = "TENANT_ID")
	@NotNull
	@Size(max = COLUMN_LENGTH_100)
	private String tenantId;

	/**
	 * The key type
	 */
	@Column(name = "KEY_TYPE")
	@NotNull
	@Size(max = COLUMN_LENGTH_100)
	private String keyType;

	/**
	 * the platform name
	 **/
	@Column(name = "PLATFORM_NAME")
	@Size(max = COLUMN_LENGTH_100)
	private String platformName;

	/**
	 * The content of the keystore
	 */
	@Column(name = "KEYSTORE_CONTENT", length = Integer.MAX_VALUE)
	@NotNull
	@Lob
	private String keystoreContent;

	/**
	 * Gets the tenant Id
	 *
	 * @return Value of tenantId
	 */
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * Sets new tenant identifier.
	 *
	 * @param tenantId New value of tenantId.
	 */
	public void setTenantId(final String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * Gets the platformName
	 *
	 * @return Value of platformName
	 */
	public String getPlatformName() {
		return platformName;
	}

	/**
	 * Sets new platformName.
	 *
	 * @param platformName New value of platform name.
	 */
	public void setPlatformName(final String platformName) {
		this.platformName = platformName;
	}

	/**
	 * Gets keyType.
	 *
	 * @return Value of keyType.
	 */
	public String getKeyType() {
		return keyType;
	}

	/**
	 * Sets new keyType.
	 *
	 * @param keyType New value of keyType.
	 */
	public void setKeyType(final String keyType) {
		this.keyType = keyType;
	}

	/**
	 * Gets keystoreContent.
	 *
	 * @return Value of keystoreContent.
	 */
	public String getKeystoreContent() {
		return keystoreContent;
	}

	/**
	 * Sets new keystoreContent.
	 *
	 * @param keystoreContent New value of keystoreContent.
	 */
	public void setKeystoreContent(final String keystoreContent) {
		this.keystoreContent = keystoreContent;
	}

}
