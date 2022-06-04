/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.commons;

import com.google.common.collect.ImmutableList;

public final class Constants {

	public static final String HUNDRED = "100";

	// ////////////////////////////////////
	//
	// characters
	//
	// ////////////////////////////////////

	public static final String COMMA = ",";

	public static final String EMPTY = "";

	public static final String SEMICOLON = ";";

	// ////////////////////////////////////
	//
	// file extensions
	//
	// ////////////////////////////////////

	public static final String CSV = ".csv";

	public static final String CSV_GLOB = "*" + CSV;

	public static final String JSON = ".json";

	public static final String KEY = ".key";

	public static final String PEM = ".pem";

	public static final String SEPARATOR_BEFORE_CHUNK_ID = ".";

	public static final String SIGN = ".sign";

	public static final String SKS = ".sks";

	// ////////////////////////////////////
	//
	// filenames
	//
	// ////////////////////////////////////

	public static final String CONFIG_DIR_NAME_BALLOTBOX_JSON = "ballotBox.json";

	public static final String CONFIG_FILE_NAME_AUDITABLE_VOTES = "auditableVotes.csv";

	public static final String CONFIG_FILE_NAME_AUTH_CONTEXT_DATA = "authenticationContextData.json";

	public static final String CONFIG_FILE_NAME_AUTH_VOTER_DATA = "authenticationVoterData.json";

	public static final String CONFIG_FILE_NAME_AUTHORITIESCA_PEM = "authoritiesca.pem";

	public static final String CONFIG_FILE_NAME_BALLOT_JSON = "ballot.json";

	public static final String CONFIG_FILE_NAME_ELECTION_INFORMATION_DOWNLOADED_BALLOT_BOX = "downloadedBallotBox.csv";

	public static final String CONFIG_FILE_NAME_ELECTION_OPTION_REPRESENTATIONS = "electionOptionRepresentations.csv";

	public static final String CONFIG_FILE_NAME_ELECTIONS_CONFIG_JSON = "elections_config.json";

	public static final String CONFIG_FILE_NAME_ELECTIONS_CONFIG_JSON_SIGN = CONFIG_FILE_NAME_ELECTIONS_CONFIG_JSON + ".p7";

	public static final String CONFIG_FILE_NAME_ENCRYPTION_PARAMETERS_JSON = "encryptionParameters.json";

	public static final String CONFIG_FILE_NAME_ENCRYPTION_PARAMETERS_SIGN_JSON = "encryptionParameters.json.sign";

	public static final String CONFIG_FILE_NAME_PLATFORM_ROOT_CA = "platformRootCA.pem";

	public static final String CONFIG_FILE_NAME_PREFIX_CHOICE_CODE_GENERATION_REQUEST_PAYLOAD = "choiceCodeGenerationRequestPayload.";

	public static final String CONFIG_FILE_NAME_PREFIX_CONTROL_COMPONENT_PUBLIC_KEYS_PAYLOAD = "controlComponentPublicKeysPayload.";

	public static final String CONFIG_FILE_NAME_ELECTION_EVENT_CONTEXT_PAYLOAD = "electionEventContextPayload.json";

	public static final String CONFIG_FILE_NAME_REPRESENTATIONS_CSV = "primes.csv";

	public static final String CONFIG_FILE_NAME_REPRESENTATIONS_CSV_SIGN = "primes.csv.p7";

	public static final String CONFIG_FILE_NAME_CONFIGURATION_ANONYMIZED = "configuration-anonymized.xml";

	public static final String CONFIG_FILE_NAME_CONFIGURATION_ANONYMIZED_SIGN = "configuration-anonymized.xml.p7";

	public static final String CONFIG_FILE_NAME_SIGNED_AUTH_CONTEXT_DATA = CONFIG_FILE_NAME_AUTH_CONTEXT_DATA + SIGN;

	public static final String CONFIG_FILE_NAME_SIGNED_AUTH_VOTER_DATA = CONFIG_FILE_NAME_AUTH_VOTER_DATA + SIGN;

	public static final String CONFIG_FILE_NAME_SIGNED_BALLOTBOX_JSON = CONFIG_DIR_NAME_BALLOTBOX_JSON + SIGN;

	public static final String CONFIG_FILE_NAME_SUFFIX_CHOICE_CODE_GENERATION_REQUEST_PAYLOAD = ".json";

	public static final String CONFIG_FILE_NAME_TENANT_CA_PATTERN = "tenant-%s-CA.pem";

	public static final String CONFIG_FILE_NAME_TRUSTED_CA_PEM = "integrationCA.pem";

	public static final String CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM = "trustedChain.pem";

	public static final String CONFIG_FILE_NAME_VERIFICATION_CONTEXT_DATA = "voteVerificationContextData.json";

	public static final String CONFIG_FILE_NAME_VERIFICATIONSET_DATA = "verificationCardSetData.json";

