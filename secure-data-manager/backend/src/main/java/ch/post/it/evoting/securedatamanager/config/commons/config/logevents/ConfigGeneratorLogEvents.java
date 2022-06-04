/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.config.logevents;

@SuppressWarnings("squid:S1192") // Ignore 'String literals should not be duplicated' Sonar's rule for this enum definition.
public enum ConfigGeneratorLogEvents {

	GENEECA_SUCCESS_CA_CERTIFICATE_GENERATED("GENERATOR", "GENEECA", "000", "CA certificate successfully generated and stored"),
	GENEECA_ERROR_GENERATING_CA_CERTIFICATE("GENERATOR", "GENEECA", "512", "Error generating the CA certificate"),
	GENEECA_ERROR_STORING_KEYSTORE("GENERATOR", "GENEECA", "513", "error creating keystore"),
	GENEECA_SUCCESS_STORING_KEYSTORE("GENERATOR", "GENEECA", "000", "successfully created and stored keystore"),
	GENEECA_ERROR_GENERATING_KEYPAIR_CA_CERTIFICATE("GENERATOR", "GENEECA", "515", "Error generating the key pair for the CA Certificate"),
	GENEECA_SUCCESS_KEYPAIR_GENERATED_STORED("GENERATOR", "GENEECA", "000", "Key Pairs successfully generated and stored"),

	GENABK_ERROR_GENERATING_KEYPAIR_AB("GENERATOR", "GENABK", "516", "error generating the key pair of the AB"),
	GENABK_SUCCESS_KEYPAIR_GENERATED_STORED("GENERATOR", "GENABK", "000", "key pair successfully generated and stored"),
	GENABK_SUCCESS_AB_CERTIFICATE_GENERATED("GENERATOR", "GENABK", "000", "AB certificate correctly generated"),
	GENABK_ERROR_GENERATING_AB_CERTIFICATE("GENERATOR", "GENABK", "517", "error generating the AB certificate"),
	GENABK_SUCCESS_AB_KEY_SHARES_GENERATED("GENERATOR", "GENABK", "000", "Administration Board key shares successfully generated"),
	GENABK_ERROR_GENERATING_AB_KEY_SHARES("GENERATOR", "GENABK", "518", "error while generating the Administrations Board key shares"),

	GENEBK_ERROR_GENERATING_KEYPAIR_EB("GENERATOR", "GENEBK", "519", "error - error generating the key pair of the EB"),
	GENEBK_SUCCESS_KEYPAIR_SUCCESSFULLY_GENERATED("GENERATOR", "GENEBK", "000", "key pair successfully generated"),
	GENEBK_SUCCESS_EB_KEY_SHARES_GENERATED("GENERATOR", "GENEBK", "000", "Electoral Board key shares successfully generated"),
	GENEBK_ERROR_GENERATING_EB_KEY_SHARES("GENERATOR", "GENEBK", "520", "error while generating the Electoral Board key shares"),

	GENSSK_ERROR_GENERATING_SERVICE_KEYPAIR("GENERATOR", "GENSSK", "521", "error - error generating the service key pair"),
	GENSSK_SUCCESS_KEYPAIR_GENERATED_STORED("GENERATOR", "GENSSK", "000", "success - key pair successfully generated and stored"),
	GENSSK_ERROR_GENERATING_SERVICE_CERTIFICATE("GENERATOR", "GENSSK", "522", "error - error generating the service certificate"),
	GENSSK_SUCCESS_SERVICE_CERTIFICATE_GENERATED("GENERATOR", "GENSSK", "000", "success - service certificate correctly generated"),
	GENSSK_ERROR_CREATING_KEYSTORE("GENERATOR", "GENSSK", "523", "error - error creating keystore"),
	GENSSK_SUCCESS_CREATED_STORED_KEYSTORE("GENERATOR", "GENSSK", "000", "success - successfully created and stored keystore"),

	GENSVPK_ERROR_GENERATING_SVK("GENERATOR", "GENSVPK", "524", "error - error generating the start voting keys"),
	GENSVPK_SUCCESS_SVK_GENERATED("GENERATOR", "GENSVPK", "000", "success - Start Voting Keys successfuly generated"),
	GENSVPK_ERROR_GENERATING_VCID("GENERATOR", "GENSVPK", "525", "error - error generating the Voting Card Id"),
	GENSVPK_ERROR_DERIVING_KEYSTORE_SYMMETRIC_ENCRYPTION_KEY("GENERATOR", "GENSVPK", "526",
			"error - error deriving the keystore symmetric encryption key"),
	GENSVPK_SUCCESS_VCIDS_GENERATED("GENERATOR", "GENSVPK", "000", "success - Voting Card IDs successfuly generated"),
	GENSVPK_SUCCESS_KEYSTORE_SYMMETRIC_ENCRYPTION_KEY_DERIVED("GENERATOR", "GENSVPK", "000",
			"success - Keystore symmetric encryption key successfully derived"),

