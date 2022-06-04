/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.confirmvote;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.hashing.Hashable;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.domain.ContextIds;

@JsonPropertyOrder({ "contextIds", "element" })
public class ConfirmationKey implements HashableList {

	@JsonProperty
	private final ContextIds contextIds;
	@JsonProperty
	private final GqElement element;

	@JsonCreator
	public ConfirmationKey(
			@JsonProperty("contextIds")
			final ContextIds contextIds,
			@JsonProperty("element")
			final GqElement element) {
		this.contextIds = checkNotNull(contextIds);
		this.element = checkNotNull(element);
	}

	public ContextIds getContextIds() {
		return contextIds;
	}

	public GqElement getElement() {
		return element;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ConfirmationKey that = (ConfirmationKey) o;
		return contextIds.equals(that.contextIds) && element.equals(that.element);
	}

	@Override
	public int hashCode() {
		return Objects.hash(contextIds, element);
	}

	@Override
	public ImmutableList<? extends Hashable> toHashableForm() {
		return ImmutableList.of(contextIds, element);
	}
}
