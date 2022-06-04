/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Implementation of {@link ReceiverManager}.
 */
class ReceiverManagerImpl implements ReceiverManager {
	private final Map<ReceiverId, Receiver> receivers = new HashMap<>();

	private final ReceiverFactory factory;

	private boolean destroyed;

	/**
	 * Constructor.
	 *
	 * @param factory
	 */
	public ReceiverManagerImpl(ReceiverFactory factory) {
		this.factory = factory;
	}

	@Override
	public synchronized <T> void createReceiver(Destination destination, MessageListener listener, Executor executor) throws MessagingException {
		if (destroyed) {
			throw new MessagingException("Receiver manager is already destroyed.");
		}
		ReceiverId id = new ReceiverId(destination, listener);
		if (!receivers.containsKey(id)) {
			Receiver receiver = factory.newReceiver(destination, listener, executor);
			try {
				receiver.receive();
			} catch (MessagingException e) {
				try {
					receiver.destroy();
				} catch (MessagingException suppressed) {
					e.addSuppressed(suppressed);
				}
				throw e;
			}
			receivers.put(id, receiver);
		}
	}

	@Override
	public synchronized void destroy() throws MessagingException {
		if (!destroyed) {
			for (Receiver receiver : receivers.values()) {
				receiver.destroy();
			}
			receivers.clear();
			destroyed = true;
		}
	}

	@Override
	public synchronized void destroyReceiver(Destination destination, MessageListener listener) throws MessagingException {
		if (!destroyed) {
			ReceiverId id = new ReceiverId(destination, listener);
			Receiver receiver = receivers.remove(id);
			if (receiver != null) {
				receiver.destroy();
			}
		}
	}
}
