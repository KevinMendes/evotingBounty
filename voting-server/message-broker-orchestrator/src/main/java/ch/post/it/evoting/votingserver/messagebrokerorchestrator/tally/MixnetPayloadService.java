/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.messagebrokerorchestrator.tally;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;
import ch.post.it.evoting.domain.tally.MixDecryptOnlinePayload;

@Service
public class MixnetPayloadService {
	private final ObjectMapper objectMapper;
	private final InitialPayloadRepository initialPayloadRepository;
	private final ShufflePayloadRepository shufflePayloadRepository;

	public MixnetPayloadService(final ObjectMapper objectMapper, final InitialPayloadRepository initialPayloadRepository,
			final ShufflePayloadRepository shufflePayloadRepository) {
		this.objectMapper = objectMapper;
		this.initialPayloadRepository = initialPayloadRepository;
		this.shufflePayloadRepository = shufflePayloadRepository;
	}

	@Transactional
	public void saveShufflePayload(final MixnetShufflePayload mixnetShufflePayload) {

		checkNotNull(mixnetShufflePayload);

		final String electionEventId = mixnetShufflePayload.getElectionEventId();
		final String ballotBoxId = mixnetShufflePayload.getBallotBoxId();
		final int nodeId = mixnetShufflePayload.getNodeId();

		try {
			final byte[] payload = objectMapper.writeValueAsBytes(mixnetShufflePayload);

			final InitialPayloadEntity initialPayloadEntity =
					initialPayloadRepository.findByElectionEventIdAndBallotBoxId(electionEventId, ballotBoxId)
							.orElseThrow(() -> new IllegalStateException("MixnetInitialPayload hasn't been saved before saving MixnetShufflePayload."));
			final ShufflePayloadEntity shufflePayloadEntity =
					new ShufflePayloadEntity(electionEventId, ballotBoxId, nodeId, payload, initialPayloadEntity);

			shufflePayloadRepository.save(shufflePayloadEntity);
		} catch (final JsonProcessingException ex) {
			throw new UncheckedIOException("Failed to process the mixing DTO", ex);
		}
	}

	@Transactional
	public void saveInitialPayload(final MixnetInitialPayload mixnetInitialPayload) {

		checkNotNull(mixnetInitialPayload);

		final String electionEventId = mixnetInitialPayload.getElectionEventId();
		final String ballotBoxId = mixnetInitialPayload.getBallotBoxId();

		try {
			final byte[] initialPayload = objectMapper.writeValueAsBytes(mixnetInitialPayload);

			InitialPayloadEntity initialPayloadEntity = new InitialPayloadEntity(electionEventId, ballotBoxId, initialPayload);

			initialPayloadRepository.save(initialPayloadEntity);
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException("Could not serialize mixnet initial payload", e);
		}
	}

	@Transactional
	public int countMixDecryptOnlinePayloads(final String electionEventId, final String ballotBoxId) {
		return shufflePayloadRepository.countByElectionEventIdAndBallotBoxId(electionEventId, ballotBoxId);
	}

	@Transactional
	public Optional<MixDecryptOnlinePayload> getFinalPayload(final String electionEventId, final String ballotBoxId) {
		final Optional<InitialPayloadEntity> optionalInitialPayloadEntity =
				initialPayloadRepository.findByElectionEventIdAndBallotBoxId(electionEventId, ballotBoxId);

		if (!optionalInitialPayloadEntity.isPresent()){
			return Optional.empty();
		}

		final InitialPayloadEntity initialPayloadEntity = optionalInitialPayloadEntity.get();

		final List<ShufflePayloadEntity> shufflePayloadEntities =
				shufflePayloadRepository.findByElectionEventIdAndBallotBoxId(electionEventId, ballotBoxId).stream()
						.sorted(Comparator.comparingInt(ShufflePayloadEntity::getNodeId))
						.collect(Collectors.toList());

		if (shufflePayloadEntities.size() != 4) {
			return Optional.empty();
		}

		return Optional.of(createMixDecryptOnlinePayload(initialPayloadEntity, shufflePayloadEntities));
	}

	private MixDecryptOnlinePayload createMixDecryptOnlinePayload(final InitialPayloadEntity initialPayloadEntity,
			final List<ShufflePayloadEntity> shufflePayloadEntities) {
		final MixnetInitialPayload mixnetInitialPayload;

		try {
			mixnetInitialPayload = objectMapper.readValue(initialPayloadEntity.getInitialPayload(), MixnetInitialPayload.class);
		} catch (IOException e) {
			throw new UncheckedIOException("Error deserialiazing MixnetInitialPayload", e);
		}

		final List<MixnetShufflePayload> mixnetShufflePayloads =
				shufflePayloadEntities.stream()
						.map(bytes -> {
							try {
								return objectMapper.readValue(bytes.getShufflePayload(), MixnetShufflePayload.class);
							} catch (final IOException e) {
								throw new UncheckedIOException("Error deserialiazing MixnetShufflePayload", e);
							}
						})
						.collect(Collectors.toList());

		final String electionEventId = initialPayloadEntity.getElectionEventId();
		final String ballotBoxId = initialPayloadEntity.getBallotBoxId();

		return new MixDecryptOnlinePayload(electionEventId, ballotBoxId, mixnetInitialPayload, mixnetShufflePayloads);

	}

	@Transactional
	public List<MixnetShufflePayload> getMixnetShufflePayloads(final String electionEventId, final String ballotBoxId) {
		checkNotNull(electionEventId);
		checkNotNull(ballotBoxId);

		return shufflePayloadRepository.findByElectionEventIdAndBallotBoxId(electionEventId, ballotBoxId)
				.stream()
				.map(ShufflePayloadEntity::getShufflePayload)
				.map(bytes -> {
					try {
						return objectMapper.readValue(bytes, MixnetShufflePayload.class);
					} catch (IOException e) {
						throw new UncheckedIOException("Couldn't deserialize a MixnetShufflePayload", e);
					}
				})
				.collect(Collectors.toList());
	}
}