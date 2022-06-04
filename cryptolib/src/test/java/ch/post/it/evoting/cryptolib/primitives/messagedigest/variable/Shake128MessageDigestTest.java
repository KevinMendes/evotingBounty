/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.messagedigest.variable;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class Shake128MessageDigestTest {

	private static final byte[] MESSAGE = "This is what will be digested".getBytes(StandardCharsets.UTF_8);
	private static final String MESSAGE_DIGEST_BASE64 = "Mrgot88b8g78iufMe0scPA==";
	private static final byte[] MESSAGE_DIGEST_BYTES = Base64.getDecoder().decode(MESSAGE_DIGEST_BASE64);
	private static final int MINIMUM_OUTPUT_BIT_LENGTH = 128;
	private static final Base64.Encoder base64Encoder = Base64.getEncoder();

	private final Shake128MessageDigest sut = new Shake128MessageDigest();

	@Test
	void testInputStream() throws IOException {
		final int bitLength = 1024;
		final BitSet output;
		try (final InputStream is = new ByteArrayInputStream(MESSAGE_DIGEST_BYTES)) {
			output = sut.digest(is, bitLength);
		}

		// There should be no more than `bitLength` bits.
		assertTrue(bitLength >= output.length());
	}

	@Test
	void testNonStandardLength() {
		final int outputBitLength = 16 * Byte.SIZE + 1;
		final BitSet output = sut.digest(MESSAGE, outputBitLength);

		// Ensure there are no more bits than expected.
		assertEquals(1, new BigInteger(output.toByteArray()).compareTo(BigInteger.ONE.shiftLeft(outputBitLength)));
		assertFalse(outputBitLength > output.size());

		assertEquals("Mrgot88b8g78iufMe0scPAE=", base64Encoder.encodeToString(output.toByteArray()));
	}

	@Test
	void testLargeOutputLength() {
		final int outputBitLength = 1048576;
		final BitSet output = sut.digest(MESSAGE, outputBitLength);

		// There should be no more than `outputBitLength` bits.
		assertTrue(outputBitLength >= output.length());
	}

	@Test
	void testIncreasingLengths() {
		final byte[] input = "This is the input".getBytes(StandardCharsets.UTF_8);
		final BitSet output16 = sut.digest(new ByteArrayInputStream(input), 128);
		final BitSet output24 = sut.digest(new ByteArrayInputStream(input), 192);
		final BitSet output32 = sut.digest(new ByteArrayInputStream(input), 256);

		// Different lengths produce the same initial digest.
		assertEquals(output16, output24.get(0, 128));
		assertEquals(output16, output32.get(0, 128));
	}

	@Test
	void testLargeMessage() throws IOException {
		final int messageSize = 1_000_000; // 1 MB
		final int outputBitLength = 128;
		final BitSet output;

		final PipedOutputStream os = new PipedOutputStream();
		try (final PipedInputStream is = new PipedInputStream(os)) {
			new Thread(() -> {
				try {
					for (int i = 0; i < messageSize; i++) {
						os.write(i);
					}
					os.close();
				} catch (final IOException e) {
					throw new IllegalStateException(e);
				}
			}).start();

			output = sut.digest(is, outputBitLength);
		}

		assertFalse(output.length() > outputBitLength);

		final byte[] expected = Base64.getDecoder().decode("hlpXEJIKGtcrmDbJYUrCfw==");
		assertArrayEquals(expected, output.toByteArray());
	}

	@Test
	void testMinimumLength() {
		final int outputLength = MINIMUM_OUTPUT_BIT_LENGTH;
		final BitSet output = sut.digest(MESSAGE, outputLength);

		assertFalse(output.length() > outputLength);
		assertArrayEquals(MESSAGE_DIGEST_BYTES, output.toByteArray());
	}

	@Test
	void testInvalidLength() {
		assertThrows(IllegalArgumentException.class, () -> sut.digest(MESSAGE, 127));
	}

	@Test
	void testThreadSafety() throws InterruptedException, ExecutionException {
		final int concurrentOperations = 16;
		// Calculate some digests, one by one.
		final Map<Integer, String> digests = new HashMap<>();
		for (int i = 0; i < concurrentOperations; i++) {
			digests.put(i, getDigest(new byte[] { (byte) i }));
		}

		// Calculate the same digests, now all at once.
		final ExecutorService executorService = Executors.newFixedThreadPool(concurrentOperations);
		final Map<Integer, Future<String>> results = new HashMap<>(digests.size());
		for (int i = 0; i < concurrentOperations; i++) {
			final int index = i;
			results.put(index, executorService.submit(() -> getDigest(new byte[] { (byte) index })));
		}
		// Let the threads finish.
		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);

		// Test that the digests match whether they were generated in isolation
		// or concurrently.
		for (int i = 0; i < concurrentOperations; i++) {
			final String result = results.get(i).get();
			assertEquals(digests.get(i), result);
		}
	}

	@Test
	void testMessageTooShort() {
		assertThrows(IllegalArgumentException.class, () -> sut.digest(new byte[0], MINIMUM_OUTPUT_BIT_LENGTH));
	}

	@Test
	void testShortestMessage() {
		final int outputBitLength = MINIMUM_OUTPUT_BIT_LENGTH;
		final BitSet output = sut.digest(new byte[Shake128MessageDigest.MINIMUM_MESSAGE_LENGTH], outputBitLength);

		assertEquals(outputBitLength, output.length());
	}

	private String getDigest(final byte[] initialData) {
		final int iterations = 100000;
		final int digestLength = 128;

		byte[] digesterInput = initialData;
		for (int i = 0; i < iterations; i++) {
			digesterInput = sut.digest(digesterInput, digestLength).toByteArray();
		}

		return base64Encoder.encodeToString(digesterInput);
	}
}
