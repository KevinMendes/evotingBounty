/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MixDecKeysResponse {

	/* key: electoralAuthorityId, value: mixDecryptKey */
	private Map<String, List<String>> electoralAuthorityMixDecryptKeys;

	private List<String> errors;

	public Map<String, List<String>> getElectoralAuthorityMixDecryptKeys() {
		return electoralAuthorityMixDecryptKeys;
	}

	public void setElectoralAuthorityMixDecryptKeys(Map<String, List<String>> electoralAuthorityMixDecryptKeys) {
		this.electoralAuthorityMixDecryptKeys = electoralAuthorityMixDecryptKeys;
	}

	/**
	 * Gets the errors.
	 *
	 * @return the errors
	 */
	public List<String> getErrors() {
		return errors;
	}

	/**
	 * Sets the errors.
	 *
	 * @param errors the errors to set
	 */
	public void setErrors(final List<String> errors) {
		this.errors = errors;
	}

}
