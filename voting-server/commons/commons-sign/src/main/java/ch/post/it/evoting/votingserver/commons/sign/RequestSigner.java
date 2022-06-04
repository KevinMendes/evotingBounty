/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.sign;

import java.security.PrivateKey;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;
import ch.post.it.evoting.votingserver.commons.exception.OvCommonsSignException;
import ch.post.it.evoting.votingserver.commons.sign.beans.SignedRequestContent;

public class RequestSigner {

	public byte[] sign(final SignedRequestContent signedRequestContent, final PrivateKey privateKey)
			throws GeneralCryptoLibException, OvCommonsSignException {

		if (signedRequestContent == null || privateKey == null) {
			throw new OvCommonsSignException("Nothing to sign or invalid key.");
		}

		AsymmetricService asymmetricService = new AsymmetricService();

		byte[] objectBytes = signedRequestContent.getBytes();

		PrimitivesServiceAPI primitivesService = new PrimitivesService();
		byte[] objectHash = primitivesService.getHash(objectBytes);

		return asymmetricService.sign(privateKey, objectHash);
	}

}
