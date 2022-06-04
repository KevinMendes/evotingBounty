/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.Callable;

public class ExactlyOnceTask {

	private final String correlationId;
	private final String contextId;
	private final String context;
	private final Callable<byte[]> task;
	private final byte[] requestContent;

	private ExactlyOnceTask(String correlationId, String contextId, String context, Callable<byte[]> task, final byte[] requestContent) {
		this.correlationId = correlationId;
		this.contextId = contextId;
		this.context = context;
		this.task = task;
		this.requestContent = requestContent;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public String getContextId() {
		return contextId;
	}

	public String getContext() {
		return context;
	}

	public Callable<byte[]> getTask() {
		return task;
	}

	public byte[] getRequestContent() {
		return requestContent;
	}

	public static class Builder {
		private String correlationId;
		private String contextId;
		private String context;
		private Callable<byte[]> task;
		private byte[] requestContent;

		/**
		 * Sets the correlation id which is identical for all subtask of a business operation execution.
		 * <p>
		 *     The combination of correlation id, context id and context uniquely identify this task.
		 * </p>
		 *
		 * @param correlationId The correlation id to be used for building the ExactlyOnceTask.
		 * @return this Builder with the correlationId set.
		 */
		public Builder setCorrelationId(String correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		/**
		 * Sets the context id which uniquely identifies the resource being operated on.
		 * <p>
		 *     The combination of correlation id, context id and context uniquely identify this task.
		 * </p>
		 *
		 * @param contextId The context id to be used for building the ExactlyOnceTask.
		 * @return this Builder with the contextId set.
		 */
		public Builder setContextId(String contextId) {
			this.contextId = contextId;
			return this;
		}

		/**
		 * Sets the context which identifies the business operation being executed.
		 * <p>
		 *     The combination of correlation id, context id and context uniquely identify this task.
		 * </p>
		 *
		 * @param context The context to be used for building the ExactlyOnceTask.
		 * @return this Builder with the context set.
		 */
		public Builder setContext(String context) {
			this.context = context;
			return this;
		}

		/**
		 * Sets the task that is guaranteed to be successfully processed exactly once. If the task fails, it is guaranteed not to be persisted.
		 *
		 * @param task The task to be executed in the ExactlyOnceTask.
		 * @return the Builder with the task set.
		 */
		public Builder setTask(Callable<byte[]> task) {
			this.task = task;
			return this;
		}

		/**
		 * Sets the request content which is the input to the process.
		 *
		 * @param requestContent The message to be processed in the ExactlyOnceTask.
		 * @return the Builder with the message set.
		 */
		public Builder setRequestContent(byte[] requestContent) {
			this.requestContent = requestContent;
			return this;
		}

		/**
		 * Instantiates an ExactlyOnceTask with the fields set according to the Builder's fields.
		 *
		 * @return an ExactlyOnceTask.
		 * @throws NullPointerException if any of the Builder's fields is null
		 */
		public ExactlyOnceTask build() {
			checkNotNull(correlationId);
			checkNotNull(contextId);
			checkNotNull(context);
			checkNotNull(task);
			checkNotNull(requestContent);

			return new ExactlyOnceTask(correlationId, contextId, context, task, requestContent);
		}
	}
}
