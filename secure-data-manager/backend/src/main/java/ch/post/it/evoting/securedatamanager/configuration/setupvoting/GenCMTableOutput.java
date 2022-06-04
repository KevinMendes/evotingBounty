/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Regroups the outputs produced by the GenCMTable algorithm.
 */
@SuppressWarnings("java:S115")
public class GenCMTableOutput {
	private static final int l_HB64 = GenCMTableService.BASE64_ENCODE_HASH_OUTPUT_LENGTH;

	private final ImmutableMap<String, String> returnCodesMappingTable;
	private final ImmutableList<ImmutableList<String>> shortChoiceReturnCodes;
	private final ImmutableList<String> shortVoteCastReturnCodes;

	/**
	 * @throws NullPointerException     if any of the fields is null.
	 * @throws IllegalArgumentException if
	 *                                  <ul>
	 *                                      <li>The size of {@code returnCodesMappingTable} is zero.</li>
	 *                                      <li>The size of {@code shortChoiceReturnCodes} is zero.</li>
	 *                                      <li>The size of any {@code shortChoiceReturnCodes} element is zero.</li>
	 *                                      <li>The size of {@code shortVoteCastReturnCodesCopy} is zero.</li>
	 *                                      <li>The Return Codes Mapping table key length is not {@value l_HB64}.</li>
	 *                                      <li>The short Choice Return Codes elements length is not {@value CHOICE_RETURN_CODES_LENGTH}.</li>
	 *                                      <li>The short Vote Cast Return elements length is not {@value VOTE_CAST_RETURN_CODE_LENGTH}.</li>
	 *                                      <li>The number of elements of {@code shortChoiceReturnCodes} and {@code shortVoteCastReturnCodes} are not equal.</li>
	 *                                      <li>The {@code returnCodesMappingTable} size is not equal to N_E * (n + 1).</li>
	 *                                  </ul>
	 */
	private GenCMTableOutput(final Map<String, String> returnCodesMappingTable, final List<List<String>> shortChoiceReturnCodes,
			final List<String> shortVoteCastReturnCodes) {

		checkNotNull(returnCodesMappingTable);
		checkNotNull(shortChoiceReturnCodes);
		checkNotNull(shortVoteCastReturnCodes);

		final ImmutableMap<String, String> returnCodesMappingTableCopy = ImmutableMap.copyOf(returnCodesMappingTable);
		final ImmutableList<ImmutableList<String>> shortChoiceReturnCodesCopy = shortChoiceReturnCodes.stream()
				.map(ImmutableList::copyOf)
				.collect(ImmutableList.toImmutableList());
		final ImmutableList<String> shortVoteCastReturnCodesCopy = ImmutableList.copyOf(shortVoteCastReturnCodes);

		// Not empty lists check
		checkArgument(returnCodesMappingTable.size() > 0, "Return Codes Mapping table must not be empty.");
		checkArgument(!shortChoiceReturnCodesCopy.isEmpty(), "Short Choice Return Codes must not be empty.");
		checkArgument(shortChoiceReturnCodesCopy.stream().map(List::size).allMatch(size -> size > 0),
				"Short Choice Return Codes must not contain empty lists.");
		checkArgument(!shortVoteCastReturnCodesCopy.isEmpty(), "Vote Cast Return Codes must not be empty.");

		// Values length check
		checkArgument(returnCodesMappingTableCopy.keySet().stream().allMatch(key -> key.length() == l_HB64),
				String.format("Return Codes Mapping table keys must have a length of %s.", l_HB64));
		checkArgument(
				shortChoiceReturnCodesCopy.stream().flatMap(
						Collection::stream).allMatch(cc -> cc.length() == GenCMTableService.CHOICE_RETURN_CODES_LENGTH),
				String.format("Short Choice Return Codes values must have a length of %s.", GenCMTableService.CHOICE_RETURN_CODES_LENGTH));
		checkArgument(shortVoteCastReturnCodesCopy.stream().allMatch(vcc -> vcc.length() == GenCMTableService.VOTE_CAST_RETURN_CODE_LENGTH),
				String.format("Short Vote Cast Return Codes values must have a length of %s.", GenCMTableService.VOTE_CAST_RETURN_CODE_LENGTH));

		// Elements size checks.
		checkArgument(shortChoiceReturnCodesCopy.size() == shortVoteCastReturnCodesCopy.size(),
				"Short Choice Return Codes and short Vote Cast Return Codes must have the same number of elements.");

		// Expected CMtable size : N_E * (n + 1)
		final int expectedReturnCodesMappingTableSize = shortChoiceReturnCodesCopy.size() * (shortChoiceReturnCodesCopy.get(0).size() + 1);
		checkArgument(returnCodesMappingTable.size() == expectedReturnCodesMappingTableSize,
				String.format("Return Codes Mapping table must have a size of %s.", expectedReturnCodesMappingTableSize));

		this.returnCodesMappingTable = returnCodesMappingTableCopy;
		this.shortChoiceReturnCodes = shortChoiceReturnCodesCopy;
		this.shortVoteCastReturnCodes = shortVoteCastReturnCodesCopy;
	}

	public Map<String, String> getReturnCodesMappingTable() {
		return returnCodesMappingTable;
	}

	public List<List<String>> getShortChoiceReturnCodes() {
		return new ArrayList<>(shortChoiceReturnCodes);
	}

	public List<String> getShortVoteCastReturnCodes() {
		return shortVoteCastReturnCodes;
	}

	public static class Builder {
		private Map<String, String> returnCodesMappingTable;
		private List<List<String>> shortChoiceReturnCodes;
		private List<String> shortVoteCastReturnCodes;

		public Builder setReturnCodesMappingTable(final Map<String, String> returnCodesMappingTable) {
			this.returnCodesMappingTable = returnCodesMappingTable;
			return this;
		}

		public Builder setShortChoiceReturnCodes(final List<List<String>> shortChoiceReturnCodes) {
			this.shortChoiceReturnCodes = shortChoiceReturnCodes;
			return this;
		}

		public Builder setShortVoteCastReturnCodes(final List<String> shortVoteCastReturnCodes) {
			this.shortVoteCastReturnCodes = shortVoteCastReturnCodes;
			return this;
		}

		public GenCMTableOutput build() {
			return new GenCMTableOutput(returnCodesMappingTable, shortChoiceReturnCodes, shortVoteCastReturnCodes);
		}
	}
}
