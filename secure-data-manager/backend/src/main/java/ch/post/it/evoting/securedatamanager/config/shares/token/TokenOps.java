/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.securedatamanager.config.shares.token;

import java.security.PublicKey;
import java.util.Arrays;

import ch.post.it.evoting.cryptolib.api.secretsharing.Share;
import ch.post.it.evoting.securedatamanager.config.shares.shares.EncryptedShare;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SharesException;

import iaik.pkcs.pkcs11.Session;
import iaik.pkcs.pkcs11.Token;
import iaik.pkcs.pkcs11.TokenException;
import iaik.pkcs.pkcs11.TokenInfo;
import iaik.pkcs.pkcs11.objects.Data;

/**
 * Operations on a {@link Token}. This class deals with saving and recovering a share from a {@link Token}, as well as recovering the number of
 * retries until the PIN or PUK is locked.
 */
public class TokenOps {

	/**
	 * Default number of retries until a {@link Token} is locked.
	 */
	public static final int NUMBER_OF_RETRIES = 3;

	/**
	 * Label inside the {@link Token} where the encrypted {@link Share} signature is stored.
	 */
	public static final String SHARE_SIGNATURE_LABEL = "share_signature";

	/**
	 * Label inside the {@link Token} where the {@link Share} is stored in its encrypted form.
	 */
	public static final String ENCRYPTED_SHARE_LABEL = "share_public_section";

	/**
	 * Label inside the {@link Token} where the secret key encrypting the {@link Share} is stored.
	 */
	public static final String SECRET_KEY_SHARE_LABEL = "share_private_section";

	/**
	 * Creates a {@link Data} object to use as a template to search {@link Data} associated to a given label.
	 *
	 * @param label          The label to which the {@link Data} is associated.
	 * @param privateSection True if the object searched is in the private section of the token, false otherwise.
	 * @return A template {@link Data} object for the given label and privateness.
	 */
	protected static Data createSearchTemplate(final char[] label, final boolean privateSection) {
		// create certificate object template
		final Data dataObjectTemplate = new Data();

		// Set the name that manages this data object
		dataObjectTemplate.getApplication().setCharArrayValue("Shares API".toCharArray());

		// set the data object's label
		dataObjectTemplate.getLabel().setCharArrayValue(label);

		// set the data as private
		dataObjectTemplate.getPrivate().setBooleanValue(privateSection);

		// ensure that it is stored on the token and not just in this
		// session
		dataObjectTemplate.getToken().setBooleanValue(Boolean.TRUE);

		// prevent modification
		dataObjectTemplate.getModifiable().setBooleanValue(Boolean.FALSE);
		return dataObjectTemplate;
	}

	/**
	 * Create a full {@link Data} object to save into the token.
	 *
	 * @param data           The byte[] that the {@link Data} will contain.
	 * @param label          The label where to store the {@link Data}
	 * @param privateSection true if the {@link Data} is to be stored in the private token section, false otherwise.
	 */
	private static Data createDataObject(final byte[] data, final char[] label, final boolean privateSection) {
		final Data dataObject = createSearchTemplate(label, privateSection);

		// set the object's data content
		dataObject.getValue().setByteArrayValue(data);
		return dataObject;
	}

	/**
	 * Recover a single object (the first one) matching the template from the session. Extracts the byte[] content of that object to return it.
	 */
	protected static byte[] recoverData(final Session session, final Data templateData) throws TokenException {
		session.findObjectsInit(templateData);
		try {
			final Object[] objects = session.findObjects(1);
			return extractBytes(objects);
		} finally {
			session.findObjectsFinal();
		}
	}

	/**
	 * Extract the byte[] content of one {@link Data} object that is the first and only content of the given {@link Object} array.
	 */
	private static byte[] extractBytes(final Object[] objects) {
		if (objects == null || objects.length != 1) {
			throw new IllegalArgumentException("Expected 1 object, but found " + (objects == null ? 0 : objects.length));
		}
		final Object o = objects[0];
		if (o instanceof Data) {
			final Data data = (Data) o;
			return data.getValue().getByteArrayValue();
		} else {
			throw new IllegalArgumentException("Expected object of type Data, but found " + o.getClass());
		}
	}

	private static int getNumberOfRetriesUser(final TokenInfo tokenInfo) {
		int retriesLeft = 0;
		// 0 attempts left if pin locked
		if (tokenInfo.isUserPinLocked()) {
			return retriesLeft;
		}
		// 1 attempts left because the flag final try is active.
		if (tokenInfo.isUserPinFinalTry()) {
			retriesLeft = 1;
		} else {
			if (tokenInfo.isUserPinCountLow()) {
				// last attempt was unsuccessful and it is not the last try.
				// so, any number between 2 and the total number of retries
				// -1
				retriesLeft = NUMBER_OF_RETRIES - 1;
			} else {
				// no unsuccessful attempts, all retries are available
				retriesLeft = NUMBER_OF_RETRIES;
			}
		}
		return retriesLeft;
	}

