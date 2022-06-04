/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.exceptions;

/**
 * The class {@code CryptoLibException} is the superclass of those <em>unchecked</em> exceptions that can be thrown from any call to cryptolib. They
 * do <em>not</em> need to be declared in a {@code throws} clause.
 *
 * @see RuntimeException
 */
public class CryptoLibException extends RuntimeException {

	private static final long serialVersionUID = 160835072294679014L;

	/**
	 * Creates a {@code CryptoLibException} with the given detail message.
	 *
	 * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
	 * @see RuntimeException#RuntimeException(String)
	 */
	public CryptoLibException(final String message) {
		super(message);
	}

	/**
	 * Creates a {@code CryptoLibException} with the specified detail message and cause.
	 *
	 * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
	 * @param cause   The cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt> value is permitted, and
	 *                indicates that the cause is nonexistent or unknown.)
	 * @see RuntimeException#RuntimeException(String, Throwable)
	 */
	public CryptoLibException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a {@code CryptoLibException} with the specified cause and a detail message of
	 * <tt>(cause==null ? null : cause.toString())</tt> (which typically contains the class and detail
	 * message of <tt>cause</tt>).
	 *
	 * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt> value is permitted, and
	 *              indicates that the cause is nonexistent or unknown.)
	 */
	public CryptoLibException(final Throwable cause) {
		super(cause);
	}
}
