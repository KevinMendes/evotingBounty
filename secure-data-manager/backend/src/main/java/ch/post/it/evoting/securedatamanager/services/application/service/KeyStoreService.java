/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import java.security.PrivateKey;

public interface KeyStoreService {

	PrivateKey getPrivateKey();
}
