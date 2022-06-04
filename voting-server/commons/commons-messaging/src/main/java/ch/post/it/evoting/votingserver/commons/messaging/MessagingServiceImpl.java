/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static java.util.Objects.requireNonNull;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;

import javax.annotation.Nonnegative;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.ConnectionFactory;

/**
 * Implementation of {@link MessagingService}.
 */
@ThreadSafe
public final class MessagingServiceImpl implements MessagingService {
	private static final String DESTINATION_IS_NULL = "Destination is null.";
	private static final String EXECUTOR_IS_NULL = "Executor is null.";
	private static final String LISTENER_IS_NULL = "Listener is null.";
	private static final String MESSAGE_IS_NULL = "Message is null.";
	private static final Logger LOGGER = LoggerFactory.getLogger(MessagingServiceImpl.class);

	private final ConnectionManager connectionManager;
	private final SenderManager senderManager;
	private final ReceiverManager receiverManager;

	/**
	 * Constructor. For internal use only.
	 *
	 * @param connectionManager
	 * @param senderManager
	 * @param receiverManager
	 */
	MessagingServiceImpl(ConnectionManager connectionManager, SenderManager senderManager, ReceiverManager receiverManager) {
		LOGGER.info("Starting the messaging service...");
		this.connectionManager = connectionManager;
		this.senderManager = senderManager;
		this.receiverManager = receiverManager;
		LOGGER.info("The messaging service is started");
	}

	@Override
	public void createReceiver(Destination destination, MessageListener listener) throws MessagingException {

		createReceiver(destination, listener, CurrentThreadExecutor.getInstance());
	}

	@Override
	public void createReceiver(Destination destination, MessageListener listener, Executor executor) throws MessagingException {
		requireNonNull(destination, DESTINATION_IS_NULL);
		requireNonNull(listener, LISTENER_IS_NULL);
		requireNonNull(executor, EXECUTOR_IS_NULL);

		LOGGER.info("Creating message receiver for {} - (listener: {}, executor: {})", destination.name(), listener, executor);
		receiverManager.createReceiver(destination, listener, executor);
		LOGGER.info("Now listening to messages from {}", destination.name());
	}

	@Override
	public void destroyReceiver(Destination destination, MessageListener listener) throws MessagingException {
		requireNonNull(destination, DESTINATION_IS_NULL);
		requireNonNull(listener, LISTENER_IS_NULL);

		receiverManager.destroyReceiver(destination, listener);
		LOGGER.info("No longer listening to {}", destination.name());
	}

	@Override
	public void send(Destination destination, Object message) throws MessagingException {
		requireNonNull(destination, DESTINATION_IS_NULL);
		requireNonNull(message, MESSAGE_IS_NULL);

		Sender sender = senderManager.acquireSender();
		try {
			sender.send(destination, message);
		} finally {
			senderManager.releaseSender(sender);
		}
	}

	@Override
	public void shutdown() {
		try {
			LOGGER.info("Stopping the messaging service...");
			senderManager.destroy();
			receiverManager.destroy();
			connectionManager.destroy();
			LOGGER.info("The messaging service has been stopped");
		} catch (MessagingException e) {
			LOGGER.warn("Failed to shutdown messaging service.", e);
		}
	}

	/**
	 * Builder for creating {@link MessagingServiceImpl} instances.
	 */
	public static final class Builder {
		private static final String HOST_NAME_IS_NULL = "Host name is null.";

		// Ignore 'PASSWORD' detected in this expression, review this potentially hard-coded credential.' Sonar's rule for this sentence.
		@SuppressWarnings("squid:S2068")
		private static final String PASSWORD_IS_NULL = "Password is null.";

		private static final String USERNAME_IS_NULL = "Username is null.";
		private static final String VIRTUAL_HOST_IS_NULL = "Virtual host is null.";

		private String hostName;

		private int port;

		private String virtualHost;

		private String username;

		private String password;

		private int senderPoolSize;

		private boolean useSSL;

		/**
		 * Builds a new {@link MessagingServiceImpl} instance
		 *
		 * @return a new instance.
		 */
		public MessagingServiceImpl build() {
			requireNonNull(hostName, HOST_NAME_IS_NULL);
			requireNonNull(virtualHost, VIRTUAL_HOST_IS_NULL);
			requireNonNull(username, USERNAME_IS_NULL);
			requireNonNull(password, PASSWORD_IS_NULL);
			if (port < 0) {
				throw new IllegalStateException("Port is negative.");
			}
			if (senderPoolSize < 0) {
				throw new IllegalStateException("Sender pool size is negative.");
			}
			ConnectionManager connectionManager = newConnectionManager();
			Codec codec = getCodec();
			SenderManager senderManager = newSenderManager(connectionManager, codec);
			ReceiverManager receiverManager = newReceiverManager(connectionManager, codec);
			return new MessagingServiceImpl(connectionManager, senderManager, receiverManager);
		}

		/**
		 * Sets the host name.
		 *
		 * @param hostName the host name
		 * @return this instance.
		 */
		public Builder setHostName(String hostName) {
			this.hostName = hostName;
			return this;
		}

		/**
		 * Sets the password.
		 *
		 * @param password the password
		 * @return this instance.
		 */
		public Builder setPassword(String password) {
			this.password = password;
			return this;
		}

		/**
		 * Sets the port.
		 *
		 * @param port the port
		 * @return this instance.
		 */
		public Builder setPort(
				@Nonnegative
						int port) {
			this.port = port;
			return this;
		}

		/**
		 * Sets the sender pool size.
		 *
		 * @param senderPoolSize the sender pool size
		 * @return this instance.
		 */
		public Builder setSenderPoolSize(
				@Nonnegative
						int senderPoolSize) {
			this.senderPoolSize = senderPoolSize;
			return this;
		}

		/**
		 * Sets the username.
		 *
		 * @param username the username
		 * @return this instance.
		 */
		public Builder setUsername(String username) {
			this.username = username;
			return this;
		}

		/**
		 * Sets the use of SSL.
		 *
		 * @return this instance.
		 */
		public Builder useSSL() {
			useSSL = true;
			return this;
		}

		/**
		 * Sets the virtual host.
		 *
		 * @param virtualHost the virtual host
		 * @return this instance.
		 */
		public Builder setVirtualHost(String virtualHost) {
			this.virtualHost = virtualHost;
			return this;
		}

		private Codec getCodec() {
			return CodecImpl.getInstance();
		}

		private ConnectionManager newConnectionManager() {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(hostName);
			factory.setPort(port);
			factory.setVirtualHost(virtualHost);
			factory.setUsername(username);
			factory.setPassword(password);
			if (useSSL) {
				try {
					factory.useSslProtocol();
					factory.enableHostnameVerification();
				} catch (KeyManagementException | NoSuchAlgorithmException e) {
					throw new IllegalStateException("Failed to create a connection manager.", e);
				}
			}
			return new ConnectionManagerImpl(factory);
		}

		private ReceiverManager newReceiverManager(ConnectionManager connectionManager, Codec codec) {
			ReceiverFactory factory = new ReceiverFactoryImpl(connectionManager, codec);
			return new ReceiverManagerImpl(factory);
		}

		private SenderManager newSenderManager(ConnectionManager connectionManager, Codec codec) {
			SenderFactory factory = new SenderFactoryImpl(connectionManager, codec);
			return new SenderManagerImpl(factory, senderPoolSize);
		}
	}
}
