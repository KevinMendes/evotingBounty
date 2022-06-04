/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.actions;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdate;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdateRequest;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.validation.ExtendedAuthValidationService;

/**
 * This class defines a DDD action to validate an Extended Authentication entity.
 */
@Stateless
public class ValidateExtendedAuthUpdateAction {

	@Inject
	private ExtendedAuthValidationService extendedAuthValidationService;

	/**
	 * Validates the information received prior to trying the update of an extended authentication entry. The validations affect the authentication
	 * token, the verification of signature of the request, and also the credential ID provided both in the token and certificate used for
	 * verification. It also verifies the certificate chain.
	 *
	 * @param tenantId                            Tenant ID.
	 * @param electionEventId                     Election Event ID.
	 * @param authenticationToken                 Authentication Token.
	 * @param extendedAuthenticationUpdateRequest Extended Authentication request for update.
	 * @return The ExtendedAuthenticationUpdate object.
	 * @throws ResourceNotFoundException
	 * @throws ApplicationException
	 */
	public ExtendedAuthenticationUpdate validate(final String tenantId, final String electionEventId, final AuthenticationToken authenticationToken,
			final ExtendedAuthenticationUpdateRequest extendedAuthenticationUpdateRequest) throws ResourceNotFoundException, ApplicationException {

		final ExtendedAuthenticationUpdate extendedAuthenticationUpdate = extendedAuthValidationService
				.verifySignature(extendedAuthenticationUpdateRequest, authenticationToken);

		extendedAuthValidationService.validateToken(tenantId, electionEventId, authenticationToken);

		extendedAuthValidationService
				.validateTokenWithAuthIdAndCredentialId(authenticationToken, extendedAuthenticationUpdate, tenantId, electionEventId);

		extendedAuthValidationService.validateCertificate(extendedAuthenticationUpdateRequest.getCertificate(), authenticationToken);

		extendedAuthValidationService
				.validateCertificateChain(tenantId, electionEventId, extendedAuthenticationUpdateRequest.getCertificate(), authenticationToken);

		return extendedAuthenticationUpdate;
	}
}
