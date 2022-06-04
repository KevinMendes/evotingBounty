/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.messagebrokerorchestrator.tally;

import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.post.it.evoting.domain.tally.MixDecryptOnlinePayload;

@RestController
@RequestMapping("/api/v1/tally/")
public class MixDecryptOnlineController {

	private static final Logger LOGGER = LoggerFactory.getLogger(MixDecryptOnlineController.class);

	private final MixDecryptOnlineProducer mixDecryptOnlineProducer;
	private final MixnetPayloadService mixnetPayloadService;

	public MixDecryptOnlineController(final MixDecryptOnlineProducer mixDecryptOnlineProducer,
			final MixnetPayloadService mixnetPayloadService) {
		this.mixDecryptOnlineProducer = mixDecryptOnlineProducer;
		this.mixnetPayloadService = mixnetPayloadService;
	}

	/**
	 * Start the online mixing of the given ballot box.
	 */
	@PutMapping("/mixonline/electionevent/{electionEventId}/ballotbox/{ballotBoxId}/mix")
	public ResponseEntity<Void> createMixDecryptOnline(
			@PathVariable
			final String electionEventId,
			@PathVariable
			final String ballotBoxId) {
		try {
			LOGGER.info("Mix online [electionEventId: {}, ballotBoxId:{}]", electionEventId, ballotBoxId);

			checkNotNull(electionEventId, "Election event Id must not be null");
			checkNotNull(ballotBoxId, "Ballot Box Id should not be null");

			mixDecryptOnlineProducer.initialSend(electionEventId, ballotBoxId);

			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (Exception ex) {
			LOGGER.error("Error: {}", ex.getMessage(), ex);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Get a MixDecryptOnlinePayload if the mixing is finished.
	 */
	@GetMapping("/mixonline/electionevent/{electionEventId}/ballotbox/{ballotBoxId}/download")
	public ResponseEntity<MixDecryptOnlinePayload> downloadMixDecryptOnline(
			@PathVariable
			final String electionEventId,
			@PathVariable
			final String ballotBoxId) {
		LOGGER.info("Download MixDecryptOnline [electionEventId: {}, ballotBoxId:{}]", electionEventId, ballotBoxId);
		try {

			return mixnetPayloadService.getFinalPayload(electionEventId, ballotBoxId)
					.map(mixDecryptOnlinePayload -> new ResponseEntity<>(mixDecryptOnlinePayload, HttpStatus.OK))
					.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

		} catch (final Exception ex) {
			LOGGER.error("Error: {}", ex.getMessage(), ex);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Check the status of the online mixing for the given ballot box. Statuses are defined by {@code MixDecryptOnlineStatus}.
	 */
	@GetMapping("/mixonline/electionevent/{electionEventId}/ballotbox/{ballotBoxId}/status")
	public ResponseEntity<MixDecryptOnlineStatus> checkMixDecryptOnlineStatus(
			@PathVariable
			final String electionEventId,
			@PathVariable
			final String ballotBoxId) {
		LOGGER.info("Status MixDecryptOnline [electionEventId: {}, ballotBoxId:{}]", electionEventId, ballotBoxId);

		switch (mixnetPayloadService.countMixDecryptOnlinePayloads(electionEventId, ballotBoxId)) {
			case 0:
				return new ResponseEntity<>(MixDecryptOnlineStatus.NOT_STARTED, HttpStatus.OK);
			case 1:
			case 2:
			case 3:
				return new ResponseEntity<>(MixDecryptOnlineStatus.PROCESSING, HttpStatus.OK);
			case 4:
				return new ResponseEntity<>(MixDecryptOnlineStatus.MIXED, HttpStatus.OK);
			default:
				return new ResponseEntity<>(MixDecryptOnlineStatus.ERROR, HttpStatus.OK);

		}
	}
}
