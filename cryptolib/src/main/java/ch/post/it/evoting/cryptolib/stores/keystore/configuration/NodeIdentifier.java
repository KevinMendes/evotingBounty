/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

public enum NodeIdentifier {

	ADMIN_PORTAL("Admin Portal", "AP", "ap"),
	API_GATEWAY("API Gateway", "AG", "ag"),
	AUTHENTICATION("Authentication", "AU", "au"),
	CERTIFICATE_REGISTRY("Certificate Registry", "CR", "cr"),
	CONFIG_PLATFORM_ROOT("Config Platform Root", "CPR", "cpr"),
	ELECTION_INFORMATION("Election Information", "EI", "ei"),
	EXTENDED_AUTHENTICATION("Extended Authentication", "EA", "ea"),
	SECURE_DATA_MANAGER("Secure Data Manager", "SDM", "sdm"),
	VOTE_VERIFICATION("Vote Verification", "VV", "vv"),
	VOTER_MATERIAL("Voter Material", "VM", "vm"),
	VOTING_WORKFLOW("Voting Workflow", "VW", "vw"),
	SERVICES("Services Application", "SRV", "srv");

	private final String name;
	private final String shortName;
	private final String alias;

	NodeIdentifier(final String name, final String shortName, final String alias) {
		this.name = name;
		this.shortName = shortName;
		this.alias = alias;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	public String getAlias() {
		return alias;
	}

}
