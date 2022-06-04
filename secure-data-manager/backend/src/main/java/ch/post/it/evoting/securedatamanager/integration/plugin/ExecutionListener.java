/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.integration.plugin;

public class ExecutionListener {

	private int error;
	private String progress;
	private String message;

	public void onError(int error) {
		this.error = error;
	}

	public int getError() {
		return error;
	}

	public void onProgress(String progress) {
		this.progress = progress;

	}

	public String getProgress() {
		return progress;
	}

	public String getMessage() {
		return message;
	}

	public void onMessage(String message) {
		this.message = message;
	}
}
