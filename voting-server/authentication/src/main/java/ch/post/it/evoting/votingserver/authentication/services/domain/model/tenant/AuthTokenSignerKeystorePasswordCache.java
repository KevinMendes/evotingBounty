/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.tenant;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;

import ch.post.it.evoting.votingserver.commons.cache.Cache;
import ch.post.it.evoting.votingserver.commons.cache.KeyStorePasswordCache;

@Singleton
@Local(value = Cache.class)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class AuthTokenSignerKeystorePasswordCache extends KeyStorePasswordCache {

	public AuthTokenSignerKeystorePasswordCache() {
		super("AuthTokenSignerKeystorePassword");
	}

}
