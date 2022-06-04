/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.utils;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;

/**
 * Helper class for using Lightweight Directory Access Protocol (LDAP).
 */
public class LdapHelper {

	/**
	 * Quote character.
	 */
	private static final String QUOTE = "'";

	/**
	 * An empty {@link String}.
	 */
	private static final String EMPTY_STRING = "";

	private static final String CREATE_LDAP_NAME_OBJECT_FROM_DN_ERROR_MESSAGE = "Could not create LDAP name object from distinguished name ";

	/**
	 * Retrieves an attribute from a distinguished name.
	 *
	 * @param distinguishedName distinguished name.
	 * @param attributeType     type of attribute to retrieve from distinguished name.
	 * @return attribute from distinguished name.
	 */
	public String getAttributeFromDistinguishedName(final String distinguishedName, final String attributeType) {

		// Create LDAP name object from distinguished name.
		final LdapName ldapName;
		try {
			ldapName = new LdapName(distinguishedName);
		} catch (final InvalidNameException e) {
			throw new CryptoLibException(CREATE_LDAP_NAME_OBJECT_FROM_DN_ERROR_MESSAGE + QUOTE + distinguishedName + QUOTE, e);
		}

		// Retrieve attribute from LDAP name object.
		for (final Rdn relativeDn : ldapName.getRdns()) {
			if (relativeDn.getType().equalsIgnoreCase(attributeType)) {
				return (String) relativeDn.getValue();
			}
		}

		return EMPTY_STRING;
	}
}
