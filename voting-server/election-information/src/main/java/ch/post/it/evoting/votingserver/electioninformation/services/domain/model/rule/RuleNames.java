/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.rule;

/**
 * Enum to hold all the text of the existing rules.
 */
public enum RuleNames {

	VOTE_VERIFY_SIGNATURE("vote_verify_signature"),
	VOTE_CREDENTIAL_ID("vote_credential_id"),
	VOTE_ELECTION_DATES("vote_election_dates"),
	VOTE_MAX_NUMBER_OF_ALLOWED_VOTES("max_number_of_allowed_votes"),
	VOTE_OPTIONS_BIG_INTEGERS("encrypted_options_big_integers"),
	VOTE_NUMBER_OF_PARTIAL_CHOICE_CODES("number_of_partial_choice_codes"),
	VOTE_VERIFY_CERT_CHAIN("certificate_chain_validation"),
	VOTE_IDS("vote_ids_validation"),
	VOTE_CORRECTNESS("vote_correctness"),
	VOTE_BLOCKED_BALLOT_BOX("vote_blocked_ballot_box");

	/**
	 * The actual name of the rule.
	 */
	private final String text;

	RuleNames(String name) {
		this.text = name;
	}

	/**
	 * Returns the enum element for a given text.
	 *
	 * @param ruleName2Find The rule to be searched in the enum.
	 * @return a RulesName element having the given text.
	 */
	public static RuleNames getRuleName4Text(String ruleName2Find) {
		RuleNames result = null;
		for (RuleNames ruleName : RuleNames.values()) {
			if (ruleName.getText().equals(ruleName2Find)) {
				result = ruleName;
				break;
			}
		}
		return result;
	}

	/**
	 * Returns the current text of the field text.
	 *
	 * @return Returns the text.
	 */
	public String getText() {
		return text;
	}
}
