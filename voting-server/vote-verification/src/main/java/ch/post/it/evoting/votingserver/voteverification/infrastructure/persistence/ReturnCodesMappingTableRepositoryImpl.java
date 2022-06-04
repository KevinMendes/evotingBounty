/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.persistence;

import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.ReturnCodesMappingTableEntity;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.ReturnCodesMappingTableRepository;

/**
 * Implementation of the repository with JPA
 */
public class ReturnCodesMappingTableRepositoryImpl extends BaseRepositoryImpl<ReturnCodesMappingTableEntity, String>
		implements ReturnCodesMappingTableRepository {

}