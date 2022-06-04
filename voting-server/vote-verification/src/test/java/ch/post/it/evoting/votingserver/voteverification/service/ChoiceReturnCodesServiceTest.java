/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.domain.election.VoteVerificationContextData;
import ch.post.it.evoting.domain.returncodes.ShortChoiceReturnCodeAndComputeResults;
import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.domain.voting.sendvote.LongReturnCodesSharePayload;
import ch.post.it.evoting.domain.voting.sendvote.PartiallyDecryptedEncryptedPCCPayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformCAEntity;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformCARepository;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.VerificationContent;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.VerificationContentRepository;
import ch.post.it.evoting.votingserver.voteverification.domain.model.verification.Verification;
import ch.post.it.evoting.votingserver.voteverification.domain.model.verification.VerificationRepository;
import ch.post.it.evoting.votingserver.voteverification.infrastructure.remote.MessageBrokerOrchestratorClient;

import retrofit2.Call;
import retrofit2.Response;

@DisplayName("ChoiceReturnCodesService")
@ExtendWith(MockitoExtension.class)
class ChoiceReturnCodesServiceTest {

	private static final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
	private static ChoiceReturnCodesService choiceReturnCodesService;
	private static VerificationRepository mockVerificationRepository;
	private static VerificationContentRepository mockVerificationContentRepository;
	private static MessageBrokerOrchestratorClient mockMessageBrokerOrchestratorClient;
	private static PlatformCARepository mockPlatformCARepository;

	private static InputStream getResourceAsStream(final String name) {
		return ChoiceReturnCodesServiceTest.class.getResourceAsStream("/choiceReturnCodesServiceTest/" + name);
	}

	@BeforeAll
	static void setUpAll() {
		// mockPlatformCARepository
		mockVerificationRepository = mock(VerificationRepository.class);
		mockVerificationContentRepository = mock(VerificationContentRepository.class);
		mockMessageBrokerOrchestratorClient = mock(MessageBrokerOrchestratorClient.class);
		final ExtractCRCService mockExtractCRCService = mock(ExtractCRCService.class);
		mockPlatformCARepository = mock(PlatformCARepository.class);

		choiceReturnCodesService = new ChoiceReturnCodesService(HashService.getInstance(), objectMapper, mockExtractCRCService,
				mockPlatformCARepository,
				mockVerificationRepository, new CryptolibPayloadSignatureService(new AsymmetricService(), HashService.getInstance()),
				mockVerificationContentRepository, mockMessageBrokerOrchestratorClient);
	}

	@Test
	@DisplayName("retrieveShortChoiceReturnCodes with valid parameters and happy path")
	void retrieveShortChoiceReturnCodesHappyPath() throws CryptographicOperationException, IOException, ResourceNotFoundException {

		final String tenantId = "100";
		final String electionEventId = "2ab0f89bb1b242f4be9857fbd1dd6c3d";
		final String verificationCardId = "de7af86472e409def5263e2fe2002c66";
		final String verificationCardSetId = "afafa2434e1c4fa691865dfc5ab3a4d4";

		final Verification verification = new Verification();
		final VerificationContent verificationContent = new VerificationContent();
		final PlatformCAEntity platformCAEntity = new PlatformCAEntity();

		final String voteAndComputeResultsJson = "voteAndComputeResults.json";
		final String voteVerificationContextDataJson = "voteVerificationContextData.json";
		final String partiallyDecryptedEncryptedPCCPayloadsJson = "partiallyDecryptedEncryptedPCCPayloads.json";
		final String longReturnCodesSharePayloadsJson = "longReturnCodesSharePayloads.json";
		final String platformCACertificatePem = "platformCACertificate.pem";

		// Convert json to object.
		InputStream is = getResourceAsStream(voteAndComputeResultsJson);
		final VoteAndComputeResults voteAndComputeResults = objectMapper.readValue(is, VoteAndComputeResults.class);

		is = getResourceAsStream(voteVerificationContextDataJson);
		final VoteVerificationContextData voteVerificationContextData = objectMapper.readValue(is, VoteVerificationContextData.class);

		is = getResourceAsStream(partiallyDecryptedEncryptedPCCPayloadsJson);
		final List<PartiallyDecryptedEncryptedPCCPayload> partiallyDecryptedEncryptedPCCPayloads = Arrays.asList(
				objectMapper.readValue(is, PartiallyDecryptedEncryptedPCCPayload[].class));

		is = getResourceAsStream(longReturnCodesSharePayloadsJson);
		final List<LongReturnCodesSharePayload> longReturnCodesSharePayload = Arrays.asList(
				objectMapper.readValue(is, LongReturnCodesSharePayload[].class));

		is = getResourceAsStream(platformCACertificatePem);
		final String pem = new BufferedReader(
				new InputStreamReader(is, StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.joining("\n"));

		platformCAEntity.setCertificateContent(pem);
		verification.setVerificationCardSetId(verificationCardSetId);
		verificationContent.setJson(objectMapper.writeValueAsString(voteVerificationContextData));

		/* Expectations */
		when(mockVerificationRepository.findByTenantIdElectionEventIdVerificationCardId(tenantId, electionEventId, verificationCardId))
				.thenReturn(verification);

		when(mockVerificationContentRepository.findByTenantIdElectionEventIdVerificationCardSetId(tenantId, electionEventId, verificationCardSetId))
				.thenReturn(verificationContent);

		final Call<List<PartiallyDecryptedEncryptedPCCPayload>> callMockPartiallyDecryptedEncryptedPCCPayload = (Call<List<PartiallyDecryptedEncryptedPCCPayload>>) mock(
				Call.class);
		final Response<List<PartiallyDecryptedEncryptedPCCPayload>> partiallyDecryptedSuccess = Response.success(
				partiallyDecryptedEncryptedPCCPayloads);
		when(callMockPartiallyDecryptedEncryptedPCCPayload.execute()).thenReturn(partiallyDecryptedSuccess);

		final Call<List<LongReturnCodesSharePayload>> callMockLongReturnCodesSharePayload = (Call<List<LongReturnCodesSharePayload>>) mock(
				Call.class);
		final Response<List<LongReturnCodesSharePayload>> longReturnCodesSuccess = Response.success(longReturnCodesSharePayload);
		when(callMockLongReturnCodesSharePayload.execute()).thenReturn(longReturnCodesSuccess);

		when(mockMessageBrokerOrchestratorClient.getChoiceReturnCodesPartialDecryptContributions(eq(electionEventId), eq(verificationCardSetId),
				eq(verificationCardId), any()))
				.thenReturn(callMockPartiallyDecryptedEncryptedPCCPayload);

		when(mockMessageBrokerOrchestratorClient.getLongChoiceReturnCodesContributions(eq(electionEventId), eq(verificationCardSetId),
				eq(verificationCardId), any()))
				.thenReturn(callMockLongReturnCodesSharePayload);

		when(mockPlatformCARepository.getRootCACertificate())
				.thenReturn(platformCAEntity);

		/* Execution */
		final ShortChoiceReturnCodeAndComputeResults shortChoiceReturnCodeAndComputeResultsResponse = choiceReturnCodesService.retrieveShortChoiceReturnCodes(
				tenantId, electionEventId, verificationCardId, voteAndComputeResults);

		/* Verification */
		verify(mockVerificationRepository).findByTenantIdElectionEventIdVerificationCardId(tenantId, electionEventId, verificationCardId);

		assertNotNull(shortChoiceReturnCodeAndComputeResultsResponse);
		assertNotNull(shortChoiceReturnCodeAndComputeResultsResponse.getComputationResults());

	}
}
