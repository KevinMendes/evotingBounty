/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.ballotbox;

import java.nio.file.Path;

import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.domain.election.EncryptionParameters;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans.ElectionInputDataPack;

/**
 * Encapsulates the parameters required by this command:
 * <ul>
 * <li>a {@link Ballot} ID.
 * <li>a list of ballot box IDs.
 * <li>the absolute output path as a {@code Path}.
 * <li>the signer private key.
 * </ul>
 */
public class BallotBoxParametersHolder {

	private final String ballotBoxID;
	private final String alias;
	private final Path outputPath;
	private final String ballotID;
	private final String electoralAuthorityID;
	private final String eeID;
	private final ElectionInputDataPack electionInputDataPack;
	private final String writeInAlphabet;
	private EncryptionParameters encryptionParameters;
	private String test;
	private String gracePeriod;

	public BallotBoxParametersHolder(final String ballotID, final String electoralAuthorityID, final String ballotBoxID, final String alias,
			final Path outputPath, final String eeID, final ElectionInputDataPack electionInputDataPack, final String test, final String gracePeriod,
			final String writeInAlphabet) {
		this.ballotID = ballotID;
		this.electoralAuthorityID = electoralAuthorityID;
		this.ballotBoxID = ballotBoxID;
		this.alias = alias;
		this.outputPath = outputPath;
		this.eeID = eeID;
		this.electionInputDataPack = electionInputDataPack;
		this.test = test;
		this.gracePeriod = gracePeriod;
		this.writeInAlphabet = writeInAlphabet;
	}

	public String getBallotBoxID() {
		return ballotBoxID;
	}

	public Path getOutputPath() {
		return outputPath;
	}

	public String getBallotID() {
		return ballotID;
	}

	public String getElectoralAuthorityID() {
		return electoralAuthorityID;
	}

	public String getEeID() {
		return eeID;
	}

	public ElectionInputDataPack getInputDataPack() {
		return electionInputDataPack;
	}

	public EncryptionParameters getEncryptionParameters() {
		return encryptionParameters;
	}

	public void setEncryptionParameters(final EncryptionParameters encryptionParameters) {
		this.encryptionParameters = encryptionParameters;
	}

	public String getTest() {
		return test;
	}

	public void setTest(final String test) {
		this.test = test;
	}

	public String getGracePeriod() {
		return gracePeriod;
	}

	public void setGracePeriod(final String gracePeriod) {
		this.gracePeriod = gracePeriod;
	}

	public String getAlias() {
		return alias;
	}

	public String getWriteInAlphabet() {
		return writeInAlphabet;
	}

}
