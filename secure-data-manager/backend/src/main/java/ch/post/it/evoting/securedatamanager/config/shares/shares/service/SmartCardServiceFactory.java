/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.shares.service;

/**
 * Factory that instantiates the proper SmartCardService, whether the smart cards will be used or not . Last scenario will be provided for automated
 * e2e
 */
public final class SmartCardServiceFactory {

	private SmartCardServiceFactory() {
	}

	/**
	 * Returns an implementation of {@link SmartCardService} which uses physical smart cards depending on the specified flag.
	 *
	 * @param useSmartCards the flag
	 * @return the instance.
	 */
	public static SmartCardService getSmartCardService(final boolean useSmartCards) {
		return useSmartCards ? new DefaultSmartCardService() : new FileSystemSmartCardService();
	}
}
