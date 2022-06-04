/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.controlcomponents.configuration.electioncontext.ElectionContextRepository;
import ch.post.it.evoting.controlcomponents.voting.EncryptedVerifiableVoteRepository;

@Service
public class TestDatabaseCleanUpService {

	@Autowired
	private ElectionContextRepository electionContextRepository;

	@Autowired
	private CcrjReturnCodesKeysRepository ccrjReturnCodesKeysRepository;

	@Autowired
	private CcmjElectionKeysRepository ccmjElectionKeysRepository;

	@Autowired
	private VerificationCardRepository verificationCardRepository;

	@Autowired
	private VerificationCardSetRepository verificationCardSetRepository;

	@Autowired
	private EncryptedVerifiableVoteRepository encryptedVerifiableVoteRepository;

	@Autowired
	private ElectionEventRepository electionEventRepository;

	public void cleanUp() {
		encryptedVerifiableVoteRepository.deleteAll();
		verificationCardRepository.deleteAll();
		verificationCardSetRepository.deleteAll();
		ccrjReturnCodesKeysRepository.deleteAll();
		ccmjElectionKeysRepository.deleteAll();
		electionContextRepository.deleteAll();
		electionEventRepository.deleteAll();
	}

}
