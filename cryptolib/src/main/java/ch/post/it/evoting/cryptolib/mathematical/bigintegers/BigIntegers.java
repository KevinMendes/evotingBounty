/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.bigintegers;

import java.math.BigInteger;

/**
 * Utility class which provides some extensions and optimizations for standard {@link BigInteger}.
 *
 * <p>The native library (.dll/.so) is also loaded, if available.
 *
 * <p>The loading process has the following features:
 *
 * <ul>
 *   <li>The library is loaded once during static initialization.
 *   <li>The library is loaded by it's "generic" name as described in the
 *       contract of {@link System#loadLibrary(String)}.
 *   <li>Possible {@link UnsatisfiedLinkError} is not reported as a fatal problem, it means that the
 *       library is not available.
 *   <li>System property {@code -Dch.post.it.evoting.math.jni.debug=true} allows to trace how the library is
 *       loaded in the {@link System#err}
 * </ul>
 */
public final class BigIntegers {

	private BigIntegers() {
	}

	/**
	 * Multiplies two numbers modulo given modulus. This is a shortcut for {@code a.multiply(b).mod(modulus);}.
	 *
	 * @param number1 the first number
	 * @param number2 the second number
	 * @param modulus the modulus, must be positive
	 * @return the product
	 * @throws ArithmeticException the modulus is not positive.
	 */
	public static BigInteger modMultiply(final BigInteger number1, final BigInteger number2, final BigInteger modulus) {
		return number1.multiply(number2).mod(modulus);
	}

	/**
	 * Executes {@link BigInteger#modPow(BigInteger, BigInteger)} for given base, exponent and modulus. See {@link BigInteger#modPow(BigInteger,
	 * BigInteger)} for details.
	 *
	 * @param base     the base
	 * @param exponent the exponent
	 * @param modulus  the modulus, must be positive
	 * @return the value
	 * @throws ArithmeticException the modulus is not positive or the exponent is negative and the base is not relatively prime to the modulus.
	 */
	public static BigInteger modPow(final BigInteger base, final BigInteger exponent, final BigInteger modulus) {
		return base.modPow(exponent, modulus);
	}

	/**
	 * Multiplies two given factors. See {@link BigInteger#multiply(BigInteger)} for details.
	 *
	 * @param number1 the first factor
	 * @param number2 the second factor
	 * @return the product.
	 */
	public static BigInteger multiply(final BigInteger number1, final BigInteger number2) {
		return number1.multiply(number2);
	}

}
