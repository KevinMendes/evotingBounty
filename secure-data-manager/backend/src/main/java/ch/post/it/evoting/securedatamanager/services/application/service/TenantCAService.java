/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;

@Service
public class TenantCAService extends FileRootCAService {

	public TenantCAService(final PathResolver pathResolver,
			@Value("${tenantID}")
			final
			String tenantId) {
		super(pathResolver, String.format(Constants.CONFIG_FILE_NAME_TENANT_CA_PATTERN, tenantId));
	}
}
