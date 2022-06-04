/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication;

/**
 * Interface for defining creation strategies of the authentication key
 */
public interface ChallengeGenerator {

	/**
	 * @return
	 */
	ExtraParams generateExtraParams();
}
