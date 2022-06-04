/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure;

/**
 * Constants used for managing json formats.
 */
public final class JsonConstants {

	public static final String ELECTION_EVENTS = "electionEvents";

	public static final String VOTING_CARD_SETS = "votingCardSets";

	public static final String ELECTORAL_AUTHORITIES = "electoralAuthorities";

	public static final String ADMINISTRATION_AUTHORITIES = "administrationAuthorities";

	public static final String ADMINISTRATION_AUTHORITY = "administrationAuthority";

	public static final String ELECTORAL_AUTHORITY = "electoralAuthority";

	public static final String P = "p";

	public static final String Q = "q";

	public static final String G = "g";

	public static final String ENCRYPTION_PARAMETERS = "encryptionParameters";

	public static final String ENCRYPTION_PARAMETERS_FILE = "encryptionParametersFile";

	public static final String REPRESENTATIONS_FILE = "representationsFile";

	public static final String REPRESENTATIONS_SIGNATURE_FILE = "representationsSignatureFile";

	public static final String BALLOT_BOXES = "ballotBoxes";

	public static final String SETTINGS = "settings";

	public static final String BALLOTS = "ballots";

	public static final String BALLOT = "ballot";

	public static final String ID = "id";

	public static final String STATUS = "status";

	public static final String HOST_MODE = "hostMode";

	public static final String TEST = "test";

	public static final String ELECTION_EVENT = "electionEvent";

	public static final String TEXTS = "texts";

	public static final String TRANSLATIONS = "translations";

	public static final String BALLOTTEXT = "ballottext";

	public static final String LOCALE = "locale";

	public static final String RESULT = "result";

	public static final String DETAILS = "details";

	public static final String BALLOT_BOX = "ballotBox";

	public static final String VERIFICATION_CARD_SET_ID = "verificationCardSetId";

	public static final String ELECTORAL_AUTHORITY_ID = "electoralAuthorityId";

	public static final String ALIAS = "alias";

	public static final String BALLOT_BOX_ALIAS = "ballotBoxAlias";

	public static final String GRACE_PERIOD = "gracePeriod";

	public static final String ELECTION_EVENT_DOT_ID = "electionEvent.id";

	public static final String BALLOT_ID = "ballot.id";

	public static final String ELECTORAL_AUTHORITY_DOT_ID = "electoralAuthority.id";

	public static final String SYNCHRONIZED = "synchronized";

	public static final String EMPTY_OBJECT = "{}";

	public static final String RESULT_EMPTY = "{\"result\":[]}";

	public static final String DATE_TO = "dateTo";

	public static final String DATE_FROM = "dateFrom";

	public static final String CERTIFICATES_VALIDITY_PERIOD = "certificatesValidityPeriod";

	public static final String SIGNED_OBJECT = "signedObject";

	public static final java.lang.String BALLOT_ALIAS = "ballotAlias";

	public static final String WRITE_IN_ALPHABET = "writeInAlphabet";

	public static final String NUMBER_OF_VOTING_CARDS_TO_GENERATE = "numberOfVotingCardsToGenerate";

	private JsonConstants() {
		// Avoids instantiation.
	}
}
