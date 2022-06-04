/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.infrastructure.transaction;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;

import ch.post.it.evoting.votingserver.commons.infrastructure.transaction.AbstractTransactionController;
import ch.post.it.evoting.votingserver.commons.infrastructure.transaction.TransactionController;

/**
 * Implementation of {@link TransactionController} as a singleton stateless session bean.
 */
@Singleton
@Local(TransactionController.class)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class VoterMaterialTransactionController extends AbstractTransactionController {
}
