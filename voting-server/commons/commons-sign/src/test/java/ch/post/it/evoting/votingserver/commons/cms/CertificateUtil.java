/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cms;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Date;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.RootCertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.ValidityDates;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesService;

public class CertificateUtil {

	public static KeyStore createTestP12(String filename, char[] password, String alias)
			throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, GeneralCryptoLibException {

		KeyStore store = KeyStore.getInstance("PKCS12");
		store.load(null, password);
		store.store(new FileOutputStream("target/" + filename), password);
		store.load(new FileInputStream("target/" + filename), password);
		AsymmetricServiceAPI asymmetricService = new AsymmetricService();
		CertificatesServiceAPI certificateGenerator = new CertificatesService();

		KeyPair keyPairForParentCert = asymmetricService.getKeyPairForSigning();
		KeyPair keyPairForSigning = asymmetricService.getKeyPairForSigning();
		RootCertificateData rootCertificateData = new RootCertificateData();
		X509DistinguishedName self = new X509DistinguishedName.Builder("test", "ES").addOrganization("test").addOrganizationalUnit("test")
				.addLocality("test").build();
		rootCertificateData.setSubjectDn(self);
		rootCertificateData.setSubjectPublicKey(keyPairForParentCert.getPublic());
		Date now = Calendar.getInstance().getTime();
		Calendar future = Calendar.getInstance();
		future.add(Calendar.YEAR, 1);
		rootCertificateData.setValidityDates(new ValidityDates(now, future.getTime()));
		CryptoAPIX509Certificate createRootAuthorityX509Certificate = certificateGenerator
				.createRootAuthorityX509Certificate(rootCertificateData, keyPairForParentCert.getPrivate());
		CertificateData certificateData = new CertificateData();
		certificateData.setIssuerDn(self);
		certificateData.setSubjectDn(
				new X509DistinguishedName.Builder("test2", "ES").addOrganization("test").addOrganizationalUnit("test").addLocality("test").build());
		certificateData.setSubjectPublicKey(keyPairForSigning.getPublic());
		certificateData.setValidityDates(new ValidityDates(now, future.getTime()));
		CryptoAPIX509Certificate createSignerX509Certificate = certificateGenerator
				.createSignX509Certificate(certificateData, keyPairForSigning.getPrivate());
		store.setCertificateEntry("test", createRootAuthorityX509Certificate.getCertificate());
		Certificate[] chain = new Certificate[1];
		chain[0] = createSignerX509Certificate.getCertificate();
		store.setKeyEntry(alias, keyPairForSigning.getPrivate(), password, chain);
		store.store(new FileOutputStream("target/" + filename), password);
		store.load(new FileInputStream("target/" + filename), password);
		return store;
	}
}
