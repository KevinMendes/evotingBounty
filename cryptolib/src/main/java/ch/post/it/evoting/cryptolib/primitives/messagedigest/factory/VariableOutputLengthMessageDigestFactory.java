/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.messagedigest.factory;

import java.util.Objects;

import ch.post.it.evoting.cryptolib.CryptolibFactory;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.variable.Shake128MessageDigest;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.variable.VariableOutputLengthMessageDigest;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.variable.VariableOutputLengthMessageDigestAlgorithm;

/**
 * Produces variable-output-length message digest objects.
 */
public class VariableOutputLengthMessageDigestFactory extends CryptolibFactory {

	private VariableOutputLengthMessageDigestFactory() {
	}

	/**
	 * Creates a variable-length-output message digest
	 *
	 * @return a variable-length-output message digest according to policy
	 */
	public static VariableOutputLengthMessageDigest create(final VariableOutputLengthMessageDigestAlgorithm algorithm) {
		Objects.requireNonNull(algorithm, "The variable-output-length message digest algorithm is required");

		if (VariableOutputLengthMessageDigestAlgorithm.SHAKE128.equals(algorithm)) {
			return new Shake128MessageDigest();
		}

		throw new IllegalArgumentException("Unsupported algorithm");
	}
}
