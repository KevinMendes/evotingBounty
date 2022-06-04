/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.infrastructure.persistence;

import javax.persistence.EntityManager;

import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VoterInformation;

import de.akquinet.jbosscc.needle.db.testdata.AbstractTestdataBuilder;

public class VoterInformationTestdataBuilder extends AbstractTestdataBuilder<VoterInformation> {

	private VoterInformation voterInformation;

	public VoterInformationTestdataBuilder(EntityManager entityManager) {
		super(entityManager);
	}

	public void setVoterInformation(VoterInformation voterInformation) {
		this.voterInformation = voterInformation;
	}

	@Override
	public VoterInformation build() {
		return voterInformation;
	}

}
