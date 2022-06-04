/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.configuration;

import static ch.post.it.evoting.controlcomponents.configuration.setupvoting.GenKeysCCRService.GenKeysCCROutput;
import static com.google.common.base.Preconditions.checkNotNull;

import java.security.KeyManagementException;
import java.time.ZonedDateTime;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.controlcomponents.CcmjElectionKeysService;
import ch.post.it.evoting.controlcomponents.CcrjReturnCodesKeys;
import ch.post.it.evoting.controlcomponents.CcrjReturnCodesKeysService;
import ch.post.it.evoting.controlcomponents.configuration.setuptally.SetupTallyCCMService;
import ch.post.it.evoting.controlcomponents.configuration.setupvoting.GenKeysCCRService;
import ch.post.it.evoting.controlcomponents.keymanagement.ElectionSigningKeys;
import ch.post.it.evoting.controlcomponents.keymanagement.ElectionSigningKeysService;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeys;

@Service
class KeyGenerationService {

	private final GenKeysCCRService genKeysCCRService;
	private final SetupTallyCCMService setupTallyCCMService;
	private final CcmjElectionKeysService ccmjElectionKeysService;
	private final CcrjReturnCodesKeysService ccrjReturnCodesKeysService;
	private final ElectionSigningKeysService electionSigningKeysService;

	@Value("${nodeID}")
	private int nodeId;

	KeyGenerationService(
			final GenKeysCCRService genKeysCCRService,
			final SetupTallyCCMService setupTallyCCMService,
			final CcmjElectionKeysService ccmjElectionKeysService,
			final CcrjReturnCodesKeysService ccrjReturnCodesKeysService,
			final ElectionSigningKeysService electionSigningKeysService) {
		this.genKeysCCRService = genKeysCCRService;
		this.setupTallyCCMService = setupTallyCCMService;
		this.ccmjElectionKeysService = ccmjElectionKeysService;
		this.ccrjReturnCodesKeysService = ccrjReturnCodesKeysService;
		this.electionSigningKeysService = electionSigningKeysService;
	}

	@Transactional
	ControlComponentPublicKeys generateCCKeys(final String electionEventId, final GqGroup encryptionParameters) {
		// Generate ccrj keys and save them.
		final GenKeysCCROutput genKeysCCROutput = genKeysCCRService.genKeysCCR(encryptionParameters);
		final ZqElement ccrjReturnCodesGenerationSecretKey = genKeysCCROutput.getCcrjReturnCodesGenerationSecretKey();
		final ElGamalMultiRecipientKeyPair ccrjChoiceReturnCodesEncryptionKeyPair = genKeysCCROutput.getCcrjChoiceReturnCodesEncryptionKeyPair();

		final CcrjReturnCodesKeys newCcrjReturnCodesKeys = new CcrjReturnCodesKeys(electionEventId, ccrjReturnCodesGenerationSecretKey,
				ccrjChoiceReturnCodesEncryptionKeyPair);
		ccrjReturnCodesKeysService.save(newCcrjReturnCodesKeys);

		// Generate ccm election key pair and save it.
		final ElGamalMultiRecipientKeyPair ccmElectionKeyPair = setupTallyCCMService.setupTallyCCM(encryptionParameters);
		ccmjElectionKeysService.save(electionEventId, ccmElectionKeyPair);

		return new ControlComponentPublicKeys(nodeId, ccrjChoiceReturnCodesEncryptionKeyPair.getPublicKey(), ccmElectionKeyPair.getPublicKey());
	}

	ElectionSigningKeys generateElectionSigningKeys(final String electionEventId) throws KeyManagementException {
		checkNotNull(electionEventId);

		// Generate election signing keys used to sign payloads created by the control-components.
		final ZonedDateTime from = ZonedDateTime.now();
		final ZonedDateTime to = from.plusMonths(6);

		return electionSigningKeysService.createElectionSigningKeys(electionEventId, from, to);
	}

}