	public static final String CONFIG_VERIFICATION_CARDS_KEY_PAIR_DIRECTORY = "verificationCardsKeyPairs";

	public static final String CONFIG_BALLOT_CASTING_KEYS_DIRECTORY = "ballotCastingKeys";

	public static final String CREDENTIAL_SIGNER_SKS_FILENAME = "credentialsca.sks";

	public static final String DBDUMP_FILE_NAME = "db_dump.json";

	public static final String DBDUMP_SIGNATURE_FILE_NAME = DBDUMP_FILE_NAME + ".p7";

	public static final String INTEGRATION_KEYSTORE_ONLINE_FILE = "integration_online.p12";

	public static final String KEYS_CONFIG_FILENAME = "keys_config.json";

	public static final String PW_TXT = "passwords.txt";

	public static final String SERVICES_SIGNER_SKS_FILENAME = "servicesca.sks";

	public static final String CONFIG_FILE_NAME_SIGNED_VERIFICATION_CONTEXT_DATA = CONFIG_FILE_NAME_VERIFICATION_CONTEXT_DATA + SIGN;

	public static final String SETUP_SECRET_KEY_FILE_NAME = "setupSecretKey.json";

	public static final String SETUP_KEY_PAIR_FILE_NAME = "setupKeyPair.json";

	// ////////////////////////////////////
	//
	// directories
	//
	// ////////////////////////////////////

	public static final String CONFIG_DIR_NAME = "config";

	public static final String CONFIG_DIR_NAME_AUTHENTICATION = "authentication";

	public static final String CONFIG_DIR_NAME_BALLOTBOXES = "ballotBoxes";

	public static final String CONFIG_DIR_NAME_BALLOTS = "ballots";

	public static final String CONFIG_DIR_NAME_CUSTOMER = "CUSTOMER";

	public static final String CONFIG_DIR_NAME_ELECTIONINFORMATION = "electionInformation";

	public static final String CONFIG_DIR_NAME_OFFLINE = "OFFLINE";

	public static final String CONFIG_DIR_NAME_ONLINE = "ONLINE";

	public static final String CONFIG_DIR_NAME_OUTPUT = "output";

	public static final String CONFIG_DIR_NAME_INPUT = "input";

	public static final String CONFIG_DIR_NAME_PRINTING = "printing";

	public static final String CONFIG_DIR_NAME_VOTERMATERIAL = "voterMaterial";

	public static final String CONFIG_DIR_NAME_VOTEVERIFICATION = "voteVerification";

	public static final String CONFIG_FILE_EXTENDED_AUTHENTICATION_DATA = "extendedAuthentication";

	public static final String CSR_FOLDER = "csr";

	public static final String SDM_CONFIG_DIR_NAME = "sdmConfig";

	public static final String SDM_DIR_NAME = "sdm";

	public static final String SDM_LANGS_DIR_NAME = "langs";

	public static final String SYSTEM_TENANT_KEYS = SDM_DIR_NAME + "/systemKeys";

	public static final String CONFIG_FILES_BASE_DIR = SDM_DIR_NAME + "/config";

	public static final String INTEGRATION_FILES_BASE_DIR = SDM_DIR_NAME + "/integration";

	public static final String INTEGRATION_DIR_NAME_ELECTION_EVENTS = "electionEvents";

	public static final String INTEGRATION_DIR_NAME_OUTPUT = "output";

	public static final String INTEGRATION_DIR_NAME_INPUT = "input";

	// ////////////////////////////////////
	//
	// lengths
	//
	// ////////////////////////////////////

	public static final int NUM_DIGITS_BALLOT_CASTING_KEY = 9;

	public static final int SMART_CARD_LABEL_MAX_LENGTH = 32;

	public static final int SVK_LENGTH = 24;

	public static final String SVK_ALPHABET = "23456789abcdefghijkmnpqrstuvwxyz";

	public static final int NUMBER_CCG = 4;

	// ////////////////////////////////////
	//
	// tags and miscellaneous constants
	//
	// ////////////////////////////////////

	public static final String ADMIN_ID = "adminID";

	public static final String ADMIN_BOARD_ID = "adminBoardId";

	public static final String AUTH_ID = "authid";

	public static final String AUTH_PW = "authpassword";

	public static final String BALLOT_BOX_ID = "ballotBoxId";

	public static final String BALLOT_ID = "ballotId";

	public static final String BASE_PATH = "basePath";

	public static final String CERT_CN = "cert_cn";

	public static final String CERT_SN = "cert_sn";

	public static final String CHOICE_CODES_GENERATION_REQUEST_FILENAME = "choiceCodeGenerationRequestPayload";

	public static final String CONFIG_FILE_NAME_CODES_MAPPING = "codesMappingTablesContextData";

	public static final String CONFIG_FILE_NAME_CREDENTIAL_DATA = "credentialData";

	public static final String CONFIG_FILE_NAME_NODE_CONTRIBUTIONS = "nodeContributions";

