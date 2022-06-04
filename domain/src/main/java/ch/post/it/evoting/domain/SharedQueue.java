/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain;

public class SharedQueue {
	public static final String CREATE_LVCC_SHARE_REQUEST_PATTERN = "voting.return-codes.CreateLVCCShareRequest.";
	public static final String CREATE_LVCC_SHARE_RESPONSE_PATTERN = "voting.return-codes.CreateLVCCShareResponse.";

	public static final String CREATE_LCC_SHARE_REQUEST_PATTERN = "voting.return-codes.CreateLCCShareRequest.";
	public static final String CREATE_LCC_SHARE_RESPONSE_PATTERN = "voting.return-codes.CreateLCCShareResponse.";

	public static final String GEN_ENC_LONG_CODE_SHARES_REQUEST_PATTERN = "configuration.return-codes.GenEncLongCodeSharesRequest.";
	public static final String GEN_ENC_LONG_CODE_SHARES_RESPONSE_PATTERN = "configuration.return-codes.GenEncLongCodeSharesResponse.";

	public static final String GEN_KEYS_CCR_REQUEST_PATTERN = "configuration.return-codes.GenKeysCCRRequest.";
	public static final String GEN_KEYS_CCR_RESPONSE_PATTERN = "configuration.return-codes.GenKeysCCRResponse.";

	public static final String MIX_DEC_ONLINE_REQUEST_PATTERN = "tally.mixing.MixDecOnlineRequest.";
	public static final String MIX_DEC_ONLINE_RESPONSE_PATTERN = "tally.mixing.MixDecOnlineResponse.";

	public static final String NEW_ORCHESTRATOR_MIX_DEC_ONLINE_REQUEST_PATTERN = "new.orchestrator.tally.mixing.MixDecOnlineRequest.";
	public static final String NEW_ORCHESTRATOR_MIX_DEC_ONLINE_RESPONSE_PATTERN = "new.orchestrator.tally.mixing.MixDecOnlineResponse.";

	public static final String PARTIAL_DECRYPT_PCC_REQUEST_PATTERN = "voting.return-codes.PartialDecryptPCCRequest.";
	public static final String PARTIAL_DECRYPT_PCC_RESPONSE_PATTERN = "voting.return-codes.PartialDecryptPCCResponse.";

	public static final String SETUP_TALLY_CCM_REQUEST_PATTERN = "configuration.mixing.SetupTallyCCMRequest.";
	public static final String SETUP_TALLY_CCM_RESPONSE_PATTERN = "configuration.mixing.SetupTallyCCMResponse.";

	public static final String ELECTION_CONTEXT_REQUEST_PATTERN = "configuration.electioncontext.ElectionContextRequest.";
	public static final String ELECTION_CONTEXT_RESPONSE_PATTERN = "configuration.electioncontext.ElectionContextResponse.";

	private SharedQueue() {
		// static usage only.
	}

	public static String fromName(final String name) {
		switch (name) {
		case "CREATE_LVCC_SHARE_RESPONSE_PATTERN":
			return CREATE_LVCC_SHARE_RESPONSE_PATTERN;
		case "CREATE_LCC_SHARE_RESPONSE_PATTERN":
			return CREATE_LCC_SHARE_RESPONSE_PATTERN;
		case "GEN_KEYS_CCR_RESPONSE_PATTERN":
			return GEN_KEYS_CCR_RESPONSE_PATTERN;
		case "PARTIAL_DECRYPT_PCC_RESPONSE_PATTERN":
			return PARTIAL_DECRYPT_PCC_RESPONSE_PATTERN;
		case "ELECTION_CONTEXT_RESPONSE_PATTERN":
			return ELECTION_CONTEXT_RESPONSE_PATTERN;
		case "NEW_ORCHESTRATOR_MIX_DEC_ONLINE_RESPONSE_PATTERN":
			return NEW_ORCHESTRATOR_MIX_DEC_ONLINE_RESPONSE_PATTERN;
		default:
			throw new IllegalArgumentException(String.format("Unknown shared queue name provided. [provided name: %s]", name));
		}
	}
}
