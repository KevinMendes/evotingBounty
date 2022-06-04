/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.crypto;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;

/**
 * Repository for password strings.
 */
public class PasswordRepository {

	/**
	 * Find a password string identified by this tenant id, election event id and alias
	 *
	 * @param tenant the tenant identifier.
	 * @param eeid   the election event identifier
	 * @param alias  the password alias
	 * @return the password as string
	 * @throws ResourceNotFoundException if the password for this tenant id, election event id and alias can not be found.
	 */
	public String getByTenantEEIDAlias(String tenant, String eeid, String alias) throws ResourceNotFoundException {
		// This implementation ignores tenant and eeid for key identification. Implement this feature
		// when required.
		String password = PropertiesFileReader.getInstance().getPropertyValue(alias + ".pwd");
		if (password == null) {
			throw new ResourceNotFoundException("Can not find password for tenant " + tenant + " election event id " + eeid + " and alias " + alias);
		}
		return password;
	}
}
