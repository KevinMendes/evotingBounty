/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.platform;

import javax.ejb.Stateless;

import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateValidationServiceImpl;

@Stateless
@CrCertificateValidationService
public class CrCertificateValidationServiceImpl extends CertificateValidationServiceImpl {

}
