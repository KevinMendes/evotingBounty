/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands;

import java.io.Console;

/**
 * Provides utilities to read passwords.
 */
public class PasswordReaderUtils {

	/**
	 * Non-public constructor.
	 */
	private PasswordReaderUtils() {
	}

	/**
	 * Reads a password from console.
	 * <p>
	 * Note that this method is to be used when asking a password to the user when opening a Keystore.
	 * </p>
	 */
	public static char[] readPasswordFromConsole() {
		Console console = getConsole();
		return console.readPassword("Enter your password: ");
	}

	private static Console getConsole() {
		Console console = System.console();
		if (console == null) {
			throw new PasswordReaderUtilsException("No console was found");
		}
		return console;
	}
}
