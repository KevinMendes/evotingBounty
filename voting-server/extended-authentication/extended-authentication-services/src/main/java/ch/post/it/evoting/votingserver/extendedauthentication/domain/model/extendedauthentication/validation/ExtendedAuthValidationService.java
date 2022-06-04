/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.validation;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdate;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdateRequest;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ExtendedAuthValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * The Interface offering validation methods over Extended Authentication objects.
 */
public interface ExtendedAuthValidationService {

	/**
	 * This method validates an authentication token by applying the set of validation rules.
	 *
	 * @param tenantId            - the tenant identifier.
	 * @param electionEventId     - the election event identifier.
	 * @param authenticationToken - the token to be validated.
	 * @return true if the validation is correct and an exception on the contrary
	 * @throws AuthTokenValidationException - if the auth token is not properly validated
	 */
	boolean validateToken(String tenantId, String electionEventId, AuthenticationToken authenticationToken);

	/**
	 * Validates the certificate used for verifying the signature
	 *
	 * @param certificate         The certificate to be validated.
	 * @param authenticationToken The authentication Token.
	 * @return true if the validation is correct and an exception on the contrary
	 * @throws ExtendedAuthValidationException - if the validation is not correct
	 */
	boolean validateCertificate(String certificate, AuthenticationToken authenticationToken);

	/**
	 * Performs a verification of the signature, getting as a result the Extended Authentication
	 * update object if valid, and an Exception on the contrary
	 *
	 * @param extendedAuthenticationUpdateRequest The update request for Extended Authentication.
	 * @param authenticationToken                 The authentication Token.
	 * @return the ExtendedAuthenticationUpdate object contained in the signature
	 * @throws ExtendedAuthValidationException - if the validation is not correct
	 */
	ExtendedAuthenticationUpdate verifySignature(ExtendedAuthenticationUpdateRequest extendedAuthenticationUpdateRequest,
			AuthenticationToken authenticationToken);

	/**
	 * Validates that the provided auth token contains the same credential ID which was related to the
	 * old authId in the system.
	 *
	 * @param authenticationToken          The Authentication Token.
	 * @param extendedAuthenticationUpdate The Extended Authentication update object.
	 * @param tenantId                     The Tenant Id.
	 * @param electionEventId              The Election Event Id.
	 * @throws ResourceNotFoundException       , if the oldAuthID of the request does not exist in the
	 *                                         database
	 * @throws ApplicationException,           if an error occurs when trying to acquire the entry of the
	 *                                         database
	 * @throws ExtendedAuthValidationException - if the validation is not correct
	 */
	void validateTokenWithAuthIdAndCredentialId(AuthenticationToken authenticationToken, ExtendedAuthenticationUpdate extendedAuthenticationUpdate,
			String tenantId, String electionEventId) throws ResourceNotFoundException, ApplicationException;

	/**
	 * Validates that the certificate is issued by a trusted root against credentials certificate
	 * chain.
	 *
	 * @param tenantId            The Tenant Id.
	 * @param electionEventId     The Election Event Id.
	 * @param certificate         The Certificate to be validated.
	 * @param authenticationToken The Authentication Token.
	 * @throws ExtendedAuthValidationException
	 */
	void validateCertificateChain(String tenantId, String electionEventId, String certificate, AuthenticationToken authenticationToken);
}
