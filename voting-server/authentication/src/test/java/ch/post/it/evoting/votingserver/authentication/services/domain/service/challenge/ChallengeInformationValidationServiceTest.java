/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.challenge;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Iterator;

import javax.enterprise.inject.Instance;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.validation.ChallengeInformationValidation;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class ChallengeInformationValidationServiceTest {

	@InjectMocks
	ChallengeInformationValidationService validationService = new ChallengeInformationValidationService();

	@Mock
	ChallengeInformation challengeInformationMock = new ChallengeInformation();

	@Mock
	Instance<ChallengeInformationValidation> challengeInformationValidationsMock;

	@Mock
	Iterator<ChallengeInformationValidation> iteratorMock1;

	@Mock
	Iterator<ChallengeInformationValidation> iteratorMock2;

	@Mock
	ChallengeInformationValidation challengeInformationValidationMock;

	@Mock
	Logger logger;
	ChallengeInformationCredentialIdValidation rule1 = new ChallengeInformationCredentialIdValidation();
	ChallengeInformationExpirationTimeValidation rule2 = new ChallengeInformationExpirationTimeValidation();
	ChallengeClientInformationSignatureValidation rule3 = new ChallengeClientInformationSignatureValidation();
	ChallengeServerInformationSignatureValidation rule4 = new ChallengeServerInformationSignatureValidation();

	@Test
	public void testCreation() {
		when(challengeInformationValidationsMock.iterator()).thenReturn(Arrays.asList(rule1, rule2, rule3, rule4).iterator());
		try {
			validationService.setValidations(challengeInformationValidationsMock);
		} catch (Exception e) {
			fail("Test shouldn't throw exception " + e.getMessage());
		}
	}

	@Test
	public void validateFalse() throws ResourceNotFoundException, CryptographicOperationException {
		when(challengeInformationValidationsMock.iterator()).thenReturn(iteratorMock1);
		when(iteratorMock1.hasNext()).thenReturn(true, false);
		when(iteratorMock1.next()).thenReturn(challengeInformationValidationMock);
		validationService.setValidations(challengeInformationValidationsMock);

		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";
		when(challengeInformationValidationMock.execute(tenantId, electionEventId, votingCardId, challengeInformationMock)).thenReturn(false);

		ValidationResult result = validationService.validate(tenantId, electionEventId, votingCardId, challengeInformationMock);

		assertFalse(result.isResult());
	}

	@Test
	public void validateTrue() throws ResourceNotFoundException, CryptographicOperationException {
		when(challengeInformationValidationsMock.iterator()).thenReturn(iteratorMock2);
		when(iteratorMock2.hasNext()).thenReturn(true, false);
		when(iteratorMock2.next()).thenReturn(challengeInformationValidationMock);
		validationService.setValidations(challengeInformationValidationsMock);

		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";
		when(challengeInformationValidationMock.execute(tenantId, electionEventId, votingCardId, challengeInformationMock)).thenReturn(true);

		ValidationResult result = validationService.validate(tenantId, electionEventId, votingCardId, challengeInformationMock);

		assertTrue(result.isResult());
	}
}
