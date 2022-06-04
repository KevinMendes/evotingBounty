/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.votingserver.voteverification.domain.model.content;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Repository for handling ReturnCodesMappingTable entities
 */
@Local
public interface ReturnCodesMappingTableRepository extends BaseRepository<ReturnCodesMappingTableEntity, String> {
}