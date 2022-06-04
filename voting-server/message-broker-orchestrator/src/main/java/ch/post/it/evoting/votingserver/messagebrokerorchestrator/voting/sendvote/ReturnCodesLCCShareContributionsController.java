/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator.voting.sendvote;

import static ch.post.it.evoting.domain.SharedQueue.CREATE_LCC_SHARE_REQUEST_PATTERN;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.domain.voting.sendvote.CombinedPartiallyDecryptedEncryptedPCCPayload;
import ch.post.it.evoting.domain.voting.sendvote.LongReturnCodesSharePayload;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.voting.BroadcastProducerService;

@RestController
@RequestMapping("/api/v1/voting/sendvote")
public class ReturnCodesLCCShareContributionsController {

	private final ObjectMapper objectMapper;
	private final BroadcastProducerService broadcastProducerService;

	public ReturnCodesLCCShareContributionsController(
			final ObjectMapper objectMapper,
			final BroadcastProducerService broadcastProducerService) {
		this.objectMapper = objectMapper;
		this.broadcastProducerService = broadcastProducerService;
	}

	@PostMapping("/lccshare/electionevent/{electionEventId}/verificationCardSetId/{verificationCardSetId}/verificationCardId/{verificationCardId}")
	public List<LongReturnCodesSharePayload> getLongChoiceReturnCodesContributions(
			@PathVariable
			final String electionEventId,
			@PathVariable
			final String verificationCardSetId,
			@PathVariable
			final String verificationCardId,
			@RequestBody
			final CombinedPartiallyDecryptedEncryptedPCCPayload combinedPartiallyDecryptedEncryptedPCCPayload)
			throws ExecutionException, InterruptedException, TimeoutException {
		final String contextId = String.join("-", Arrays.asList(electionEventId, verificationCardSetId, verificationCardId));

		return broadcastProducerService.sendMessagesAwaitingNotification(contextId, Context.VOTING_RETURN_CODES_CREATE_LCC_SHARE,
				combinedPartiallyDecryptedEncryptedPCCPayload, CREATE_LCC_SHARE_REQUEST_PATTERN, this::deserializePayload);
	}

	private LongReturnCodesSharePayload deserializePayload(final byte[] payload) {
		try {
			return objectMapper.readValue(payload, LongReturnCodesSharePayload.class);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
