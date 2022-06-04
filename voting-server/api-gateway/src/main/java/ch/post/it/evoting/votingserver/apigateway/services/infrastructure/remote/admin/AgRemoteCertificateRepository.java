/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin;

import javax.ejb.Stateless;

import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateServiceImpl;

@Stateless(name = "agRemoteCertificateService")
public class AgRemoteCertificateRepository extends RemoteCertificateServiceImpl implements RemoteCertificateService {
}
