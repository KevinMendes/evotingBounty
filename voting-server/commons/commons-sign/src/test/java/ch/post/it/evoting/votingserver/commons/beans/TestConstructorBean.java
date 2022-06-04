/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestConstructorBean {

	private final String fieldOne;

	private final Integer fieldTwo;

	@JsonCreator
	public TestConstructorBean(
			@JsonProperty("fieldOne")
					String fieldOne,
			@JsonProperty("fieldTwo")
					Integer fieldTwo) {
		this.fieldOne = fieldOne;
		this.fieldTwo = fieldTwo;
	}

	public Integer getFieldTwo() {
		return fieldTwo;
	}

	public String getFieldOne() {
		return fieldOne;
	}

}
