/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services;

import ch.post.it.evoting.domain.returncodes.KeyCreationDTO;

public class MixDecCommons {

	private static final String ID_SEPARATOR = ".";

	private static final String COMMONS_TYPE = "MIXDEC_";

	private static final String MIXDEC_KEY_GENERATION_ID = COMMONS_TYPE + "KEY_GENERATION";

	/**
	 * Non-public constructor
	 */
	private MixDecCommons() {

	}

	private static String getPartialResultsKey(String... resultsKeyFields) {
		return String.join(ID_SEPARATOR, resultsKeyFields);
	}

	private static String getMixDecKeyGenerationKey(String... resultsKeyFields) {
		return String.join(ID_SEPARATOR, MIXDEC_KEY_GENERATION_ID, getPartialResultsKey(resultsKeyFields));
	}

	public static String getMixDecKeyGenerationKey(KeyCreationDTO keyCreationDTO) {
		return getMixDecKeyGenerationKey(keyCreationDTO.getResultsKeyFields());
	}
}
