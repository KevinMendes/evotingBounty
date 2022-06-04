/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.task;

public class FileCreationTask {

	private String id;

	private String description;

	private FileCreationTaskStatus status;

	private FileCreationTask(final FileCreationTaskType fileType, final String businessId, final String description,
			final FileCreationTaskStatus status) {
		this.id = String.format("%s-%s", fileType.toString(), businessId);
		this.description = description;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public FileCreationTaskStatus getStatus() {
		return status;
	}

	public void setStatus(final FileCreationTaskStatus status) {
		this.status = status;
	}

	public static class Builder {
		private FileCreationTaskType fileType;
		private String businessId;
		private String description;
		private FileCreationTaskStatus status;

		public Builder setFileType(FileCreationTaskType fileType) {
			this.fileType = fileType;
			return this;
		}

		public Builder setBusinessId(String businessId) {
			this.businessId = businessId;
			return this;
		}

		public Builder setDescription(String description) {
			this.description = description;
			return this;
		}

		public Builder setStatus(FileCreationTaskStatus status) {
			this.status = status;
			return this;
		}

		public FileCreationTask build() {
			return new FileCreationTask(fileType, businessId, description, status);
		}
	}
}
