/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.config;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.votingserver.commons.messaging.Destination;
import ch.post.it.evoting.votingserver.commons.messaging.Queue;

// This Sonar's rule declares that 'Mutable fields should not be "public static"'.
// Public static fields declared in this class are instantiated here and are not modified elsewhere so they are effectively final.
// Therefore, we choose to suppress this rule.
@SuppressWarnings("squid:S2386")
public final class QueuesConfig {
	public static final Queue[] CHOICE_CODES_KEY_GENERATION_REQ_QUEUES;

	public static final Queue[] CHOICE_CODES_KEY_GENERATION_RES_QUEUES;

	public static final Queue[] GENERATION_CONTRIBUTIONS_REQ_QUEUES;

	public static final Queue[] GENERATION_CONTRIBUTIONS_RES_QUEUES;

	public static final Queue[] MIX_DEC_KEY_GENERATION_REQ_QUEUES;

	public static final Queue[] MIX_DEC_KEY_GENERATION_RES_QUEUES;

	public static final Queue[] MIX_DEC_COMPUTATION_REQ_QUEUES;

	public static final Queue[] MIX_DEC_COMPUTATION_RES_QUEUES;

	private static final String CC_QUEUE_NAMES_PROPERTY = "CC_QUEUE_NAMES";

	private static final String GENERATION_COMPUTATION = "cg-comp";

	private static final String GENERATION_KEYGENERATION = "cg-keygen";

	private static final String MIXDEC_COMPUTATION = "md-mixdec";

	private static final String MIXDEC_KEYGENERATION = "md-keygen";

	private static final String REQUEST_QUEUE = "req";

	private static final String RESPONSE_QUEUE = "res";

	static {
		String ccQueueNames = System.getenv(CC_QUEUE_NAMES_PROPERTY);
		if (ccQueueNames != null) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode jsonNode = mapper.readTree(ccQueueNames);

				Comparator<Queue> comparator = Comparator.comparing(Destination::name);

				SortedSet<Queue> keyGenerationRequestQueues = new TreeSet<>(comparator);
				SortedSet<Queue> keyGenerationResponseQueues = new TreeSet<>(comparator);
				SortedSet<Queue> generationComputeContributionsRequestQueues = new TreeSet<>(comparator);
				SortedSet<Queue> generationComputeContributionsResponseQueues = new TreeSet<>(comparator);
				SortedSet<Queue> mixDecKeyGenerationRequestQueues = new TreeSet<>(comparator);
				SortedSet<Queue> mixDecKeyGenerationResponseQueues = new TreeSet<>(comparator);
				SortedSet<Queue> mixDecComputationRequestQueues = new TreeSet<>(comparator);
				SortedSet<Queue> mixDecComputationResponseQueues = new TreeSet<>(comparator);

				for (JsonNode nodeField : iteratorToIterable(jsonNode.elements())) {
					for (Map.Entry<String, JsonNode> action : iteratorToIterable(nodeField.fields())) {
						switch (action.getKey()) {
						case GENERATION_KEYGENERATION:
							keyGenerationRequestQueues.add(getRequestQueue(action.getValue()));
							keyGenerationResponseQueues.add(getResponseQueue(action.getValue()));
							break;

						case GENERATION_COMPUTATION:
							generationComputeContributionsRequestQueues.add(getRequestQueue(action.getValue()));
							generationComputeContributionsResponseQueues.add(getResponseQueue(action.getValue()));
							break;

						case MIXDEC_COMPUTATION:
							mixDecComputationRequestQueues.add(getRequestQueue(action.getValue()));
							mixDecComputationResponseQueues.add(getResponseQueue(action.getValue()));
							break;

						case MIXDEC_KEYGENERATION:
							mixDecKeyGenerationRequestQueues.add(getRequestQueue(action.getValue()));
							mixDecKeyGenerationResponseQueues.add(getResponseQueue(action.getValue()));
							break;

						default:
							throw new IllegalArgumentException(
									String.format("Unknown action %s found when parsing %s property", action.getKey(), CC_QUEUE_NAMES_PROPERTY));
						}
					}
				}

				CHOICE_CODES_KEY_GENERATION_REQ_QUEUES = keyGenerationRequestQueues.toArray(new Queue[keyGenerationRequestQueues.size()]);

				CHOICE_CODES_KEY_GENERATION_RES_QUEUES = keyGenerationResponseQueues.toArray(new Queue[keyGenerationResponseQueues.size()]);

				GENERATION_CONTRIBUTIONS_REQ_QUEUES = generationComputeContributionsRequestQueues
						.toArray(new Queue[generationComputeContributionsRequestQueues.size()]);

				GENERATION_CONTRIBUTIONS_RES_QUEUES = generationComputeContributionsResponseQueues
						.toArray(new Queue[generationComputeContributionsResponseQueues.size()]);

				MIX_DEC_KEY_GENERATION_REQ_QUEUES = mixDecKeyGenerationRequestQueues.toArray(new Queue[mixDecKeyGenerationRequestQueues.size()]);

				MIX_DEC_KEY_GENERATION_RES_QUEUES = mixDecKeyGenerationResponseQueues.toArray(new Queue[mixDecKeyGenerationResponseQueues.size()]);

				MIX_DEC_COMPUTATION_REQ_QUEUES = mixDecComputationRequestQueues.toArray(new Queue[mixDecComputationRequestQueues.size()]);

				MIX_DEC_COMPUTATION_RES_QUEUES = mixDecComputationResponseQueues.toArray(new Queue[mixDecComputationResponseQueues.size()]);

			} catch (NullPointerException | JsonProcessingException e) {
				throw new IllegalArgumentException(String.format("Error parsing %s property", CC_QUEUE_NAMES_PROPERTY), e);
			}
		} else {
			throw new IllegalArgumentException(String.format("System property %s is missing", CC_QUEUE_NAMES_PROPERTY));
		}
	}

	private QueuesConfig() {
	}

	private static Queue getRequestQueue(JsonNode action) {
		return getQueue(action, REQUEST_QUEUE);
	}

	private static Queue getResponseQueue(JsonNode action) {
		return getQueue(action, RESPONSE_QUEUE);
	}

	private static Queue getQueue(JsonNode action, String type) {
		return new Queue(action.get(type).asText());
	}

	private static <T> Iterable<T> iteratorToIterable(Iterator<T> iterator) {
		return () -> iterator;
	}
}
