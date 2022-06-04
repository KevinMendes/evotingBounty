/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters;

import java.nio.file.Path;
import java.security.PrivateKey;
import java.time.ZonedDateTime;

import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.domain.election.EncryptionParameters;
import ch.post.it.evoting.securedatamanager.commons.domain.CreateVotingCardSetCertificatePropertiesContainer;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VerificationCardCredentialInputDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VotingCardCredentialInputDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VotingCardSetCredentialInputDataPack;

public class VotersParametersHolder {
	private final int numberVotingCards;
	private final String ballotID;
	private final Ballot ballot;
	private final String ballotBoxID;
	private final String votingCardSetID;
	private final String verificationCardSetID;
	private final String votingCardSetAlias;
	private final String electoralAuthorityID;
	private final Path absoluteBasePath;
	private final String eeID;
	private final ZonedDateTime certificatesStartValidityPeriod;
	private final ZonedDateTime certificatesEndValidityPeriod;
	private final String platformRootCACertificate;
	private final CreateVotingCardSetCertificatePropertiesContainer createVotingCardSetCertificateProperties;

	private VotingCardCredentialInputDataPack votingCardInputDataPack;
	private VotingCardSetCredentialInputDataPack votingCardSetInputDataPack;
	private VerificationCardCredentialInputDataPack verificationCardCredentialInputDataPack;
	private PrivateKey credentialCAPrivKey;
	private PrivateKey servicesCAPrivKey;
	private CryptoAPIX509Certificate credentialCACert;
	private CryptoAPIX509Certificate electionCACert;
	private EncryptionParameters encryptionParameters;

	public VotersParametersHolder(final int numberVotingCards, final String ballotID, final Ballot ballot, final String ballotBoxID,
			final String votingCardSetID, final String verificationCardSetID, final String electoralAuthorityID, final Path absoluteBasePath,
			final String eeID, final ZonedDateTime certificatesStartValidityPeriod, final ZonedDateTime certificatesEndValidityPeriod,
			final String votingCardSetAlias, final String platformRootCACertificate,
			final CreateVotingCardSetCertificatePropertiesContainer createVotingCardSetCertificateProperties) {
		super();
		this.numberVotingCards = numberVotingCards;
		this.ballotID = ballotID;
		this.ballot = ballot;
		this.ballotBoxID = ballotBoxID;
		this.votingCardSetID = votingCardSetID;
		this.verificationCardSetID = verificationCardSetID;
		this.electoralAuthorityID = electoralAuthorityID;
		this.absoluteBasePath = absoluteBasePath;
		this.eeID = eeID;
		this.certificatesStartValidityPeriod = certificatesStartValidityPeriod;
		this.certificatesEndValidityPeriod = certificatesEndValidityPeriod;
		this.votingCardSetAlias = votingCardSetAlias;
		this.platformRootCACertificate = platformRootCACertificate;
		this.createVotingCardSetCertificateProperties = createVotingCardSetCertificateProperties;
	}

	public PrivateKey getServicesCAPrivKey() {
		return servicesCAPrivKey;
	}

	public void setServicesCAPrivKey(final PrivateKey servicesCAPrivKey) {
		this.servicesCAPrivKey = servicesCAPrivKey;
	}

	public int getNumberVotingCards() {

		return numberVotingCards;
	}

	public String getBallotID() {
		return ballotID;
	}

	public Ballot getBallot() {
		return ballot;
	}

	public String getBallotBoxID() {
		return ballotBoxID;
	}

	public String getVotingCardSetID() {
		return votingCardSetID;
	}

	public String getVerificationCardSetID() {
		return verificationCardSetID;
	}

	public String getElectoralAuthorityID() {
		return electoralAuthorityID;
	}

	public Path getAbsoluteBasePath() {
		return absoluteBasePath;
	}

	public String getEeid() {
		return eeID;
	}

	public VotingCardCredentialInputDataPack getVotingCardCredentialInputDataPack() {
		return votingCardInputDataPack;
	}

	public void setVotingCardCredentialInputDataPack(final VotingCardCredentialInputDataPack votingCardInputDataPack) {
		this.votingCardInputDataPack = votingCardInputDataPack;
	}

	public VotingCardSetCredentialInputDataPack getVotingCardSetCredentialInputDataPack() {
		return votingCardSetInputDataPack;
	}

	public void setVotingCardSetCredentialInputDataPack(final VotingCardSetCredentialInputDataPack votingCardSetInputDataPack) {
		this.votingCardSetInputDataPack = votingCardSetInputDataPack;
	}

	public PrivateKey getCredentialCAPrivKey() {
		return credentialCAPrivKey;
	}

	public void setCredentialCAPrivKey(final PrivateKey credentialCAPrivKey) {
		this.credentialCAPrivKey = credentialCAPrivKey;
	}

	public CryptoAPIX509Certificate getCredentialCACert() {
		return credentialCACert;
	}

	public void setCredentialsCACert(final CryptoAPIX509Certificate credentialCACert) {
		this.credentialCACert = credentialCACert;
	}

	public CryptoAPIX509Certificate getElectionCACert() {
		return electionCACert;
	}

	public void setElectionCACert(final CryptoAPIX509Certificate electionCACert) {
		this.electionCACert = electionCACert;
	}

	public EncryptionParameters getEncryptionParameters() {
		return encryptionParameters;
	}

	public void setEncryptionParameters(final EncryptionParameters encryptionParameters) {
		this.encryptionParameters = encryptionParameters;
	}

	public VerificationCardCredentialInputDataPack getVerificationCardInputDataPack() {
		return verificationCardCredentialInputDataPack;
	}

	public ZonedDateTime getCertificatesStartValidityPeriod() {
		return certificatesStartValidityPeriod;
	}

	public ZonedDateTime getCertificatesEndValidityPeriod() {
		return certificatesEndValidityPeriod;
	}

	public void setVerificationCardCredentialInputDataPack(final VerificationCardCredentialInputDataPack verificationCardCredentialInputDataPack) {
		this.verificationCardCredentialInputDataPack = verificationCardCredentialInputDataPack;
	}

	public String getVotingCardSetAlias() {
		return votingCardSetAlias;
	}

	public String getPlatformRootCACertificate() {
		return platformRootCACertificate;
	}

	public CreateVotingCardSetCertificatePropertiesContainer getCreateVotingCardSetCertificateProperties() {
		return createVotingCardSetCertificateProperties;
	}
}
