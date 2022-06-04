/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.distributedprocessing.commands;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Version;

@Entity
@IdClass(CommandId.class)
public class Command {

	@Id
	private String contextId;

	@Id
	private String context;

	@Id
	private String correlationId;

	@Id
	private Integer nodeId;

	private byte[] requestPayload;

	private LocalDateTime requestDateTime;

	private byte[] responsePayload;

	private LocalDateTime responseDateTime;

	@Version
	private Long changeControlId;

	private Command(final Builder builder) {
		this.contextId = builder.contextId;
		this.context = builder.context;
		this.correlationId = builder.correlationId;
		this.nodeId = builder.nodeId;
		this.requestPayload = builder.requestPayload;
		this.requestDateTime = Objects.nonNull(builder.requestDateTime) ? builder.requestDateTime : LocalDateTime.now();
		this.responsePayload = builder.responsePayload;
		this.responseDateTime = builder.responseDateTime;
		this.changeControlId = builder.changeControlId;
	}

	protected Command() {
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

	public LocalDateTime getRequestDateTime() {
		return requestDateTime;
	}

	public byte[] getRequestPayload() {
		return requestPayload;
	}

	public byte[] getResponsePayload() {
		return responsePayload;
	}

	public void setResponsePayload(final byte[] responsePayload) {
		this.responsePayload = responsePayload;
	}

	public LocalDateTime getResponseDateTime() {
		return responseDateTime;
	}

	public void setResponseDateTime(final LocalDateTime responseDateTime) {
		this.responseDateTime = responseDateTime;
	}

	public Long getChangeControlId() {
		return changeControlId;
	}

	public static class Builder {
		private String contextId;
		private String context;
		private String correlationId;
		private Integer nodeId;
		private byte[] requestPayload;
		private LocalDateTime requestDateTime;
		private byte[] responsePayload;
		private LocalDateTime responseDateTime;
		private Long changeControlId;

		public Builder() {
			// Do nothing
		}

		public Builder commandId(final CommandId value) {
			this.contextId = value.getContextId();
			this.context = value.getContext();
			this.correlationId = value.getCorrelationId();
			this.nodeId = value.getNodeId();
			return this;
		}

		public Builder requestPayload(final byte[] requestPayload) {
			this.requestPayload = requestPayload;
			return this;
		}

		public Builder requestDateTime(final LocalDateTime requestDateTime) {
			this.requestDateTime = requestDateTime;
			return this;
		}

		public Builder responsePayload(final byte[] responsePayload) {
			this.responsePayload = responsePayload;
			return this;
		}

		public Builder responseDateTime(final LocalDateTime responseDateTime) {
			this.responseDateTime = responseDateTime;
			return this;
		}

		public Builder changeControlId(final Long changeControlId) {
			this.changeControlId = changeControlId;
			return this;
		}

		public Command build() {
			return new Command(this);
		}
	}

	public CommandId getCommandId() {
		return new CommandId.Builder()
				.correlationId(this.correlationId)
				.contextId(this.contextId)
				.context(this.context)
				.nodeId(this.nodeId)
				.build();
	}
}
