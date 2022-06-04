/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.controlcomponents.tally.mixonline;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyManagementException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.controlcomponents.CryptolibPayloadSignatureService;
import ch.post.it.evoting.controlcomponents.keymanagement.ElectionSigningKeysService;
import ch.post.it.evoting.controlcomponents.keymanagement.KeyServicesMock;
import ch.post.it.evoting.controlcomponents.keymanagement.KeysManager;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.SignedPayload;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.domain.election.model.messaging.PayloadSignatureException;

class CryptolibPayloadSignatureServiceTest {
	private static final AsymmetricService ASYMMETRIC_SERVICE = new AsymmetricService();
	private static final HashService HASH_SERVICE = HashService.getInstance();
	private static KeysManager keysManager;
	private static CryptolibPayloadSignatureService signatureService;
	private static ElectionSigningKeysService electionSigningKeysService;

	private PrivateKey signingKey;
	private X509Certificate[] certificateChain;
	private SignedPayload payload;

	static class DummySignedPayload implements SignedPayload {

		private CryptoPrimitivesPayloadSignature signature;
		private final String value;

		public DummySignedPayload(String value) {
			this.value = value;
		}

		@Override
		public ImmutableList<? extends Hashable> toHashableForm() {
			return ImmutableList.of(HashableString.from(value));
		}

		@Override
		public CryptoPrimitivesPayloadSignature getSignature() {
			return signature;
		}

		@Override
		public void setSignature(CryptoPrimitivesPayloadSignature signature) {
			this.signature = Preconditions.checkNotNull(signature);
		}
	}

	@BeforeAll
	static void setupAll() throws GeneralCryptoLibException, KeyManagementException {
		final KeyServicesMock keyServicesMock = new KeyServicesMock();
		keysManager = keyServicesMock.keyManager();
		electionSigningKeysService = keyServicesMock.electionSigningKeysService();
		signatureService = new CryptolibPayloadSignatureService(ASYMMETRIC_SERVICE, HASH_SERVICE);
	}

	@BeforeEach
	void setup() throws PayloadSignatureException, KeyManagementException {
		signingKey = electionSigningKeysService.getElectionSigningKeys("0b88257ec32142bb8ee0ed1bb70f362e").privateKey();
		certificateChain = new X509Certificate[1];
		certificateChain[0] = keysManager.getPlatformCACertificate();

		payload = new DummySignedPayload("Hashable payload's value");
		payload = signatureService.sign(payload, signingKey, certificateChain);
	}

	@Test
	@DisplayName("Sign with null arguments throws a NullPointerException")
	void signWithNullArguments() {
		assertThrows(NullPointerException.class, () -> signatureService.sign(null, signingKey, certificateChain));
		assertThrows(NullPointerException.class, () -> signatureService.sign(payload, null, certificateChain));
		assertThrows(NullPointerException.class, () -> signatureService.sign(payload, signingKey, null));
	}

	@Test
	@DisplayName("Sign with valid inputs does not throw")
	void signWithValidInput() {
		SignedPayload signedPayload = assertDoesNotThrow(() -> signatureService.sign(payload, signingKey, certificateChain));
		assertArrayEquals(certificateChain, signedPayload.getSignature().getCertificateChain());
	}

	@Test
	@DisplayName("Verify with null arguments throws a NullPointerException")
	void verifyWithNullArguments() {
		assertThrows(NullPointerException.class, () -> signatureService.verify(null, certificateChain[0]));
		assertThrows(NullPointerException.class, () -> signatureService.verify(payload, null));
	}

	@Test
	@DisplayName("Verify with valid arguments does not throw")
	void verifyWithValidArguments() {
		final Boolean valid = assertDoesNotThrow(() -> signatureService.verify(payload, certificateChain[0]));
		assertTrue(valid);
	}

	@Test
	@DisplayName("Verify with different certificate returns false")
	void verifyWithInvalidCertificate() throws GeneralCryptoLibException {
		certificateChain[0] = new KeyServicesMock().keyManager().getPlatformCACertificate();
		final Boolean valid = assertDoesNotThrow(() -> signatureService.verify(payload, certificateChain[0]));
		assertFalse(valid);
	}

	@Test
	@DisplayName("Verify with different signature returns false")
	void verifyWithInvalidSignature() throws PayloadSignatureException {
		DummySignedPayload differentPayload = new DummySignedPayload("Another hashable payload's value");
		DummySignedPayload differentSignedPayload = signatureService.sign(differentPayload, signingKey, certificateChain);
		payload.setSignature(differentSignedPayload.getSignature());
		final Boolean valid = assertDoesNotThrow(() -> signatureService.verify(payload, certificateChain[0]));
		assertFalse(valid);
	}
}