	private static int getNumberOfRetriesSO(final TokenInfo tokenInfo) {
		int retriesLeft = 0;
		// 0 attempts left if pin locked
		if (tokenInfo.isSoPinLocked()) {
			return retriesLeft;
		}
		// 1 attempts left because the flag final try is active.
		if (tokenInfo.isSoPinFinalTry()) {
			retriesLeft = 1;
		} else {
			if (tokenInfo.isSoPinCountLow()) {
				// last attempt was unsuccessful and it is not the last try.
				// so, any number between 2 and the total number of retries
				// -1
				retriesLeft = NUMBER_OF_RETRIES - 1;
			} else {
				// no unsuccessful attempts, all retries are available
				retriesLeft = NUMBER_OF_RETRIES;
			}
		}
		return retriesLeft;
	}

	/**
	 * Read a share. Operation can fail if the PIN is wrong, the share does not contain the expected labels, a wrong signature, or a general token
	 * failure.
	 *
	 * @param token       the {@link Token} to read from.
	 * @param pin         the pin that protects the {@link Token} private part.
	 * @param boardPublic the {@link PublicKey} to validate the {@link Share} integrity.
	 * @return the {@link Share} read.
	 * @throws TokenException  if there is problems reading from the {@link Token}.
	 * @throws SharesException if there is problems with the {@link Share} information.
	 */
	public Share readShare(final Token token, final char[] pin, final PublicKey boardPublic) throws TokenException, SharesException {

		// get session to read data from token.
		final Session sessionRO = token.openSession(Token.SessionType.SERIAL_SESSION, Token.SessionReadWriteBehavior.RO_SESSION, null, null);

		final byte[] encryptedShare = recoverData(sessionRO, createSearchTemplate(ENCRYPTED_SHARE_LABEL.toCharArray(), Boolean.FALSE));
		final byte[] encryptedShareSignature = recoverData(sessionRO, createSearchTemplate(SHARE_SIGNATURE_LABEL.toCharArray(), Boolean.FALSE));

		final EncryptedShare es = new EncryptedShare(encryptedShare, encryptedShareSignature, boardPublic);
		byte[] secretKeyBytes = null;
		try {
			sessionRO.login(Session.UserType.USER, pin);
			secretKeyBytes = recoverData(sessionRO, createSearchTemplate(SECRET_KEY_SHARE_LABEL.toCharArray(), Boolean.TRUE));
			return es.decrypt(secretKeyBytes);
		} finally {
			es.destroy();
			if (secretKeyBytes != null) {
				Arrays.fill(secretKeyBytes, (byte) 0x00);
			}
			sessionRO.logout();
			sessionRO.closeSession();
		}
	}

	/**
	 * Write a share. Operation can fail if the old pin is wrong or a general token failure.
	 *
	 * @param token          the {@link Token} to write the {@link Share} to.
	 * @param oldPINPUK      the pin that protected the {@link Token}.
	 * @param newPINPUK      the new pint to protect the {@link Token}.
	 * @param cardLabel      the name of the {@link Token}.
	 * @param encryptedShare the encrypted information of the {@link Share}
	 * @throws TokenException if there is any problem writing to the {@link Token}.
	 */
	public void writeShare(final Token token, final char[] oldPINPUK, final char[] newPINPUK, final String cardLabel,
			final EncryptedShare encryptedShare) throws TokenException {

		// clear the token, initializing PUK
		token.initToken(oldPINPUK, cardLabel);

		// get session to make operations to token.
		final Session sessionRW = token.openSession(Token.SessionType.SERIAL_SESSION, Token.SessionReadWriteBehavior.RW_SESSION, null, null);

		// initialize PUK
		try {
			sessionRW.login(Session.UserType.SO, oldPINPUK);
			sessionRW.initPIN(newPINPUK);
			sessionRW.setPIN(oldPINPUK, newPINPUK);
		} finally {
			sessionRW.logout();
		}

		try {
			// login with new pin
			sessionRW.login(Session.UserType.USER, newPINPUK);

			// save public part
			sessionRW.createObject(createDataObject(encryptedShare.getEncryptedShareBytes(), ENCRYPTED_SHARE_LABEL.toCharArray(), Boolean.FALSE));
			sessionRW.createObject(createDataObject(encryptedShare.getEncryptedShareSignature(), SHARE_SIGNATURE_LABEL.toCharArray(), Boolean.FALSE));

			// save private part
			sessionRW.createObject(createDataObject(encryptedShare.getSecretKeyBytes(), SECRET_KEY_SHARE_LABEL.toCharArray(), Boolean.TRUE));
		} finally {
			sessionRW.logout();
		}

		sessionRW.closeSession();
	}

	/**
	 * Get the number of remaining attempts before blocking the PIN or PUK. The pkcs11 API only gives partial information, and therefore, the number
	 * of retries left is only accurate if we configure the token to lock after three or fewer retries. Locking after three attempts is the default
	 * setting.
	 *
	 * @param token    The token to read.
	 * @param userType The type of use (PIN/PUK). {@link Session.UserType#USER} for PIN, {@link Session.UserType#SO} for PUK
	 * @return A number between 0 and 3
	 * @throws TokenException If there is an error accessing the {@link Token}
	 */
	public int getNumberOfRetries(final Token token, final boolean userType) throws TokenException {
		final TokenInfo tokenInfo = token.getTokenInfo();
		if (Session.UserType.USER == userType) {
			return getNumberOfRetriesUser(tokenInfo);
		} else {
			// Session.UserType.SO
			return getNumberOfRetriesSO(tokenInfo);
		}
	}
}
