/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.validation;

/**
 * Common Bean for validations result
 */
public class ContextValidationResult {

	private final ValidationType validationType;
	private final boolean result;
	private final String contextName;

	private ContextValidationResult(ValidationType validationType, boolean result, String contextName) {
		this.validationType = validationType;
		this.result = result;
		this.contextName = contextName;
	}

	public ValidationType getValidationType() {
		return validationType;
	}

	public boolean isResult() {
		return result;
	}

	public String getContextName() {
		return contextName;
	}

	public static class Builder {

		private ValidationType validationType;

		private boolean result;

		private String contextName;

		public Builder setValidationType(ValidationType validationType) {
			this.validationType = validationType;
			return this;
		}

		public Builder setResult(boolean result) {
			this.result = result;
			return this;
		}

		public Builder setContextName(String contextName) {
			this.contextName = contextName;
			return this;
		}

		public ContextValidationResult build() {
			return new ContextValidationResult(validationType, result, contextName);
		}
	}
}