	GENCREDAT_ERROR_DERIVING_CREDENTIAL_ID("GENERATOR", "GENCREDAT", "527", "error - error deriving the Credential ID"),
	GENCREDAT_SUCCESS_CREDENTIAL_ID_DERIVED("GENERATOR", "GENCREDAT", "000", "success - Credential ID successfully derived"),
	GENCREDAT_ERROR_GENERATING_CREDENTIAL_ID_AUTHENTICATION_KEYPAIR("GENERATOR", "GENCREDAT", "529",
			"error - error generating the credentialID authentication key pair"),
	GENCREDAT_SUCCESS_CREDENTIAL_ID_AUTHENTICATION_KEYPAIR_GENERATED("GENERATOR", "GENCREDAT", "000",
			"success - credentialID authentication key pair successfully generated"),
	GENCREDAT_ERROR_GENERATING_CREDENTIAL_ID_AUTHENTICATION_CERTIFICATE("GENERATOR", "GENCREDAT", "531",
			"error - error generating the credentialID authentication certificate"),
	GENCREDAT_SUCCESS_CREDENTIAL_ID_AUTHENTICATION_CERTIFICATE_GENERATED("GENERATOR", "GENCREDAT", "000",
			"success - credentialID authentication certificate correctly generated"),
	GENCREDAT_ERROR_GENERATING_KEYSTORE("GENERATOR", "GENCREDAT", "532", "error - error creating keystore"),
	GENCREDAT_SUCCESS_KEYSTORE_GENERATED("GENERATOR", "GENCREDAT", "000", "success - successfully  created and stored keystore"),
	SUCCESS_VOTINGCARD_VERIFICATIONCARD("GENERATOR", "GENCREDAT", "000", "Voting and Verification card successfully generated"),

	GENVCD_ERROR_GENERATING_VERIFICATION_CARDSET_ID("GENERATOR", "GENVCD", "533", "error - error generating the Verification Card Set ID"),
	GENVCD_SUCCESS_VERIFICATION_CARDSET_ID_GENERATED("GENERATOR", "GENVCD", "000", "success - verification card set ID successfully generated"),
	GENVCD_ERROR_GENERATING_VERIFICATION_CARD_SET_ISSUER_KEYPAIR("GENERATOR", "GENVCD", "534",
			"error - error generating the Verification Card Set Issuer Key Pair"),
	GENVCD_SUCCESS_GENERATING_VERIFICATION_CARD_SET_ISSUER_KEYPAIR("GENERATOR", "GENVCD", "000",
			"success - Verification Card Set Issuer Key pair successfully generated"),
	GENVCD_ERROR_GENERATING_VERIFICATION_CARD_SET_ISSUER_CERIFICATE("GENERATOR", "GENVCD", "535",
			"error - error generating the Verification Card Set Issuer Certificate"),
	GENVCD_SUCCESS_GENERATING_VERIFICATION_CARD_SET_ISSUER_CERIFICATE("GENERATOR", "GENVCD", "000",
			"success - Verification Card Set Issuer certificate successfully generated"),
	GENVCD_ERROR_GENERATING_CHOICES_CODES_KEYPAIR("GENERATOR", "GENVCD", "536", "error - error generating the Choice Codes key pair"),
	GENVCD_SUCCESS_GENERATING_CHOICES_CODES_KEYPAIR("GENERATOR", "GENVCD", "000", "success - Choice Codes key pair successfully generated"),
	GENVCD_ERROR_KEYSTORE_CREATED("GENERATOR", "GENVCD", "537", "error - error creating keystore"),
	GENVCD_SUCCESS_KEYSTORE_CREATED("GENERATOR", "GENVCD", "000", "success - successfully created and stored keystore"),
	GENVCD_ERROR_GENERATING_VERIFICATION_CARD_IDS("GENERATOR", "GENVCD", "538", "error - error generating the Verification Card Ids"),
	GENVCD_SUCCESS_GENERATING_VERIFICATION_CARD_IDS("GENERATOR", "GENVCD", "000", "success - Verification Card Ids successfully generated"),
	GENVCD_ERROR_GENERATING_VERIFICATION_CARD_KEYPAIR("GENERATOR", "GENVCD", "539", "error - error generating Verification Card key pair"),
	GENVCD_SUCCESS_GENERATING_VERIFICATION_CARD_KEYPAIR("GENERATOR", "GENVCD", "000", "success - Verification Card key pair successfully generated"),
	GENVCD_ERROR_GENERATING_VERIFICATION_CARD_PUBLIC_KEY("GENERATOR", "GENVCD", "540",
			"error - error while signing the Verification Card Public Key"),
	GENVCD_SUCCESS_GENERATING_VERIFICATION_CARD_PUBLIC_KEY("GENERATOR", "GENVCD", "000",
			"success - Verification Card Public Key successfully signed"),
	GENVCD_ERROR_GENERATING_VERIFICATION_KEYSTORE("GENERATOR", "GENVCD", "541", "error - error creating keystore"),
	GENVCD_SUCCESS_GENERATING_VERIFICATION_CARD_KEYSTORE("GENERATOR", "GENVCD", "000", "success - successfully created and stored keystore"),

