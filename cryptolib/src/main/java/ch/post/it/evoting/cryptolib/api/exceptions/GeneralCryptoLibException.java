/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.exceptions;

/**
 * The class {@code GeneralCryptoLibException} and its subclasses are <em>checked</em> exceptions. They indicate that some kind of expected condition
 * had happened and the caller application might want to catch.
 */
public class GeneralCryptoLibException extends Exception {

	private static final long serialVersionUID = 6191329250928062475L;

	/**
	 * Constructs a new {@code GeneralCryptoLibException} with the specified detail message.
	 *
	 * @param message The detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
	 * @see Exception#Exception(String)
	 */
	public GeneralCryptoLibException(final String message) {
		super(message);
	}

	/**
	 * Constructs a new {@code GeneralCryptoLibException} with the specified detail message and cause.
	 *
	 * <p>Note that the detail message associated with {@code cause} is <i>not</i> automatically
	 * incorporated in this exception's detail message.
	 *
	 * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
	 * @param cause   The cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt> value is permitted, and
	 *                indicates that the cause is nonexistent or unknown.)
	 * @see Exception#Exception(String, Throwable)
	 */
	public GeneralCryptoLibException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new {@code GeneralCryptoLibException} with the specified cause and a detail message of <tt>(cause==null ? null :
	 * cause.toString())</tt> (which typically contains the class and detail message of <tt>cause</tt>).
	 *
	 * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt> value is permitted, and
	 *              indicates that the cause is nonexistent or unknown.)
	 * @see Exception#Exception(Throwable)
	 */
	public GeneralCryptoLibException(final Throwable cause) {
		super(cause);
	}
}
