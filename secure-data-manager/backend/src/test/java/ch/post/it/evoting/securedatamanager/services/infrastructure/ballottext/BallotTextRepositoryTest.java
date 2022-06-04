/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.ballottext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonObject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.orientechnologies.common.exception.OException;

import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseFixture;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;

/**
 * Tests of {@link BallotTextRepository}.
 */
class BallotTextRepositoryTest {

	private DatabaseFixture fixture;
	private BallotTextRepository repository;

	@BeforeEach
	void setUp() throws OException, IOException {
		fixture = new DatabaseFixture(getClass());
		fixture.setUp();
		repository = new BallotTextRepository(fixture.databaseManager());
		repository.initialize();
		final URL resource = getClass().getResource(getClass().getSimpleName() + ".json");
		fixture.createDocuments(repository.entityName(), resource);
	}

	@AfterEach
	void tearDown() {
		fixture.tearDown();
	}

	@Test
	void testUpdateSignedBallotText() {
		repository.updateSignedBallotText("1dde9338ca074c6389407caba5c28d26en-GB", "signedBallotText");
		final JsonObject object = JsonUtils.getJsonObject(repository.find("1dde9338ca074c6389407caba5c28d26en-GB"));
		assertEquals("signedBallotText", object.getString(JsonConstants.SIGNED_OBJECT));
		assertEquals(Status.SIGNED.name(), object.getString(JsonConstants.STATUS));
	}

	@Test
	void testUpdateSignedBallotTextNotFound() {
		assertThrows(DatabaseException.class, () -> repository.updateSignedBallotText("unknownBallotText", "signedBallotText"));
	}

	@Test
	void testListSignatures() {
		final Map<String, Object> criteria = new HashMap<>();
		criteria.put("ballot.id", "1dde9338ca074c6389407caba5c28d26");
		criteria.put(JsonConstants.LOCALE, "fr-CH");
		final List<String> signatures = repository.listSignatures(criteria);
		assertEquals(1, signatures.size());
		assertEquals("eyJhbGciOiJQUzI1NiJ9.eyJvYmplY3RUb1NpZ24iOiJ7XCJiYWxsb3RcIjp7XCJpZ"
						+ "FwiOlwiMWRkZTkzMzhjYTA3NGM2Mzg5NDA3Y2FiYTVjMjhkMjZcIn0sXCJsb2Nhb"
						+ "GVcIjpcImZyLUNIXCIsXCJzdGF0dXNcIjpcIkxPQ0tFRFwiLFwidGV4dHNcIjp7X"
						+ "CIxZGRlOTMzOGNhMDc0YzYzODk0MDdjYWJhNWMyOGQyNlwiOntcInRpdGxlXCI6X"
						+ "CJUZXN0IGUyZSAyMDE2MTAxMyBDQyAzNDZcIixcImRlc2NyaXB0aW9uXCI6XCIwN"
						+ "FwifX0sXCJpZFwiOlwiMWRkZTkzMzhjYTA3NGM2Mzg5NDA3Y2FiYTVjMjhkMjZmc"
						+ "i1DSFwiLFwiZGV0YWlsc1wiOlwiMDkvMDEvMjAxNyAxNTo0NToxMVwiLFwic3luY"
						+ "2hyb25pemVkXCI6XCJ0cnVlXCJ9In0.wbK1ht8AumfltCx_BVTcs9oZTfoLjUw0G"
						+ "1N3zmG8rloXFFKXEsClI1z6YwZJ1g-uRdRyfZtSfP7mO3OKxl_B8zMAf1n-HRchT"
						+ "1_PVgP6siWIO5hJ3KFEA5Ljby9IbZwVFwHmcht-4XurKVtmtdy0DBMQV3Zh5PCwq"
						+ "NwIOxix0v_Pu6iHXg0-7N3UO0qM3GOjul82skxRFXDU3szpEEEy14yB7JSSrBwht"
						+ "GTZR-1rTM84r0v3QzmY8PLS1QIfj1h7IjUX1hbcffsNrADh3rQQZwKOO3tcgyBcy" + "9MeOtrr5FIy61_2h_dJxnh4WCQkoWXd7J3setyAJzuvi1vmazo4NA",
				signatures.get(0));
	}

	@Test
	void testListSignaturesNotFound() {
		final Map<String, Object> criteria = new HashMap<>();
		criteria.put("ballot.id", "1dde9338ca074c6389407caba5c28d26");
		criteria.put(JsonConstants.LOCALE, "ru-RU");
		assertTrue(repository.listSignatures(criteria).isEmpty());
	}
}