	GENVCC_SUCCESS_BALLOTCASTINGKEY_GENERATED("GENERATOR", "GENVCC", "000", "Ballot Casting Key successfully generated"),
	GENVCC_ERROR_GENERATING_BALLOTCASTINGKEY("GENERATOR", "GENVCC", "545", "error generating the Ballot Casting Key"),
	GENVCC_SUCCESS_SHORTVOTECASTCODE_GENERATED("GENERATOR", "GENVCC", "000", "short Vote Cast Code successfully generated"),
	GENVCC_ERROR_GENERATING_SHORTVOTECASTCODE("GENERATOR", "GENVCC", "546", "error generating the short Vote Cast Code"),
	GENVCC_SUCCESS_PRE_CHOICECODES_GENERATED("GENERATOR", "GENVCC", "000", "pre-Choice Return Codes successfully generated"),
	GENVCC_ERROR_GENERATING_PRE_CHOICECODES("GENERATOR", "GENVCC", "547", "error generating the pre-Choice Return Codes"),
	GENVCC_SUCCESS_LONGCHOICECODES_GENERATED("GENERATOR", "GENVCC", "000", "long Choice Codes successfully generated"),
	GENVCC_ERROR_GENERATING_LONGCHOICECODES("GENERATOR", "GENVCC", "548", "error generating the long Choide Codes"),
	GENVCC_SUCCESS_CHOICECODES_STORED("GENERATOR", "GENVCC", "000", "Choice Codes correctly stored"),
	GENVCC_ERROR_STORING_CHOICECODES("GENERATOR", "GENVCC", "549", "error storing the Choice Codes in the Code - Mapping Table"),
	GENVCC_SUCCESS_PRE_VOTECASTCODE_GENERATED("GENERATOR", "GENVCC", "000", "pre-Vote Cast Code successfully generated"),
	GENVCC_ERROR_GENERATING_PRE_VOTECASTCODE("GENERATOR", "GENVCC", "550", "error generating the pre-Vote Cast Code"),
	GENVCC_SUCCESS_LONGVOTECASTCODE_GENERATED("GENERATOR", "GENVCC", "000", "Long Vote Cast Code successfully generated"),
	GENVCC_ERROR_GENERATING_LONGVOTECASTCODE("GENERATOR", "GENVCC", "551", "error generating the Long Vote Cast Code"),
	GENVCC_SUCCESS_VOTECASTCODE_SIGNED("GENERATOR", "GENVCC", "000", "Vote Cast Code successfully signed"),
	GENVCC_ERROR_SIGNING_VOTECASTCODE("GENERATOR", "GENVCC", "552", "error while signing the Vote Cast Code"),
	GENVCC_SUCCESS_VOTECASTCODE_STORED("GENERATOR", "GENVCC", "000", "Vote Cast Code correctly stored"),
	GENVCC_ERROR_STORING_VOTECASTCODE("GENERATOR", "GENVCC", "553", "error storing the Vote Cast Code in the Code - Mapping Table"),

	GENBB_SUCCESS_CREATED_AND_STORED("GENERATOR", "GENBB", "000", "Ballot box successfully created and stored"),
	GENBB_SUCCESS_KEYPAIR_GENERATED_STORED("GENERATOR", "GENBB", "000", "key pair successfully generated and stored"),
	GENBB_ERROR_GENERATING_KEYPAIR("GENERATOR", "GENBB", "554", "error - error generating ballot box key pair"),
	GENBB_SUCCESS_CERTIFICATE_GENERATED("GENERATOR", "GENBB", "000", "Ballot box certificate correctly generated"),
	GENBB_ERROR_GENERATING_CERTIFICATE("GENERATOR", "GENBB", "555", "error - error generating ballot box certificate"),
	GENVCC_ERROR_GENERATING_SHORTCHOICECODE("GENERATOR", "GENVCC", "556", "error generating the short Choice Code"),

	GENVCDK_ERROR_GENERATING_DERIVED_KEY_COMMITMENTS("WRITER", "WRITEDERIVKEYS", "557", "error - error writing derived keys file");

	private final String layer;

	private final String action;

	private final String outcome;

	private final String info;

	ConfigGeneratorLogEvents(final String layer, final String action, final String outcome, final String info) {
		this.layer = layer;
		this.action = action;
		this.outcome = outcome;
		this.info = info;
	}

	public String getAction() {
		return action;
	}

	public String getOutcome() {
		return outcome;
	}

	public String getInfo() {
		return info;
	}

	public String getLayer() {
		return layer;
	}
}
