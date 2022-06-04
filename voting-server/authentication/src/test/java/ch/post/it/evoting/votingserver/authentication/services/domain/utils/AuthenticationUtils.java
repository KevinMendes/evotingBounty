/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.commons.io.IOUtils;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationContent;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceStreamException;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;

public class AuthenticationUtils {

	public static final String ELECTION_EVENT_ID = "100";

	public static final String TENANT_ID = "100";

	private static final String CREDENTIALS_CA = "credentialsCA";

	private static final String ELECTION_ROOT_CA = "electionRootCA";

	// Create a credential factory to convert string to X509 certificate
	private static final CertificateFactory cf;

	static {

		try {
			cf = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			throw new RuntimeException("Unable to create certificate factory instance");
		}
	}

	public static AuthenticationContent generateAuthenticationContentEntity() {

		AuthenticationContent entity = new AuthenticationContent();
		entity.setElectionEventId(ELECTION_EVENT_ID);
		entity.setTenantId(TENANT_ID);
		final String json = readResource("authenticationContent.json");
		entity.setJson(json);
		return entity;

	}

	public static X509Certificate getX509Cert(String certificateString) throws CertificateException {
		InputStream inputStream = new ByteArrayInputStream(certificateString.getBytes(StandardCharsets.UTF_8));
		return (X509Certificate) cf.generateCertificate(inputStream);
	}

	public static X509DistinguishedName getDistinguishName(X509Certificate x509Cert) throws GeneralCryptoLibException {
		CryptoX509Certificate wrappedCertificate = new CryptoX509Certificate(x509Cert);
		return wrappedCertificate.getSubjectDn();
	}

	public static String getCredentialCA(String authCertsJson) {
		// Intermediate certificate -> Credentials CA
		return JsonUtils.getJsonObject(authCertsJson).getString(CREDENTIALS_CA);

	}

	public static String getRootCA(String authCertsJson) {
		// Intermediate certificate -> Credentials CA
		return JsonUtils.getJsonObject(authCertsJson).getString(ELECTION_ROOT_CA);

	}

	private static String readResource(final String resource) {
		try (final InputStream inputStream = AuthenticationUtils.class.getClassLoader().getResourceAsStream(resource)) {
			if (inputStream != null) {
				return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
			}
			throw new ResourceStreamException(
					String.format("Error trying to read the content of resource file %s. The inputStream is null.", resource));
		} catch (IOException e) {
			throw new ResourceStreamException(String.format("Error trying to read the content of resource file %s.", resource), e);
		}
	}

}
