/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import javax.ejb.Stateless;

import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateServiceImpl;

/**
 * Remote Service for certificates.
 */
@Stateless(name = "vwRemoteCertificateRepository")
public class VwRemoteCertificateService extends RemoteCertificateServiceImpl implements RemoteCertificateService {
}
