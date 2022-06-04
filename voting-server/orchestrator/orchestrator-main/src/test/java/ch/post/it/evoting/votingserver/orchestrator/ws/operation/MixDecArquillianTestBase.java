/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.ws.operation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import ch.post.it.evoting.domain.election.model.EntityId;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingException;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model.BallotBoxStatus;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model.MixDecBallotBoxStatus;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model.MixDecStatus;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services.MixDecBallotBoxService;
import ch.post.it.evoting.votingserver.orchestrator.ws.config.MockMixingNode;
import ch.post.it.evoting.votingserver.orchestrator.ws.config.SystemPropertiesLoader;

public abstract class MixDecArquillianTestBase {

	static final String ELECTION_EVENT_ID = "EE";
	static final String BALLOT_BOX_ID = "BB1";
	private static final String TENANT_ID = "TENANT";
	private static final String TRACK_ID_HEADER = "X-Request-ID";
	private static final String TEST_TRACK_ID = "TestTrackingId";
	private static final String TENANT_ID_PLACEHOLDER = "{tenantId}";
	private static final String ELECTION_EVENT_ID_PLACEHOLDER = "{electionEventId}";
	private static final String BALLOT_BOX_ID_PLACEHOLDER = "{ballotBoxId}";
	private static final SystemPropertiesLoader spl = new SystemPropertiesLoader();
	static ControlComponentNodeLayout nodeLayout;
	private final List<MockMixingNode> mixingNodes = new ArrayList<>();
	@PersistenceContext(unitName = "persistenceUnitJdbc")
	private EntityManager entityManager;
	@Resource
	private UserTransaction userTransaction;
	@Inject
	private MixDecBallotBoxService mixDecBallotBoxService;

	@BeforeClass
	public static void init() {
		spl.setProperties();

		nodeLayout = createNodeLayout();
	}

	/**
	 * @return the control component infrastructure the tests will use.
	 */
	private static ControlComponentNodeLayout createNodeLayout() {
		ControlComponentNodeLayout nodeLayout = new ControlComponentNodeLayout();
		nodeLayout.addNode("m1", "md-mixdec-m1-req", "md-mixdec-m1-res");
		nodeLayout.addNode("m2", "md-mixdec-m2-req", "md-mixdec-m2-res");
		nodeLayout.addNode("m3", "md-mixdec-m3-req", "md-mixdec-m3-res");

		return nodeLayout;
	}

	@Before
	public void cleanDatabase() throws Exception {
		userTransaction.begin();
		Query query = entityManager.createQuery("DELETE FROM " + MixDecBallotBoxStatus.class.getSimpleName());
		query.executeUpdate();
		userTransaction.commit();
	}

	@Before
	public void initTest() throws MessagingException {
		mixDecBallotBoxService.startup();
	}

	@After
	public void endTest() throws MessagingException {
		mixDecBallotBoxService.shutdown();
	}

	/**
	 * Performs the request to start mixing a ballot box
	 *
	 * @param webTarget    the web target provided by Arquillian
	 * @param ballotBoxIds the identifier of the ballot boxes to be mixed
	 * @return the ballot box statuses
	 */
	List<BallotBoxStatus> startMixing(ResteasyWebTarget webTarget, String... ballotBoxIds) {
		// Prepare the request URL
		String mixDecStartURL = MixDecBallotBoxesResource.RESOURCE_PATH.replace(TENANT_ID_PLACEHOLDER, TENANT_ID)
				.replace(ELECTION_EVENT_ID_PLACEHOLDER, ELECTION_EVENT_ID);

		// Perform the request
		Response response = webTarget.path(mixDecStartURL).request(MediaType.APPLICATION_JSON).header(TRACK_ID_HEADER, TEST_TRACK_ID)
				.post(Entity.entity(buildEntityIdList(ballotBoxIds), MediaType.APPLICATION_JSON));

		// Get the list of accepted ballot boxes.
		List<LinkedHashMap> results = response.readEntity(List.class);
		List<BallotBoxStatus> bbStatuses = results.stream().map(result -> {
			BallotBoxStatus bbStatus = new BallotBoxStatus();
			bbStatus.setProcessStatus(MixDecStatus.valueOf((String) result.get("processStatus")));
			bbStatus.setErrorMessage((String) result.get("errorMessage"));
			bbStatus.setBallotBoxId((String) result.get("ballotBoxId"));

			return bbStatus;
		}).collect(Collectors.toList());

		// Ensure the request worked.
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

		// Free the connection for the next request.
		response.close();

		return bbStatuses;
	}

	/**
	 * Performs the request to retrieve the status of a ballot box.
	 * <p>
	 * Please note that the connection must be closed manually, so that the response entity is available in the caller context.
	 *
	 * @param webTarget   the web target provided by Arquillian
	 * @param ballotBoxId the identifier of the ballot box in question
	 * @return the response
	 */
	String getBallotBoxStatus(ResteasyWebTarget webTarget, String ballotBoxId) {
		// Prepare the request URL
		String mixDecStatusURL = (MixDecBallotBoxResource.RESOURCE_PATH + "/" + MixDecBallotBoxResource.PATH_STATUS)
				.replace(TENANT_ID_PLACEHOLDER, TENANT_ID).replace(ELECTION_EVENT_ID_PLACEHOLDER, ELECTION_EVENT_ID)
				.replace(BALLOT_BOX_ID_PLACEHOLDER, ballotBoxId);

		Response response = webTarget.path(mixDecStatusURL).request(MediaType.APPLICATION_JSON).header(TRACK_ID_HEADER, TEST_TRACK_ID).get();

		// Ensure the request worked.
		assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

		// Get the status string from the entity.
		String ballotBoxStatus;
		try {
			ballotBoxStatus = (String) response.readEntity(Map.class).values().toArray()[0];
		} catch (Exception e) {
			ballotBoxStatus = null;
		}

		// Free the connection for the next request.
		response.close();

		return ballotBoxStatus;
	}

	/**
	 * Creates a list of entity IDs from a number of ballot box IDs.
	 *
	 * @param ballotBoxIds the IDs to add to the list
	 * @return a list of entity IDs
	 */
	private List<EntityId> buildEntityIdList(String... ballotBoxIds) {
		List<EntityId> entityIds = new ArrayList<>(ballotBoxIds.length);

		Arrays.stream(ballotBoxIds).forEach(id -> {
			EntityId entityId = new EntityId();
			entityId.setId(id);
			entityIds.add(entityId);
		});

		return entityIds;
	}

	/**
	 * A class that stores a control component node layout, along with its queues, in a conveniently accessible structure.
	 */
	static class ControlComponentNodeLayout {
		private final Map<String, Map<QueueType, String>> map;

		public ControlComponentNodeLayout() {
			map = new HashMap<>();
		}

		public void addNode(String nodeName, String requestQueueName, String responseQueueName) {
			Map<QueueType, String> queues = new HashMap<>();
			queues.put(QueueType.REQUEST, requestQueueName);
			queues.put(QueueType.RESPONSE, responseQueueName);
			map.put(nodeName, queues);
		}

		public Set<String> getNodeNames() {
			return map.keySet();
		}

		public String getResponseQueueName(String nodeName) {
			return map.get(nodeName).get(QueueType.RESPONSE);
		}

		public String getRequestQueueName(String nodeName) {
			return map.get(nodeName).get(QueueType.REQUEST);
		}

		private enum QueueType {
			REQUEST,
			RESPONSE
		}
	}
}
