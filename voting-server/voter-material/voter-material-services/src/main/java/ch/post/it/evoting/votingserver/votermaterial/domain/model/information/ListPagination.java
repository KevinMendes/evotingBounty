/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.domain.model.information;

/**
 * The Class ListPagination.
 */
public class ListPagination {

	/**
	 * The limit.
	 */
	private int limit;

	/**
	 * The offset.
	 */
	private int offset;

	/**
	 * The count.
	 */
	private long count;

	/**
	 * Gets the limit.
	 *
	 * @return Returns the limit.
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * Sets the limit.
	 *
	 * @param limit The limit to set.
	 */
	public void setLimit(final int limit) {
		this.limit = limit;
	}

	/**
	 * Gets the offset.
	 *
	 * @return Returns the offset.
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Sets the offset.
	 *
	 * @param offset The offset to set.
	 */
	public void setOffset(final int offset) {
		this.offset = offset;
	}

	/**
	 * Gets the count.
	 *
	 * @return Returns the count.
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Sets the count.
	 *
	 * @param count The count to set.
	 */
	public void setCount(final long count) {
		this.count = count;
	}
}
