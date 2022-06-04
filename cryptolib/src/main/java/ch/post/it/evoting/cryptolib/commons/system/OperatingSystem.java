/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.system;

/**
 * Represents a type of operating system.
 *
 * <p>In this enum, all operating system are viewed as being of one of the following types:
 *
 * <ol>
 *   <li>UNIX.
 *   <li>WINDOWS.
 *   <li>OTHER.
 * </ol>
 */
public enum OperatingSystem {
	UNIX,
	WINDOWS,
	OTHER;

	private static OperatingSystem current = OTHER;

	static {
		final String name = System.getProperty("os.name").toLowerCase();
		if (name.startsWith("linux") || name.startsWith("mac")) {
			current = UNIX;
		} else if (name.startsWith("windows")) {
			current = WINDOWS;
		}
	}

	/**
	 * Returns the current operation system.
	 *
	 * @return the current operation system.
	 */
	public static OperatingSystem current() {
		return current;
	}

	/**
	 * Returns whether the operation system is the current one.
	 *
	 * @return
	 */
	public final boolean isCurrent() {
		return this == current();
	}
}
