/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;

@Service
public class PlatformRootCAService extends FileRootCAService {
	public PlatformRootCAService(final PathResolver pathResolver) {
		super(pathResolver, Constants.CONFIG_FILE_NAME_PLATFORM_ROOT_CA);
	}
}
