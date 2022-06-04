/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service;

import java.io.BufferedReader;
import java.io.IOException;

public interface VerificationCardDataGeneratorService {

	/**
	 * Requests the generation of verification card IDs.
	 *
	 * @param electionEventId       the election event the verification cards belong to
	 * @param verificationCardSetId the verification card set identifier
	 * @param numberOfVotingCards   the number of verification card IDs to generate
	 * @return a buffered reader with a verification card ID per line
	 */
	BufferedReader precompute(String electionEventId, String verificationCardSetId, int numberOfVotingCards) throws IOException;
}
