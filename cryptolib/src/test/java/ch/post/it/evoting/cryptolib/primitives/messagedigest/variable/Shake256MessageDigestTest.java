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

class Shake256MessageDigestTest {

	private static final byte[] MESSAGE = "This is what will be digested".getBytes(StandardCharsets.UTF_8);
	private static final String MESSAGE_DIGEST_BASE64 = "MPCFved3bjAFpKwUVDfuhqqkoeDogpQ2SKXeOUpoWUA=";
	private static final byte[] MESSAGE_DIGEST_BYTES = Base64.getDecoder().decode(MESSAGE_DIGEST_BASE64);
	private static final int MINIMUM_OUTPUT_BIT_LENGTH = 256;
	private static final Base64.Encoder base64Encoder = Base64.getEncoder();

	private final Shake256MessageDigest sut = new Shake256MessageDigest();

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
		final int outputBitLength = MINIMUM_OUTPUT_BIT_LENGTH + 1;
		final BitSet output = sut.digest(MESSAGE, outputBitLength);

		// Ensure there are no more bits than expected.
		assertEquals(1, new BigInteger(output.toByteArray()).compareTo(BigInteger.ONE.shiftLeft(outputBitLength)));
		assertFalse(outputBitLength > output.size());

		assertEquals("MPCFved3bjAFpKwUVDfuhqqkoeDogpQ2SKXeOUpoWUAB", base64Encoder.encodeToString(output.toByteArray()));
	}

	@Test
	void testLargeOutputLength() {
		final int outputBitLength = 1048576;
		final BitSet output = sut.digest(MESSAGE, outputBitLength);

		assertEquals(outputBitLength, output.length());
	}

	@Test
	void testIncreasingLengths() {
		final byte[] input = "This is the input".getBytes(StandardCharsets.UTF_8);
		final BitSet output32 = sut.digest(new ByteArrayInputStream(input), 256);
		final BitSet output64 = sut.digest(new ByteArrayInputStream(input), 384);
		final BitSet output48 = sut.digest(new ByteArrayInputStream(input), 512);

		// Different lengths produce the same initial digest.
		assertEquals(output32, output48.get(0, 256));
		assertEquals(output32, output64.get(0, 256));
	}

	@Test
	void testLargeMessage() throws IOException {
		final int messageSize = 1_000_000; // 1 MB
		final int outputBitLength = MINIMUM_OUTPUT_BIT_LENGTH;
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

		final byte[] expected = Base64.getDecoder().decode("LL9kFAPa3r5qCPN+7m8DNPi/k5pFrKhylgvbhLXBHlY=");
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
		final BitSet output = sut.digest(new byte[Shake256MessageDigest.MINIMUM_MESSAGE_LENGTH], outputBitLength);

		// There should be no more than `outputBitLength` bits.
		assertTrue(outputBitLength >= output.length());
	}

	private String getDigest(final byte[] initialData) {
		final int iterations = 100000;
		final int digestLength = 256;

		byte[] digesterInput = initialData;
		for (int i = 0; i < iterations; i++) {
			digesterInput = sut.digest(digesterInput, digestLength).toByteArray();
		}

		return base64Encoder.encodeToString(digesterInput);
	}
}
