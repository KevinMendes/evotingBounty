/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.certificates.csr;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CSRSigningInputProperties;
import ch.post.it.evoting.cryptolib.certificates.bean.CredentialProperties;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;

/**
 * Interface to sign certificate signing requests (CSR)
 */
public interface CertificateRequestSigner {

	CSRSigningInputProperties getCsrSigningInputProperties(final CredentialProperties credentialProperties) throws GeneralCryptoLibException;

	CryptoAPIX509Certificate signCSR(final X509Certificate issuerCA, final PrivateKey issuerPrivateKey, final JcaPKCS10CertificationRequest csr,
			final CSRSigningInputProperties csrSigningInputProperties) throws GeneralCryptoLibException;
}
