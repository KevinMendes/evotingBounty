/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.integration.plugin;

public enum ResultCode {

	UNEXPECTED_ERROR(99),
	GENERAL_ERROR(100),
	INVALID_ACTION(101),
	MISSING_PARAMETER(102),
	PARAMETER_NOT_VALID(103),
	FILE_NOT_FOUND(104),
	ERROR_PARSING_FILE(105),
	CONTENT_NOT_VALID(106),
	USB_NOT_FOUND(107),
	ERROR_COPYING_FILES(108),
	ELECTIONEVENT_ALREADY_IN_USB(109),

	SUCCESS(200);

	private final int value;

	ResultCode(final int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

}
