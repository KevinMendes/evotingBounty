/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.shares.handler;

import java.security.KeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import ch.post.it.evoting.cryptolib.api.secretsharing.Share;
import ch.post.it.evoting.cryptolib.certificates.csr.CSRGenerator;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SharesException;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SmartcardException;
import ch.post.it.evoting.securedatamanager.config.shares.shares.keys.KeyPairGenerator;
import ch.post.it.evoting.securedatamanager.config.shares.shares.service.PrivateKeySharesService;
import ch.post.it.evoting.securedatamanager.config.shares.shares.service.SmartCardService;

/**
 * Handler that allows for the generation of a keypair, the splitting of the private key into shares, the generation of a CSR from the public key if
 * appropriate, and finally, the writing of the shares to smartcards.
 * <p>
 * This class has state, and its methods should be called in a particular order. It takes steps to ensure that secret information, such as private
 * keys, that temporarily exist in memory are overwritten as soon as possible.
 * <p>
 * The following type of asymmetric key is supported:
 * <ul>
 * <li>RSA</li>
 * </ul>
 * <p>
 * The normal flow from the perspective of the consumer of this class is the following:
 * <ul>
 * <li>Generate a keypair and split it into shares (the shares are initially stored in memory).</li>
 * <li>Generate a CSR from the public key (the private key is
 * used for signing at this point, but once the CSR is created, then the private key is wiped from
 * memory as it will no longer be needed).</li>
 * <li>For each share, write that share to a smartcard.</li>
 * </ul>
 * <p>
 * Note: this class makes the assumption that RSA keys are used when creating Administration Boards
 * (a.k.a. Administration Authorities).
 * <p>
 * When writing shares to a smartcard, this class signs the share and also writes the signature of
 * the share to the smartcard. The private key of the newly created keypair
 * (the private key of the Administration Board itself) is used to sign the share.
 */
public final class CreateSharesHandler {

	private final KeyPairGenerator keyPairGenerator;

	private final SmartCardService smartcardService;

	private final PrivateKeySharesService splitService;

	private List<Share> shares;

	private PublicKey publicKey;

	private PrivateKey privateKey;

	private boolean allGenerateTasksCompleted;

	private int numSharesWritten;

	/**
	 * Create a handler for the creation and writing of shares to smartcards.
	 *
	 * @param keyPairGenerator the generator that should be used to create the keypair.
	 * @param splitService     the service to be used to split the private key into shares.
	 * @param smartcardService the service to be used to interact with smartcards.
	 * @throws SharesException if there is a problem with any of the inputs.
	 */
	public CreateSharesHandler(final KeyPairGenerator keyPairGenerator, final PrivateKeySharesService splitService,
			final SmartCardService smartcardService) throws SharesException {

		validateInputs(keyPairGenerator, splitService, smartcardService);

		this.keyPairGenerator = keyPairGenerator;
		this.splitService = splitService;
		this.smartcardService = smartcardService;

		this.allGenerateTasksCompleted = false;
		this.numSharesWritten = 0;
	}

	/**
	 * Generate a keypair and split the private key into shares.
	 *
	 * @param n         the number of shares that the private key should be split into.
	 * @param threshold the threshold to set. This is the minimum number of shares that are necessary to be known in order to reconstruct the private
	 *                  key.
	 * @throws SharesException if the threshold is bigger than the total number of shares.
	 */
	public void generateAndSplit(final int n, final int threshold) throws SharesException {

		// Validations
		if (n < threshold) {
			throw new SharesException("Threshold must be less than or equal to the total number of shares");
		}

		// Generate key pair
		final KeyPair keyPair;
		try {
			keyPair = keyPairGenerator.generate();
		} catch (final KeyException e) {
			throw new SharesException("Error while generating the keypair", e);
		}

		// store the public key for later use
		publicKey = keyPair.getPublic();

		try {
			// Split the private key and store locally the shares
			shares = splitService.split(keyPair.getPrivate(), n, threshold);
			privateKey = keyPair.getPrivate();

		} catch (final KeyException e) {
			throw new SharesException("There was an error while trying to split the private key", e);
		}

		allGenerateTasksCompleted = true;
	}

