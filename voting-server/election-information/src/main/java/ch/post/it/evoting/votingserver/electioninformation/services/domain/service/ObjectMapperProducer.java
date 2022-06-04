/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;

@Dependent
public class ObjectMapperProducer {

	@Produces
	public ObjectMapper getInstance() {
		return DomainObjectMapper.getNewInstance();
	}

}
