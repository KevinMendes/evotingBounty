/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service;

import javax.enterprise.inject.Produces;

import ch.post.it.evoting.cryptoprimitives.hashing.HashService;

/**
 * Produces a {@link HashService} with default message digest.
 */
public class HashServiceProducer {

	@Produces
	public HashService getInstance() {
		return HashService.getInstance();
	}

}
