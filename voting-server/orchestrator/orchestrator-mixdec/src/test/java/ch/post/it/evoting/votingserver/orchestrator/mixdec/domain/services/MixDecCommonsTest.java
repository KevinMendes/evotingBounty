/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.domain.returncodes.KeyCreationDTO;

class MixDecCommonsTest {

	@Test
	void whenGenerateKeyThenExpectedValue() {

		final String ee = "AAAAAAAAAAA";
		final String ea = "BBBBBBBBBBB";
		final String tracking = "CCCCCCCCCCC";

		final KeyCreationDTO keyCreationDTO = new KeyCreationDTO();
		keyCreationDTO.setElectionEventId(ee);
		keyCreationDTO.setResourceId(ea);
		keyCreationDTO.setRequestId(tracking);

		final String output = MixDecCommons.getMixDecKeyGenerationKey(keyCreationDTO);
		final String expectedOutput = String.format("MIXDEC_KEY_GENERATION.%s.%s.%s", ee, ea, tracking);

		assertEquals(expectedOutput, output);
	}
}
