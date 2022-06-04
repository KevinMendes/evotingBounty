/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.domain.model.platform;

import javax.ejb.Stateless;

import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateValidationServiceImpl;

@Stateless
@VmCertificateValidationService
public class VmCertificateValidationServiceImpl extends CertificateValidationServiceImpl {

}
