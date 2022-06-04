/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.tracking;

public interface TrackIdGenerator {

	/**
	 * Generate a 16-character Base32 TrackId
	 *
	 * @return a new randomly generated TrackId
	 */
	String generate();

	/**
	 * Generate a variable length Base32 TrackId
	 *
	 * @return a new randomly generated TrackId
	 */
	String generate(int length);

}
