/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.remote;

import javax.ejb.Stateless;

import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateServiceImpl;

@Stateless(name = "vvRemoteCertificateService")
@VvRemoteCertificateService
public class VvRemoteCertificateServiceImpl extends RemoteCertificateServiceImpl implements RemoteCertificateService {
}
