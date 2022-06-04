/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.tracking;

import ch.post.it.evoting.cryptoprimitives.CryptoPrimitives;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitivesService;

public class DefaultTrackIdGenerator implements TrackIdGenerator {

	private static final int LENGTH_IN_CHARS = 16;

	private final CryptoPrimitives cryptoPrimitives = CryptoPrimitivesService.get();

	@Override
	public String generate() {
		return generate(LENGTH_IN_CHARS);
	}

	@Override
	public String generate(final int length) {
		return cryptoPrimitives.genRandomBase32String(length);
	}
}
