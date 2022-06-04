/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.remote;

import javax.ejb.Stateless;

import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateServiceImpl;

@Stateless(name = "auRemoteCertificateService")
@AuRemoteCertificateService
public class AuRemoteCertificateServiceImpl extends RemoteCertificateServiceImpl implements RemoteCertificateService {
}
