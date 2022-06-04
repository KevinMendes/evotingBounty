/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.distributedprocessing.commands;

import java.io.Serializable;
import java.util.Objects;

public class CommandId implements Serializable {

	private String contextId;
	private String context;
	private String correlationId;
	private Integer nodeId;

	public CommandId(final Builder builder) {
		this.contextId = builder.contextId;
		this.context = builder.context;
		this.correlationId = builder.correlationId;
		this.nodeId = builder.nodeId;
	}

	protected CommandId() {
	}

	public String getContextId() {
		return contextId;
	}

	public String getContext() {
		return context;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final CommandId commandId = (CommandId) o;
		return Objects.equals(contextId, commandId.contextId) &&
				Objects.equals(context, commandId.context)
				&& Objects.equals(correlationId, commandId.correlationId) &&
				Objects.equals(nodeId, commandId.nodeId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(contextId, context, correlationId, nodeId);
	}

	@Override
	public String toString() {
		return "CommandId{" +
				"contextId='" + contextId + '\'' +
				", context='" + context + '\'' +
				", correlationId='" + correlationId + '\'' +
				", nodeId=" + nodeId +
				'}';
	}

	public static class Builder {
		private String contextId;
		private String context;
		private String correlationId;
		private Integer nodeId;

		public Builder() {
			// Do nothing
		}

		public Builder contextId(String value) {
			this.contextId = value;
			return this;
		}

		public Builder context(String value) {
			this.context = value;
			return this;
		}

		public Builder correlationId(String value) {
			this.correlationId = value;
			return this;
		}

		public Builder nodeId(Integer value) {
			this.nodeId = value;
			return this;
		}

		public CommandId build() {

			return new CommandId(this);
		}
	}
}
