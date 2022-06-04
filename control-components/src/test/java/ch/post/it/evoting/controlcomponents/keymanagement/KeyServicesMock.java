/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.springframework.context.annotation.Configuration;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.ValidityDates;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesService;

/**
 * Configuration which publish mock implementation of {@link KeysManager} and {@link ElectionSigningKeysService}.
 */
@Configuration
public class KeyServicesMock {

	private final KeyPair KEY_PAIR = new AsymmetricService().getKeyPairForSigning();
	private X509Certificate certificate;

	public KeysManager keyManager() throws GeneralCryptoLibException {

		final KeysManager manager = mock(KeysManager.class);
		certificate = createTestCertificate().getCertificate();
		when(manager.getPlatformCACertificate()).thenReturn(certificate);
		return manager;
	}

	public ElectionSigningKeysService electionSigningKeysService() throws KeyManagementException {
		final ElectionSigningKeysService electionSigningKeysService = mock(ElectionSigningKeysService.class);
		final ElectionSigningKeys electionSigningKeys = new ElectionSigningKeys(KEY_PAIR.getPrivate(), new X509Certificate[] { certificate });
		when(electionSigningKeysService.getElectionSigningKeys(any())).thenReturn(electionSigningKeys);
		return electionSigningKeysService;
	}

	private CryptoAPIX509Certificate createTestCertificate() throws GeneralCryptoLibException {
		final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		final ZonedDateTime end = now.plusYears(1);
		final ValidityDates validityDates = new ValidityDates(Date.from(now.toInstant()), Date.from(end.toInstant()));

		final CertificateData certificateData = new CertificateData();
		certificateData.setSubjectPublicKey(KEY_PAIR.getPublic());
		certificateData.setValidityDates(validityDates);
		final X509DistinguishedName distinguishedName = new X509DistinguishedName.Builder("certId", "CH").build();
		certificateData.setSubjectDn(distinguishedName);
		certificateData.setIssuerDn(distinguishedName);

		return new CertificatesService().createSignX509Certificate(certificateData, KEY_PAIR.getPrivate());
	}
}
