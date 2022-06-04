/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.securedatamanager.config.engine.commands.ballotbox;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.KeyPair;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.ValidityDates;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesService;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans.ElectionCredentialDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.generators.ElectionCredentialDataPackGenerator;

@Configuration
public class BallotBoxGeneratorTestSpringConfig {

	@Bean
	public BallotBoxGenerator ballotBoxGenerator() {
		return new BallotBoxGenerator();
	}

	@Bean
	public ElectionCredentialDataPackGenerator electionCredentialDataPackGenerator() throws GeneralCryptoLibException {

		final ElectionCredentialDataPackGenerator electionCredentialDataPackGeneratorMock = mock(ElectionCredentialDataPackGenerator.class);
		final ElectionCredentialDataPack dataPackMock = mock(ElectionCredentialDataPack.class);
		final CryptoAPIExtendedKeyStore ballotBoxKeystore = mock(CryptoAPIExtendedKeyStore.class);

		when(ballotBoxKeystore.toJSON("keystorePassword".toCharArray())).thenReturn("mockedKeystoreString");

		final CryptoAPIX509Certificate certificate = createCryptoAPIX509CertificateTest();
		final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		final ZonedDateTime end = now.plusYears(1);

		when(dataPackMock.getCertificate()).thenReturn(certificate);
		when(dataPackMock.getStartDate()).thenReturn(now);
		when(dataPackMock.getEndDate()).thenReturn(end);
		when(dataPackMock.getPassword()).thenReturn("keystorePassword".toCharArray());
		when(dataPackMock.getEncryptedPassword()).thenReturn("encryptedKeystorePassword");
		when(dataPackMock.getKeyStore()).thenReturn(ballotBoxKeystore);

		when(electionCredentialDataPackGeneratorMock.generate(any(), any(), any(), any(), any(), any())).thenReturn(dataPackMock);

		return electionCredentialDataPackGeneratorMock;
	}

	private CryptoAPIX509Certificate createCryptoAPIX509CertificateTest() throws GeneralCryptoLibException {
		final KeyPair keyPair = new AsymmetricService().getKeyPairForSigning();

		final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		final ZonedDateTime end = now.plusYears(1);
		final ValidityDates validityDates = new ValidityDates(Date.from(now.toInstant()), Date.from(end.toInstant()));

		final CertificateData certificateData = new CertificateData();
		certificateData.setSubjectPublicKey(keyPair.getPublic());
		certificateData.setValidityDates(validityDates);

		final X509DistinguishedName distinguishedName = new X509DistinguishedName.Builder("commonName", "CH").build();
		certificateData.setSubjectDn(distinguishedName);
		certificateData.setIssuerDn(distinguishedName);

		return new CertificatesService().createSignX509Certificate(certificateData, keyPair.getPrivate());
	}
}
