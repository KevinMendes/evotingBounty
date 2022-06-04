/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election;

/**
 * An object to represent encryption parameters information. It will be serialized to a json file in order to be used by other modules.*
 */
public class EncryptionParameters {

	private String p;

	private String q;

	private String g;

	/**
	 *
	 */
	public EncryptionParameters() {
	}

	/**
	 * @param p
	 * @param q
	 * @param g
	 */
	public EncryptionParameters(final String p, final String q, final String g) {
		super();
		this.p = p;
		this.q = q;
		this.g = g;
	}

	/**
	 * @return Returns the p.
	 */
	public String getP() {
		return p;
	}

	/**
	 * @param p The p to set.
	 */
	public void setP(final String p) {
		this.p = p;
	}

	/**
	 * @return Returns the q.
	 */
	public String getQ() {
		return q;
	}

	/**
	 * @param q The q to set.
	 */
	public void setQ(final String q) {
		this.q = q;
	}

	/**
	 * @return Returns the g.
	 */
	public String getG() {
		return g;
	}

	/**
	 * @param g The g to set.
	 */
	public void setG(final String g) {
		this.g = g;
	}

}
