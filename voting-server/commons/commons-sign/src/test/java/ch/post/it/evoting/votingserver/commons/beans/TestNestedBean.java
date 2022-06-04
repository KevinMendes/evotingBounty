/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans;

public class TestNestedBean {

	private TestBean bean;

	private Integer index;

	public TestBean getBean() {
		return bean;
	}

	public void setBean(TestBean bean) {
		this.bean = bean;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}
}
