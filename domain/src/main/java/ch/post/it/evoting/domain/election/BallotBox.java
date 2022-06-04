/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election;

/**
 * An object to represent ballot box information. It will be serialized to a json file in order to be used by other modules.
 */
public class BallotBox {

	private boolean test;
	private String eeid;
	private String bid;
	private String id;
	private String alias;
	private EncryptionParameters encryptionParameters;
	private String electoralAuthorityId;
	private String startDate;
	private String endDate;
	private String gracePeriod;
	private String writeInAlphabet;

	public boolean isTest() {
		return test;
	}

	public void setTest(final boolean test) {
		this.test = test;
	}

	public EncryptionParameters getEncryptionParameters() {
		return encryptionParameters;
	}

	public void setEncryptionParameters(final EncryptionParameters encryptionParameters) {
		this.encryptionParameters = encryptionParameters;
	}

	public String getEeid() {
		return eeid;
	}

	public void setEeid(final String eeid) {
		this.eeid = eeid;
	}

	public String getBid() {
		return bid;
	}

	public void setBid(final String bid) {
		this.bid = bid;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(final String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(final String endDate) {
		this.endDate = endDate;
	}

	public String getElectoralAuthorityId() {
		return electoralAuthorityId;
	}

	public void setElectoralAuthorityId(final String electoralAuthorityId) {
		this.electoralAuthorityId = electoralAuthorityId;
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

	public void setAlias(final String alias) {
		this.alias = alias;
	}

	public String getWriteInAlphabet() {
		return writeInAlphabet;
	}

	public void setWriteInAlphabet(final String writeInAlphabet) {
		this.writeInAlphabet = writeInAlphabet;
	}

}
