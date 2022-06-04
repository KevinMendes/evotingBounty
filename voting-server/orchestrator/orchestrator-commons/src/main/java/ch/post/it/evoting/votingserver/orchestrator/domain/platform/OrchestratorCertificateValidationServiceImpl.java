/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.domain.platform;

import javax.ejb.Stateless;

import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateValidationServiceImpl;

@Stateless
@OrchestratorCertificateValidationService
public class OrchestratorCertificateValidationServiceImpl extends CertificateValidationServiceImpl {

}
