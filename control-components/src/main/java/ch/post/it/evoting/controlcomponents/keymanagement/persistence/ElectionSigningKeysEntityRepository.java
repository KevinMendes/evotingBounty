/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement.persistence;

import org.springframework.data.repository.CrudRepository;

public interface ElectionSigningKeysEntityRepository extends CrudRepository<ElectionSigningKeysEntity, ElectionSigningKeysEntityPrimaryKey> {
}
