/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/**
 * Tests of {@link CodecImpl}.
 */
public class CodecImplTest {

	@Test(expected = InvalidMessageException.class)
	public void testDecodeInvalidContentType() throws InvalidMessageException {
		CodecImpl.getInstance().decode(new byte[] { 2 });

	}

	@Test(expected = InvalidMessageException.class)
	public void testDecodeNoContentType() throws InvalidMessageException {
		CodecImpl.getInstance().decode(new byte[0]);
	}

	@Test
	public void testEncodeDecodeBinary() throws InvalidMessageException {
		byte[] message = { 1, 2, 3 };
		byte[] bytes = CodecImpl.getInstance().encode(message);
		assertArrayEquals(message, (byte[]) CodecImpl.getInstance().decode(bytes));
	}

	@Test(expected = InvalidMessageException.class)
	public void testEncodeInvalid() throws InvalidMessageException {
		CodecImpl.getInstance().encode("Invalid");
	}
}