	public static final String CONFIG_FILE_NAME_VOTER_INFORMATION = "voterInformation";

	public static final String CONFIG_FILE_VERIFICATION_CARD_DATA = "verificationCardData";

	public static final String CONFIGURATION_ADMINBOARD_CA_JSON_TAG = "adminboard";

	public static final String CONFIGURATION_AUTH_TOKEN_SIGNER_JSON_TAG = "authTokenSigner";

	public static final String CONFIGURATION_AUTHORITIES_CA_JSON_TAG = "authoritiesca";

	public static final String CONFIGURATION_BALLOTBOX_JSON_TAG = "ballotBox";

	public static final String CONFIGURATION_CREDENTIALS_CA_JSON_TAG = "credentialsca";

	public static final String CONFIGURATION_CREDENTIALS_CA_PRIVATE_KEY_JSON_TAG = "privateKey";

	public static final String CONFIGURATION_ELECTION_CA_JSON_TAG = "electioneventca";

	public static final String CONFIGURATION_SERVICES_CA_JSON_TAG = "servicesca";

	public static final String CONFIGURATION_SERVICES_CA_PRIVATE_KEY_JSON_TAG = "privateKey";

	public static final String CONFIGURATION_VERIFICATION_CARD_PRIVATE_KEY_JSON_TAG = "privateKey";

	public static final String CREDENTIAL_ID = "credentialid";

	public static final String CHUNK_ID_PARAM = "chunkId";

	public static final String CHUNK_COUNT_PARAM = "chunkCount";

	public static final String ELECTION_EVENT_ID = "electionEventId";

	public static final String ELECTORAL_AUTHORITY_ID = "electoralAuthorityId";

	public static final String ERR_DESC = "err_desc";

	public static final String ERROR_COUNT = "errorCount";

	public static final String EXTENDED_AUTHENTICATION = "extendedAuthentication";

	public static final String GENERATED_VC_COUNT = "generatedVotingCardCount";

	public static final String JOB_ID_CANNOT_BE_NULL = "jobId cannot be null";

	public static final String JOB_INSTANCE_ID = "jobInstanceId";

	public static final String KEYSTORE_PIN = "keystorepin";

	public static final String MESSAGE_DIGEST_ALGORITHM = "SHA-256";

	public static final String NUMBER_VOTING_CARDS = "numberVotingCards";

	public static final String PLATFORM_ROOT_CA_CERTIFICATE = "platformRootCACertificate";

	public static final String PRINTING_DATA = "printingData";

	public static final String PROVIDED_CHALLENGE = "aliases";

	public static final String SALT_CREDENTIAL_ID = "saltCredentialId";

	public static final String SALT_KEYSTORE_SYM_ENC_KEY = "saltKeystoreSymmetricEncryptionKey";

	public static final String SAVING_SIGNATURE = "Saving signature";

	public static final String SIGNING_FILE = "Signing file";

	public static final String TENANT_ID = "tenantId";

	public static final String VALIDITY_PERIOD_END = "validityPeriodEnd";

	public static final String VALIDITY_PERIOD_START = "validityPeriodStart";

	public static final String VERIFC_ID = "verifc_id";

	public static final String VERIFCS_ID = "verifcs_id";

	public static final String VERIFICATION_CARD_SET_ID = "verificationCardSetID";

	public static final String VERIFICATION_CARD_SET_ID_PARAM = "verificationCardSetId";

	public static final String VOTE_VERIFICATION_CONTEXT_DATA = "voteVerificationContextData";

	public static final String VOTING_CARD_SET_GENERATION = "votingcardset-generation";

	public static final String VOTING_CARD_SET_ID = "votingCardSetId";

	public static final String VOTING_CARD_SET_NAME = "votingCardSetName";

	//////////////
	///
	/// OTHER CONSTANTS
	///
	///////////////////

	public static final int BASE16_ID_LENGTH = 32;

	public static final int KEYSTORE_PW_LENGTH = 26;

	public static final int PBKDF2_MIN_EXTRA_PARAM_LENGTH = 16;

	public static final int RANDOM_SALT_LENGTH = 32;

	public static final String ADMINISTRATION_BOARD_LABEL = "administrationBoard";

	public static final String MINIMUM_THRESHOLD = "minimumThreshold";

	public static final String MIX_DEC_KEY_LABEL = "mixDecKey";

	public static final String NULL_ELECTION_EVENT_ID = "";

	public static final ImmutableList<Integer> NODE_IDS = ImmutableList.of(1, 2, 3, 4);

	// ////////////////////////////////////
	//
	// characters
	//
	// ////////////////////////////////////

	public static final String BALLOT_BOX_PATH = "mixdec/tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}";

	public static final String BALLOT_BOXES_PATH = "mixdec/tenant/{tenantId}/electionevent/{electionEventId}/ballotboxes";

	private Constants() {
	}

}
