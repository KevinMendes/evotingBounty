/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.config;

/**
 * Enum representing the different status allowed for handling the smart cards
 */
public enum SmartCardConfig {

	FILE(false),
	SMART_CARD(true);

	private final boolean smartCardEnabled;

	SmartCardConfig(final boolean smartCardEnabled) {
		this.smartCardEnabled = smartCardEnabled;
	}

	public boolean isSmartCardEnabled() {
		return smartCardEnabled;
	}
}
