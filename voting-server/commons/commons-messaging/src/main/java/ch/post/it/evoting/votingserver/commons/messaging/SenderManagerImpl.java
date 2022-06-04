/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.annotation.Nonnegative;

/**
 * Implementation of {@link SenderManager}.
 */
class SenderManagerImpl implements SenderManager {
	private final Deque<Sender> availableSenders = new LinkedList<>();

	private final Set<Sender> acquiredSenders = new HashSet<>();

	private final SenderFactory factory;

	private final int poolSize;

	private boolean destroyed;

	/**
	 * Constructor.
	 *
	 * @param factory
	 * @param poolSize
	 */
	public SenderManagerImpl(SenderFactory factory,
			@Nonnegative
					int poolSize) {
		this.factory = factory;
		this.poolSize = poolSize;
	}

	@Override
	public synchronized Sender acquireSender() throws MessagingException {
		if (destroyed) {
			throw new MessagingException("Sender manager is already destroyed.");
		}
		Sender sender = availableSenders.poll();
		if (sender == null) {
			sender = factory.newSender();
		}
		acquiredSenders.add(sender);
		return sender;
	}

	@Override
	public synchronized void destroy() throws MessagingException {
		if (!destroyed) {
			for (Sender sender : availableSenders) {
				sender.destroy();
			}
			availableSenders.clear();
			for (Sender sender : acquiredSenders) {
				sender.destroy();
			}
			acquiredSenders.clear();
			destroyed = true;
		}
	}

	@Override
	public synchronized void releaseSender(Sender sender) throws MessagingException {
		if (!destroyed) {
			acquiredSenders.remove(sender);
			if (sender.isValid() && availableSenders.size() < poolSize) {
				availableSenders.offer(sender);
			} else {
				sender.destroy();
			}
		}
	}
}
