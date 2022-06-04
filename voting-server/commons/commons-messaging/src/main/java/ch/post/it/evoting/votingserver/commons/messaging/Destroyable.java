/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

/**
 * Destroyable object. Once destroyed the object cannot be used, any attempt to use a destroyed object leads to an exception.
 */
interface Destroyable {
	/**
	 * Destroys the object and releases all allocated resource.
	 *
	 * @throws MessagingException
	 */
	void destroy() throws MessagingException;
}
