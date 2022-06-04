/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class ChallengeTest {

	public static final int LENGTH = 16;

	@Test
	public void testChallenge() {

		final Challenge challenge = new Challenge(LENGTH);
		assertNotNull(challenge);
		assertFalse(StringUtils.isEmpty(challenge.getChallengeValue()));
	}

	@Test
	public void testChallengeINT() {

		final Challenge challenge = new Challenge(LENGTH);
		assertNotNull(challenge);
		assertFalse(StringUtils.isEmpty(challenge.getChallengeValue()));
	}

	@Test
	public void testChallengeString() {

		final Challenge challenge = new Challenge(LENGTH);
		assertNotNull(challenge);
		assertFalse(StringUtils.isEmpty(challenge.getChallengeValue()));
	}
}