	/**
	 * Generate a Certificate Signing Request (CSR).
	 * <p>
	 * This process also wipes the private key (created earlier) from memory as it will no longer be needed once the CSR has been generated.
	 *
	 * @param x500Principal encapsulates information that describes the subject.
	 * @param csrGenerator  a generator of CSRs.
	 * @return a CSR in the form of a {org.bouncycastle.pkcs.PKCS10CertificationRequest}.
	 * @throws SharesException if there is any problem when generating the CSR or wiping the private key from memory.
	 */
	public PKCS10CertificationRequest generateCSR(final X500Principal x500Principal, final CSRGenerator csrGenerator) throws SharesException {

		validateCsrInputsAndState(x500Principal, csrGenerator);

		final PKCS10CertificationRequest csr;
		try {
			csr = csrGenerator.generate(publicKey, privateKey, x500Principal);
		} catch (final OperatorCreationException e) {
			throw new SharesException("There was an error while trying to create the CSR", e);
		}

		// Once we wrote the certificate signing request (CSR), the private key can be wiped from memory.
		privateKey = null;

		return csr;
	}

	/**
	 * Write a share to a smartcard, sign the share using the actual private key that has been split into shares.
	 *
	 * @param i              the index of the share.
	 * @param name           the name associated with the share.
	 * @param oldPinPuk      the old pin and puk to set.
	 * @param newPinPuk      the new pin and puk to set.
	 * @param finalOperation runnable executed when the last share is written
	 * @throws SharesException if the share could not be processed.
	 */
	public void writeShareAndSelfSign(final int i, final String name, final String oldPinPuk, final String newPinPuk, final Runnable finalOperation)
			throws SharesException {

		validateInputs(i, name, oldPinPuk, newPinPuk);

		processShare(i, name, oldPinPuk, newPinPuk, privateKey, finalOperation);
	}

	public PublicKey getPublicKey() throws SharesException {

		if (!allGenerateTasksCompleted) {
			throw new SharesException("Tried to retrieve the public key before is has been generated");
		}

		return publicKey;
	}

	public boolean isSmartcardOk() {

		return smartcardService.isSmartcardOk();
	}

	public boolean isSharesInMemory() {

		return !(shares == null || shares.isEmpty());
	}

	private void validateCsrInputsAndState(final X500Principal x500Principal, final CSRGenerator csrGenerator) throws SharesException {

		if (csrGenerator == null) {
			throw new SharesException("The CSRGenerator was null");
		}
		if (x500Principal == null) {
			throw new SharesException("The X500Principal was null");
		}
		if (!allGenerateTasksCompleted) {
			throw new SharesException("Tried to create a CSR before the key pair was generated");
		}
	}

	private void processShare(final int i, final String name, final String oldPinPuk, final String newPinPuk,
			final PrivateKey privateKeyToBeUsedToSign, final Runnable finalOperation) throws SharesException {

		final Share share = shares.get(i);

		try {
			smartcardService.write(share, name, oldPinPuk, newPinPuk, privateKeyToBeUsedToSign);
		} catch (final SmartcardException e) {
			throw new SharesException("An error occured while trying to write a share", e);
		}

		numSharesWritten++;

		if (numSharesWritten == shares.size()) {
			finalOperation.run();
			shares = null;
		}
	}

	private void validateInputs(final int i, final String name, final String pin, final String puk) throws SharesException {

		if (i < 0 || i > shares.size() - 1) {
			throw new SharesException("The index must be between 0 (inclusive) and the number of shares (exclusive)");
		}
		if ((name == null) || (name.isEmpty())) {
			throw new SharesException("The name must be an initialized non-empty string");
		}
		if ((pin == null) || (pin.isEmpty())) {
			throw new SharesException("The pin must be an initialized non-empty string");
		}
		if ((puk == null) || (puk.isEmpty())) {
			throw new SharesException("The puk must be an initialized non-empty string");
		}
	}

	private void validateInputs(final KeyPairGenerator keyPairGenerator,
			final PrivateKeySharesService splitService, final SmartCardService smartcardService) throws SharesException {

		if (keyPairGenerator == null) {
			throw new SharesException("The KeyPairGenerator must be an initialized");
		}
		if (splitService == null) {
			throw new SharesException("The splitService must be an initialized");
		}
		if (smartcardService == null) {
			throw new SharesException("The smartcardService must be an initialized");
		}
	}

}
