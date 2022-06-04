/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.messagedigest.variable;

/**
 * Variable-output-length message digest based on the Bouncy Castle implementation of SHAKE with 256-bit security.
 *
 * <p>This implementation is thread-safe.
 */
public class Shake256MessageDigest extends ShakeMessageDigest {
	private static final int DIGEST_SECURITY_BITS = 256;

	@Override
	int getDigestSecurityBits() {
		return DIGEST_SECURITY_BITS;
	}
}
