/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.ui;

/**
 * Class for holding constants used in the rest web services.
 */
public final class Constants {

	/**
	 * The request id query parameter for logging purpose.
	 */
	public static final String PARAMETER_X_REQUEST_ID = "X-Request-ID";

	// The name of the parameter value tenant id.
	public static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	// The name of the parameter value voting card id.
	public static final String PARAMETER_VALUE_VOTING_CARD_ID = "votingCardId";

	// The name of the parameter value election event id.
	public static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	// The name of the parameter value election event id.
	public static final String PARAMETER_VALUE_AUTHENTICATION_TOKEN = "authenticationToken";

	public static final String PARAMETER_VALUE_BALLOT_ID = "ballotId";

	public static final String PARAMETER_VALUE_ADMIN_BOARD_ID = "adminBoardId";

	public static final String PARAMETER_X_FORWARDED_FOR = "X-Forwarded-For";

	public static final String PARAMETER_PATH_ELECTIONEVENTDATA = "pathElectioneventdata";

	public static final String PARAMETER_PATH_TENANT_DATA = "pathTenantData";

	public static final String PARAMETER_PATH_CERTIFICATE_DATA = "pathCertificateData";

	public static final String PARAMETER_VALUE_CERTIFICATE_NAME = "certificateName";

	public static final String PARAMETER_VALUE_BALLOT_BOX_ID = "ballotBoxId";

	public static final String PARAMETER_VALUE_ELECTORAL_AUTHORITY_ID = "electoralAuthorityId";

	public static final String PARAMETER_PATH_BALLOTBOXES = "pathBallotboxes";

	public static final String PARAMETER_PATH_BALLOTDATA = "pathBallotdata";

	public static final String PARAMETER_PATH_BALLOTBOXDATA = "pathBallotboxdata";

	public static final String PARAMETER_PATH_ELECTORALDATA = "pathElectoraldata";

	public static final String PARAMETER_PATH_BALLOTBOXSTATUS = "pathBallotboxstatus";

	public static final String PARAMETER_PATH_CLEANSING_OUTPUTS = "pathCleansingOutputs";

	public static final String PARAMETER_PATH_ELECTION_EVENT_ID = "electionEventId";

	public static final String PARAMETER_PATH_BALLOT_BOX = "pathBallotBox";

	public static final String QUERY_PARAMETER_SIZE = "size";

	public static final String PARAMETER_PATH_CREDENTIALDATA = "pathCredentialdata";

	public static final String PARAMETER_PATH_VOTERINFORMATIONDATA = "pathVoterInformationData";

	public static final String PARAMETER_VALUE_VOTING_CARD_SET_ID = "votingCardSetId";

	public static final String PARAMETER_PATH_VOTERINFORMATION = "pathVoterinformation";

	public static final String QUERY_PARAMETER_SEARCH_WITH_VOTING_CARD_ID = "id";

	public static final String QUERY_PARAMETER_OFFSET = "offset";

	public static final String PARAMETER_PATH_CODESMAPPINGDATA = "pathCodesmappingdata";

	public static final String PARAMETER_VALUE_VERIFICATION_CARD_SET_ID = "verificationCardSetId";

	public static final String PARAMETER_PATH_VERIFICATIONCARDDATA = "pathVerificationcarddata";

	public static final String PARAMETER_PATH_VERIFICATIONCARDSETDATA = "pathVerificationcardsetdata";

	public static final String PARAMETER_PATH_EXT_AUTH = "extAuthPath";

	public static final String PARAMETER_PATH_TOKENS = "pathTokens";

	public static final String PARAMETER_VALUE_CREDENTIAL_ID = "credentialId";

	public static final String PARAMETER_PATH_VALIDATION_TOKENS = "pathValidationTokens";

	public static final String PARAMETER_PATH_INFORMATIONS = "pathInformations";

	public static final String PARAMETER_PATH_CERTIFICATES = "pathCertificates";

	public static final String PARAMETER_PATH_AUTHENTICATION_DATA = "pathAuthenticationData";

	public static final String PATH_CLEANSED_BALLOT_BOXES = "pathCleansedBallotBoxes";

	public static final String PATH_VALIDATIONS = "pathValidations";

	public static final String PARAMETER_ACCEPT = "Accept";

	public static final String PARAMETER_PATH_BALLOT = "pathBallot";

	public static final String PARAMETER_PATH_BALLOT_TEXT = "pathBallotText";

	public static final String PARAMETER_PATH_CONFIRMATION = "pathConfirmationMessage";

	public static final String PARAMETER_PATH_CAST_CODE = "pathCastCode";

	public static final String PARAMETER_PATH_VOTE = "pathVote";

	public static final String VERIFICATION_CARD_ID = "verificationCardId";

	public static final String PARAMETER_PATH_COMPUTE_CHOICE_CODES_REQUEST = "pathComputeChoiceCodesRequest";

	public static final String PARAMETER_PATH_COMPUTE_CHOICE_CODES_RETRIEVAL = "pathComputeChoiceCodesRetrieval";

	public static final String PARAMETER_VALUE_CHUNK_ID = "chunkId";

	public static final String PARAMETER_PATH_GENERATE_MIXDEC_KEYS = "pathGenerateMixDecKeys";

	public static final String PARAMETER_PATH_GENERATE_CHOICE_CODES_KEYS = "pathGenerateChoiceCodesKeys";

	public static final String PARAMETER_PATH_MIX_DECRYPT = "pathMixDecrypt";

	public static final String PARAMETER_PATH_COMPUTE_CHOICE_CODES = "pathComputeChoiceCodes";

	public static final String PARAMETER_VALUE_CHUNK_COUNT = "chunkCount";

	public static final String PARAMETER_CHOICE_CODE_PATH = "choiceCodePath";

	public static final String PARAMETER_VERIFICATION_PATH = "verificationPath";

	public static final String PARAMETER_VERIFICATION_SET_PATH = "verificationSetPath";

	public static final String PARAMETER_CAST_CODE_PATH = "castCodePath";

	public static final String PARAMETER_VALIDATION_PATH = "validationPath";

	public static final String EA = "EA";

	public static final String AU = "AU";

	public static final String EI = "EI";

	public static final String VM = "VM";

	public static final String VW = "VW";

	public static final String CR = "CR";

	public static final String VV = "VV";

	public static final String OR = "OR";

	public static final String MO = "MO";

	public static final String PARAMETER_PATH_CONFIRMATIONS = "pathConfirmations";

	public static final String PARAMETER_PATH_VOTES = "pathVotes";

	public static final String PARAMETER_PATH_CHOICECODES = "pathChoicecodes";

	public static final String PARAMETER_PATH_CASTCODES = "pathCastcodes";

	public static final String PATH_MATERIALS = "pathMaterials";

	// Avoid instantiation.
	private Constants() {
	}
}
