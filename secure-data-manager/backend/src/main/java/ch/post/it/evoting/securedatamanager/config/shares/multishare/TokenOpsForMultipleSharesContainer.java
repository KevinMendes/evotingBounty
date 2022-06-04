/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.multishare;

import java.security.PublicKey;
import java.util.Arrays;

import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SharesException;
import ch.post.it.evoting.securedatamanager.config.shares.token.TokenOps;

import iaik.pkcs.pkcs11.Session;
import iaik.pkcs.pkcs11.Token;
import iaik.pkcs.pkcs11.TokenException;

/**
 * This class extends TokenOps (from the Shares library). It was necessary to extend that class to allow for the reading of serialized
 * MultipleSharesContainer objects from smartcards. Note: an instance of TokenOps can write a serialized MultipleSharesContainer object to a
 * smartcard, but it is not able to read one again (because in the reading operation a instance of the new read object is created).
 * <p>
 * A refactor should be considered to create a single reading and writing mechanism that supports all of the required data.
 * <p>
 * Operations on a {@link Token}. This class deals with saving and recovering a share from a {@link Token}, as well as recovering the number of
 * retries until the PIN or PUK is locked.
 */
public class TokenOpsForMultipleSharesContainer extends TokenOps {

	/**
	 * Can fail if bad pin, share does not contain expected labels, wrong signature, general token failures.
	 */
	@Override
	public MultipleSharesContainer readShare(final Token token, final char[] pin, final PublicKey boardPublic)
			throws TokenException, SharesException {

		// get session to read data from token.
		Session sessionRO = token.openSession(Token.SessionType.SERIAL_SESSION, Token.SessionReadWriteBehavior.RO_SESSION, null, null);

		byte[] encryptedShare = recoverData(sessionRO, createSearchTemplate(ENCRYPTED_SHARE_LABEL.toCharArray(), Boolean.FALSE));
		byte[] encryptedShareSignature = recoverData(sessionRO, createSearchTemplate(SHARE_SIGNATURE_LABEL.toCharArray(), Boolean.FALSE));

		byte[] secretKeyBytes = null;
		EncryptedMultipleSharesContainer es = null;
		try {
			es = new EncryptedMultipleSharesContainer(encryptedShare, encryptedShareSignature, boardPublic);
			sessionRO.login(Session.UserType.USER, pin);
			secretKeyBytes = recoverData(sessionRO, createSearchTemplate(SECRET_KEY_SHARE_LABEL.toCharArray(), Boolean.TRUE));
			return es.decrypt(secretKeyBytes);
		} finally {
			if (es != null) {
				es.destroy();
			}
			if (secretKeyBytes != null) {
				Arrays.fill(secretKeyBytes, (byte) 0x00);
			}
			sessionRO.logout();
			sessionRO.closeSession();
		}
	}

}
