/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.infrastructure.remote;

import javax.ejb.Stateless;

import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateServiceImpl;

/**
 * Remote Service for handling certificates
 */
@Stateless(name = "vmRemoteCertificateService")
@VmRemoteCertificateService
public class VmRemoteCertificateServiceImpl extends RemoteCertificateServiceImpl implements RemoteCertificateService {

}
