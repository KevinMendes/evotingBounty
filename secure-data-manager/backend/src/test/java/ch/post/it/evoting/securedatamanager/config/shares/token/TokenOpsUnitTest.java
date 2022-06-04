/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.securedatamanager.config.shares.token;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptolib.api.secretsharing.Share;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.secretsharing.service.ThresholdSecretSharingService;
import ch.post.it.evoting.securedatamanager.config.shares.shares.EncryptedShare;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SharesException;

import iaik.pkcs.pkcs11.Session;
import iaik.pkcs.pkcs11.Token;
import iaik.pkcs.pkcs11.TokenException;
import iaik.pkcs.pkcs11.TokenInfo;
import iaik.pkcs.pkcs11.UnsupportedAttributeException;
import iaik.pkcs.pkcs11.objects.Attribute;
import iaik.pkcs.pkcs11.objects.Certificate;
import iaik.pkcs.pkcs11.objects.Data;
import iaik.pkcs.pkcs11.objects.Object;

@ExtendWith(MockitoExtension.class)
class TokenOpsUnitTest {
	private static final char[] PIN_PUK = "22222222".toCharArray();
	private static final String CARD_LABEL = "Share 1 label";

	private static KeyPair boardKeyPair;
	private static EncryptedShare encryptedShare;

	private final TokenOps tokenOps = new TokenOps();

	@Mock
	private Token token;

	@Mock
	private TokenInfo tokeninfo;

	@Mock
	private Session session;

	@BeforeAll
	public static void initialize() {
		boardKeyPair = new AsymmetricService().getKeyPairForEncryption();

		final BigInteger privateExponent = ((RSAPrivateKey) boardKeyPair.getPrivate()).getPrivateExponent();
		final byte[] originalSecretBytes = privateExponent.toByteArray();

		// The split method clears the original secret bytes.
		final byte[] clonedSecretBytes = originalSecretBytes.clone();

		// Create some Shares.
		final Set<Share> shares = new ThresholdSecretSharingService().split(clonedSecretBytes, 5, 3, privateExponent.nextProbablePrime());

		// Create an encrypted share that can be used as a real one.
		encryptedShare = new EncryptedShare(shares.iterator().next(), boardKeyPair.getPrivate());
	}

	@Test
	void checkUserRetries() throws TokenException {
		when(token.getTokenInfo()).thenReturn(tokeninfo);

		final int retriesLeft = tokenOps.getNumberOfRetries(token, Session.UserType.USER);

		assertEquals(TokenOps.NUMBER_OF_RETRIES, retriesLeft);
	}

	@Test
	void checkUserRetriesLocked() throws TokenException {
		when(token.getTokenInfo()).thenReturn(tokeninfo);
		when(tokeninfo.isUserPinLocked()).thenReturn(true);

		final int retriesLeft = tokenOps.getNumberOfRetries(token, Session.UserType.USER);

		assertEquals(0, retriesLeft);
	}

	@Test
	void checkUserRetriesFinalTry() throws TokenException {
		when(token.getTokenInfo()).thenReturn(tokeninfo);
		when(tokeninfo.isUserPinFinalTry()).thenReturn(true);

		final int retriesLeft = tokenOps.getNumberOfRetries(token, Session.UserType.USER);

		assertEquals(1, retriesLeft);
	}

	@Test
	void checkUserRetriesFailedLast() throws TokenException {
		when(token.getTokenInfo()).thenReturn(tokeninfo);
		when(tokeninfo.isUserPinCountLow()).thenReturn(true);

		final int retriesLeft = tokenOps.getNumberOfRetries(token, Session.UserType.USER);

		assertEquals(2, retriesLeft);
	}

	@Test
	void checkAdminRetries() throws TokenException {
		when(token.getTokenInfo()).thenReturn(tokeninfo);

		final int retriesLeft = tokenOps.getNumberOfRetries(token, Session.UserType.SO);

		assertEquals(TokenOps.NUMBER_OF_RETRIES, retriesLeft);
	}

	@Test
	void checkAdminRetriesLocked() throws TokenException {
		when(token.getTokenInfo()).thenReturn(tokeninfo);
		when(tokeninfo.isSoPinLocked()).thenReturn(true);

		final int retriesLeft = tokenOps.getNumberOfRetries(token, Session.UserType.SO);

		assertEquals(0, retriesLeft);
	}

	@Test
	void checkAdminRetriesFinalTry() throws TokenException {
		when(token.getTokenInfo()).thenReturn(tokeninfo);
		when(tokeninfo.isSoPinFinalTry()).thenReturn(true);

		final int retriesLeft = tokenOps.getNumberOfRetries(token, Session.UserType.SO);

		assertEquals(1, retriesLeft);
	}

	@Test
	void checkAdminRetriesFailedLast() throws TokenException {
		when(token.getTokenInfo()).thenReturn(tokeninfo);
		when(tokeninfo.isSoPinCountLow()).thenReturn(true);

		final int retriesLeft = tokenOps.getNumberOfRetries(token, Session.UserType.SO);

		assertEquals(2, retriesLeft);
	}

