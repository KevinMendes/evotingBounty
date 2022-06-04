/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans;

@SuppressWarnings("unused")
public class TestNonSerializableBean {

	private String fieldOne;

	private Integer fieldTwo;

	public void setFieldTwo(Integer fieldTwo) {
		this.fieldTwo = fieldTwo;
	}

	public void setFieldOne(String fieldOne) {
		this.fieldOne = fieldOne;
	}

}
