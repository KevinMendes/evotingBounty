/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static ch.post.it.evoting.cryptolib.certificates.utils.CertificateChainValidator.isCertificateChainValid;
import static com.google.common.base.Preconditions.checkNotNull;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.SignedPayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.domain.election.model.messaging.PayloadSignatureException;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;


@Service
public class CryptolibPayloadSignatureService {

	private final AsymmetricServiceAPI asymmetricService;
	private final HashService hashService;

	@Autowired
	public CryptolibPayloadSignatureService(final AsymmetricServiceAPI asymmetricService,final HashService hashService) {
		this.asymmetricService = checkNotNull(asymmetricService);
		this.hashService = checkNotNull(hashService);
	}

	public <P extends SignedPayload> P sign(final P payload, final PrivateKey signingKey, final X509Certificate[] certificateChain)
			throws PayloadSignatureException {

		checkNotNull(payload);
		checkNotNull(signingKey);
		checkNotNull(certificateChain);

		final byte[] signature;
		try {
			final byte[] payloadHash = hashService.recursiveHash(payload);
			signature = asymmetricService.sign(signingKey, payloadHash);
		} catch (final GeneralCryptoLibException e) {
			throw new PayloadSignatureException("Failed to sign payload hash: " + e.getMessage());
		}

		payload.setSignature(new CryptoPrimitivesPayloadSignature(signature, certificateChain));

		return payload;
	}

	public boolean verify(final SignedPayload payload, final X509Certificate platformRootCA) throws PayloadVerificationException {

		checkNotNull(payload);
		checkNotNull(payload.getSignature());
		checkNotNull(platformRootCA);

		final X509Certificate[] certificateChain = payload.getSignature().getCertificateChain();

		try {
			// Verify certificate chain
			if (!isCertificateChainValid(certificateChain, platformRootCA)) {
				return false;
			}
			// Verify signature
			final PublicKey publicKey = certificateChain[0].getPublicKey();
			final byte[] payloadHash = hashService.recursiveHash(payload);
			return asymmetricService.verifySignature(payload.getSignature().getSignatureContents(), publicKey, payloadHash);
		} catch (final GeneralCryptoLibException e) {
			throw new PayloadVerificationException(e);
		}
	}
}
