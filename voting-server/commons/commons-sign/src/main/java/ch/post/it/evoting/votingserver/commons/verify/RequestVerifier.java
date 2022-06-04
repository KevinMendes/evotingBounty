/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.verify;

import java.security.PublicKey;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;
import ch.post.it.evoting.votingserver.commons.sign.beans.SignedRequestContent;

public class RequestVerifier {

	public boolean verifySignature(final SignedRequestContent signedRequestContent, final byte[] signatureBytes, final PublicKey publicKey)
			throws GeneralCryptoLibException {

		if (signatureBytes == null) {
			return false;
		}

		if (publicKey == null) {
			return false;
		}

		final AsymmetricService asymmetricService = new AsymmetricService();

		final byte[] objectBytes = signedRequestContent.getBytes();

		final PrimitivesServiceAPI primitivesService = new PrimitivesService();
		final byte[] objectHash = primitivesService.getHash(objectBytes);

		return asymmetricService.verifySignature(signatureBytes, publicKey, objectHash);

	}

}
