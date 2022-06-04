/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.certificate;

import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.ejb.Stateless;

@Stateless
public class X509CertificateService {

	private final CertificateFactory factory;

	public X509CertificateService() throws CertificateException {
		factory = CertificateFactory.getInstance("X.509");
	}

	public X509Certificate generateCertificate(final InputStream inputStream) throws CertificateException {
		return (X509Certificate) factory.generateCertificate(inputStream);
	}
}
