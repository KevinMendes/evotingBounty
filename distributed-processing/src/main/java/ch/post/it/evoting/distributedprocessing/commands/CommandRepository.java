/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.distributedprocessing.commands;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CommandRepository extends CrudRepository<Command, CommandId> {

	boolean existsByContextIdAndContext(String contextId, String context);

	Optional<Command> findByContextIdAndContextAndNodeId(String contextId, String context, Integer nodeId);

	@Transactional(isolation = Isolation.SERIALIZABLE)
	List<Command> findAllByCorrelationId(String correlationId);

	@Transactional(isolation = Isolation.SERIALIZABLE)
	List<Command> findAllByCorrelationIdAndResponsePayloadIsNotNull(String correlationId);

	@Transactional(isolation = Isolation.SERIALIZABLE)
	Integer countByCorrelationIdAndResponsePayloadIsNotNull(String correlationId);
}
