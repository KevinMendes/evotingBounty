/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.logging.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;
import ch.post.it.evoting.domain.election.model.vote.Vote;

/**
 * Computes the hash of a vote.
 */
public class VoteHashService {

	public String hash(Vote vote) throws GeneralCryptoLibException {

		PrimitivesServiceAPI primitivesService = new PrimitivesService();

		byte[] hashBytes = primitivesService.getHash(StringUtils.join(vote.getFieldsAsStringArray()).getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(hashBytes);
	}
}
