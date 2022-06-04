/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Base64;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;

/**
 * Provide methods to generate ballot boxes for use in SDM tests.
 */
public class SDMTestData {

	public static final String BALLOT_ID = "1c18f7d21dce4ac1aa1f025599bf2cfd";
	public static final String ELECTION_EVENT_ID = "1c18f7d21dce4ac1aa1f025599bf2cfa";
	public static final String BALLOT_BOX_ID = "1c18f7d21dce4ac1aa1f025599bf2cfb";
	public static final String ELECTORAL_AUTHORITY_ID = "electoralAuthorityId";
	public static final String ADMINISTRATION_AUTHORITY_ID = "certId";

	private static final String WRITE_IN_ALPHABET = "12345";
	private static final String ENCRYPTION_PARAMS = "{\"p\":\"16370518994319586760319791526293535327576438646782139419846004180837103527129035954742043590609421369665944746587885814920851694546456891767644945459124422553763416586515339978014154452159687109161090635367600349264934924141746082060353483306855352192358732451955232000593777554431798981574529854314651092086488426390776811367125009551346089319315111509277347117467107914073639456805159094562593954195960531136052208019343392906816001017488051366518122404819967204601427304267380238263913892658950281593755894747339126531018026798982785331079065126375455293409065540731646939808640273393855256230820509217411510058759\",\"q\":\"8185259497159793380159895763146767663788219323391069709923002090418551763564517977371021795304710684832972373293942907460425847273228445883822472729562211276881708293257669989007077226079843554580545317683800174632467462070873041030176741653427676096179366225977616000296888777215899490787264927157325546043244213195388405683562504775673044659657555754638673558733553957036819728402579547281296977097980265568026104009671696453408000508744025683259061202409983602300713652133690119131956946329475140796877947373669563265509013399491392665539532563187727646704532770365823469904320136696927628115410254608705755029379\",\"g\":\"2\"}";

	/**
	 * Generate the ballot box JSON.
	 *
	 * @param encryptionParameters the Zp subgroup
	 */
	public static String generateBallotBoxJson(final String ballotBoxId, final String ballotId, final String electoralAuthorityId, final ZpSubgroup encryptionParameters) {
		final JsonObjectBuilder ballotBoxBuilder = Json.createObjectBuilder();

		ballotBoxBuilder.add(JsonConstants.ID, ballotBoxId);
		ballotBoxBuilder.add(JsonConstants.STATUS, Status.BB_DOWNLOADED.name());
		ballotBoxBuilder.add(JsonConstants.ELECTORAL_AUTHORITY_ID, electoralAuthorityId);

		// Ballot section.
		final JsonObjectBuilder ballotBuilder = Json.createObjectBuilder();
		ballotBuilder.add(JsonConstants.ID, ballotId);
		ballotBoxBuilder.add(JsonConstants.BALLOT, ballotBuilder.build());

		// Encryption parameters section.
		final JsonObjectBuilder encryptionParametersBuilder = Json.createObjectBuilder();
		encryptionParametersBuilder.add(JsonConstants.P, encryptionParameters.getP().toString());
		encryptionParametersBuilder.add(JsonConstants.Q, encryptionParameters.getQ().toString());
		encryptionParametersBuilder.add(JsonConstants.G, encryptionParameters.getG().toString());
		ballotBoxBuilder.add(JsonConstants.ENCRYPTION_PARAMETERS, encryptionParametersBuilder.build());

		final JsonObject ballotBox = ballotBoxBuilder.build();

		return ballotBox.toString();
	}

	/**
	 * Generate the election event JSON.
	 */
	public static String generateElectionEventJson() {
		final JsonObjectBuilder electionEventBuilder = Json.createObjectBuilder();

		final JsonObjectBuilder administrationAuthorityBuilder = Json.createObjectBuilder();
		administrationAuthorityBuilder.add(JsonConstants.ID, ADMINISTRATION_AUTHORITY_ID);
		electionEventBuilder.add(JsonConstants.ADMINISTRATION_AUTHORITY, administrationAuthorityBuilder);

		final JsonObjectBuilder settingsBuilder = Json.createObjectBuilder();
		settingsBuilder
				.add(JsonConstants.WRITE_IN_ALPHABET, Base64.getEncoder().encodeToString(WRITE_IN_ALPHABET.getBytes(Charset.defaultCharset())));
		settingsBuilder.add(JsonConstants.ENCRYPTION_PARAMETERS, Json.createReader(new StringReader(ENCRYPTION_PARAMS)).readObject());
		electionEventBuilder.add(JsonConstants.SETTINGS, settingsBuilder);

		return electionEventBuilder.build().toString();
	}

}
