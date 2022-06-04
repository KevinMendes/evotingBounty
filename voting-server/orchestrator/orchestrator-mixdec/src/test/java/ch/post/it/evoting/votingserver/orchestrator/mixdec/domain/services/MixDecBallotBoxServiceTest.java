/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.orchestrator.commons.config.QueuesConfig;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model.BallotBoxStatus;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model.MixDecStatus;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.persistence.CleansedBallotBoxRepository;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.persistence.MixDecBallotBoxStatusRepository;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.persistence.MixDecPayloadRepository;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.remote.ResourceNotReadyException;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith({ SystemStubsExtension.class, MockitoExtension.class })
class MixDecBallotBoxServiceTest {

	@InjectMocks
	private static final MixDecBallotBoxService sut = new MixDecBallotBoxService();

	@SystemStub
	private static EnvironmentVariables environmentVariables;

	private final String electionEventId = "b5f8fddd30234a12bb3b544af46d4fc4";
	private final String ballotBoxId = "1b17408618934e6493d9bbc8a8aca82b";
	private final List<String> ballotBoxIds = Collections.singletonList(ballotBoxId);
	private final String trackingId = "tracking";

	@Mock
	private ObjectMapper objectMapper;
	@Mock
	private MixDecBallotBoxStatusRepository mixDecBallotBoxStatusRepository;
	@Mock
	private CleansedBallotBoxRepository cleansedBallotBoxRepository;
	@Mock
	private MixDecPayloadRepository nodeOutputRepository;

	private String ccQueueNames;

	@BeforeEach
	void setUp() {
		ccQueueNames = System.getenv("CC_QUEUE_NAMES");
		environmentVariables.set("CC_QUEUE_NAMES",
				"{\"g4\": {\"cg-comp\": {\"res\": \"cg-comp-g4-res\", \"req\": \"cg-comp-g4-req\"}, \"cg-keygen\": {\"res\": \"cg-keygen-g4-res\", \"req\": \"cg-keygen-g4-req\"}}, \"g3\": {\"cg-comp\": {\"res\": \"cg-comp-g3-res\", \"req\": \"cg-comp-g3-req\"}, \"cg-keygen\": {\"res\": \"cg-keygen-g3-res\", \"req\": \"cg-keygen-g3-req\"}}, \"g2\": {\"cg-comp\": {\"res\": \"cg-comp-g2-res\", \"req\": \"cg-comp-g2-req\"}, \"cg-keygen\": {\"res\": \"cg-keygen-g2-res\", \"req\": \"cg-keygen-g2-req\"}}, \"g1\": {\"cg-comp\": {\"res\": \"cg-comp-g1-res\", \"req\": \"cg-comp-g1-req\"}, \"cg-keygen\": {\"res\": \"cg-keygen-g1-res\", \"req\": \"cg-keygen-g1-req\"}}, \"m1\": {\"md-keygen\": {\"res\": \"md-keygen-m1-res\", \"req\": \"md-keygen-m1-req\"}, \"md-mixdec\": {\"res\": \"md-mixdec-m1-res\", \"req\": \"md-mixdec-m1-req\"}}, \"m3\": {\"md-keygen\": {\"res\": \"md-keygen-m3-res\", \"req\": \"md-keygen-m3-req\"}, \"md-mixdec\": {\"res\": \"md-mixdec-m3-res\", \"req\": \"md-mixdec-m3-req\"}}, \"m2\": {\"md-keygen\": {\"res\": \"md-keygen-m2-res\", \"req\": \"md-keygen-m2-req\"}, \"md-mixdec\": {\"res\": \"md-mixdec-m2-res\", \"req\": \"md-mixdec-m2-req\"}}}");
	}

	@AfterEach
	void tearDown() {
		if (ccQueueNames == null) {
			System.clearProperty("CC_QUEUE_NAMES");
		} else {
			environmentVariables.set("CC_QUEUE_NAMES", ccQueueNames);
		}
	}

	@Test
	void testBlankBallotBox() throws ApplicationException {
		assertNotNull(QueuesConfig.MIX_DEC_COMPUTATION_REQ_QUEUES);
		when(cleansedBallotBoxRepository.isBallotBoxEmpty(any(), any())).thenReturn(true);

		final List<BallotBoxStatus> result = sut.processBallotBoxes(electionEventId, ballotBoxIds, trackingId);

		assertThat(result.get(0).getProcessStatus(), is(MixDecStatus.PROCESSING));
	}

	@Test
	void testBallotBox() throws ApplicationException, IOException, ResourceNotFoundException, ResourceNotReadyException {
		final MixnetInitialPayload mixnetInitialPayload = mock(MixnetInitialPayload.class);

		assertNotNull(QueuesConfig.MIX_DEC_COMPUTATION_REQ_QUEUES);
		when(cleansedBallotBoxRepository.isBallotBoxEmpty(any(), any())).thenReturn(false);
		when(cleansedBallotBoxRepository.getMixnetInitialPayload(any())).thenReturn(mixnetInitialPayload);

		final List<BallotBoxStatus> result = sut.processBallotBoxes(electionEventId, ballotBoxIds, trackingId);

		assertThat(result.get(0).getProcessStatus(), is(MixDecStatus.PROCESSING));
	}

}