	@Test
	void writeShare() throws TokenException {
		when(token.openSession(anyBoolean(), anyBoolean(), any(), any())).thenReturn(session);

		assertDoesNotThrow(() -> tokenOps.writeShare(token, PIN_PUK, PIN_PUK, CARD_LABEL, encryptedShare));
	}

	@Test
	void writeShareFilSOLogin() throws TokenException {
		when(token.openSession(anyBoolean(), anyBoolean(), any(), any())).thenReturn(session);

		doThrow(new TokenException()).when(session).login(Session.UserType.SO, PIN_PUK);

		assertThrows(TokenException.class, () -> tokenOps.writeShare(token, PIN_PUK, PIN_PUK, CARD_LABEL, encryptedShare));
	}

	@Test
	void writeShareFailUserLogin() throws TokenException {
		when(token.openSession(anyBoolean(), anyBoolean(), any(), any())).thenReturn(session);

		doNothing().when(session).login(Session.UserType.SO, PIN_PUK);
		doThrow(new TokenException()).when(session).login(Session.UserType.USER, PIN_PUK);

		assertThrows(TokenException.class, () -> tokenOps.writeShare(token, PIN_PUK, PIN_PUK, CARD_LABEL, encryptedShare));
	}

	@Test
	void readShare() throws TokenException, SharesException, UnsupportedAttributeException {
		final Data encryptedShareBytesData = new Data();
		encryptedShareBytesData.putAttribute(Attribute.VALUE, getEncryptedShareBytes());

		final Data encryptedShareSignatureBytesData = new Data();
		encryptedShareSignatureBytesData.putAttribute(Attribute.VALUE, getEncryptedShareSignatureBytes());

		final Data shareSecretKeyBytes = new Data();
		shareSecretKeyBytes.putAttribute(Attribute.VALUE, getShareSecretKeyBytes());

		when(session.findObjects(anyInt())).thenReturn(new Object[] { encryptedShareBytesData }, new Object[] { encryptedShareSignatureBytesData },
				new Object[] { shareSecretKeyBytes });

		when(token.openSession(anyBoolean(), anyBoolean(), any(), any())).thenReturn(session);

		final Share share = tokenOps.readShare(token, PIN_PUK, getBoardPublicKey());

		assertEquals(5, share.getNumberOfParts());
		assertEquals(3, share.getThreshold());
	}

	@Test
	void readShareErrorReadingPrivatePart() throws TokenException, UnsupportedAttributeException {
		final Data encryptedShareBytesData = new Data();
		encryptedShareBytesData.putAttribute(Attribute.VALUE, getEncryptedShareBytes());

		final Data encryptedShareSignatureBytesData = new Data();
		encryptedShareSignatureBytesData.putAttribute(Attribute.VALUE, getEncryptedShareSignatureBytes());

		final Data shareSecretKeyBytes = new Data();
		shareSecretKeyBytes.putAttribute(Attribute.VALUE, getShareSecretKeyBytes());

		when(session.findObjects(anyInt())).thenReturn(new Object[] { encryptedShareBytesData }, new Object[] { encryptedShareSignatureBytesData },
				new Object[] { shareSecretKeyBytes });

		when(token.openSession(anyBoolean(), anyBoolean(), any(), any())).thenReturn(session);

		doThrow(new TokenException()).when(session).login(Session.UserType.USER, PIN_PUK);

		assertThrows(TokenException.class, () -> tokenOps.readShare(token, PIN_PUK, getBoardPublicKey()));
	}

	@Test
	void readShareErrorRecoveringPublicData() throws TokenException {
		doThrow(new TokenException()).when(session).findObjects(anyInt());
		when(token.openSession(anyBoolean(), anyBoolean(), any(), any())).thenReturn(session);

		assertThrows(TokenException.class, () -> tokenOps.readShare(token, null, null));
	}

	@Test
	void readShareErrorEmptyData() throws TokenException {
		when(token.openSession(anyBoolean(), anyBoolean(), any(), any())).thenReturn(session);

		assertThrows(IllegalArgumentException.class, () -> tokenOps.readShare(token, null, null));
	}

	@Test
	void readShareErrorWrongObjectClass() throws TokenException {
		when(session.findObjects(anyInt())).thenReturn(new Object[] { new Certificate() });
		when(token.openSession(anyBoolean(), anyBoolean(), any(), any())).thenReturn(session);

		assertThrows(IllegalArgumentException.class, () -> tokenOps.readShare(token, null, null));
	}

	@Test
	void readShareErrorNullObject() throws TokenException {
		when(session.findObjects(anyInt())).thenReturn(null);
		when(token.openSession(anyBoolean(), anyBoolean(), any(), any())).thenReturn(session);

		assertThrows(IllegalArgumentException.class, () -> tokenOps.readShare(token, null, null));
	}

	private byte[] getEncryptedShareBytes() {
		return encryptedShare.getEncryptedShareBytes();
	}

	private byte[] getEncryptedShareSignatureBytes() {
		return encryptedShare.getEncryptedShareSignature();
	}

	private byte[] getShareSecretKeyBytes() {
		return encryptedShare.getSecretKeyBytes();
	}

	private PublicKey getBoardPublicKey() {
		return boardKeyPair.getPublic();
	}

}
