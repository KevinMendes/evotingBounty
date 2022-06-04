/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.task;

import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;

@Service
public class FileCreationTaskService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileCreationTaskService.class);

	private final ObjectMapper objectMapper;
	private final ExecutorService executorService;
	private final FileCreationTaskRepository fileCreationTaskRepository;

	public FileCreationTaskService(
			final ObjectMapper objectMapper,
			final ExecutorService executorService,
			final FileCreationTaskRepository fileCreationTaskRepository) {
		this.objectMapper = objectMapper;
		this.executorService = executorService;
		this.fileCreationTaskRepository = fileCreationTaskRepository;
	}

	public void executeFileCreationTask(final String businessId, final FileCreationTaskType fileType, final String taskDescription,
			final Runnable fn) {
		final FileCreationTask task = new FileCreationTask.Builder()
				.setFileType(fileType)
				.setBusinessId(businessId)
				.setDescription(taskDescription)
				.setStatus(FileCreationTaskStatus.STARTED)
				.build();

		// Check if task already exists
		final Optional<FileCreationTask> taskExists = findById(task.getId());
		if (taskExists.isPresent()) {
			LOGGER.warn("{} already exists. [taskId: {}]", task.getDescription(), task.getId());
			return;
		}

		// Save the new task
		save(task);

		// Submit task to executor
		executorService.submit(() -> taskExecution(fn, task));
		LOGGER.info("{} submitted to executor service. taskId: {}]", task.getDescription(), task.getId());
	}

	private void taskExecution(final Runnable fn, final FileCreationTask task) {
		// Execute the task function.
		fn.run();

		// Change status and update task
		task.setStatus(FileCreationTaskStatus.COMPLETED);
		update(task);
		LOGGER.info("{} finished. [taskId: {}]", task.getDescription(), task.getId());
	}

	public FileCreationTaskStatus getStatus(final String taskId) {
		// Check if task exists
		final Optional<FileCreationTask> taskExists = findById(taskId);
		if (taskExists.isPresent()) {
			return taskExists.get().getStatus();
		}
		return FileCreationTaskStatus.UNKNOWN;
	}

	private Optional<FileCreationTask> findById(final String entityId) {
		final String fileCreationTask = fileCreationTaskRepository.find(entityId);
		if (StringUtils.isEmpty(fileCreationTask) || JsonConstants.EMPTY_OBJECT.equals(fileCreationTask)) {
			return Optional.empty();
		}

		try {
			return Optional.of(objectMapper.readValue(fileCreationTask, FileCreationTask.class));
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void save(final FileCreationTask fileCreationTask) {
		try {
			final String json = objectMapper.writeValueAsString(fileCreationTask);
			fileCreationTaskRepository.save(json);
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void update(final FileCreationTask fileCreationTask) {
		try {
			final String json = objectMapper.writeValueAsString(fileCreationTask);
			fileCreationTaskRepository.update(json);
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}

}
