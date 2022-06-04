/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of {@link CompositeInputStream}.
 */
class CompositeInputStreamTest {

	private InputStream part1;
	private InputStream part2;
	private InputStream part3;
	private CompositeInputStream stream;

	@BeforeEach
	void setUp() throws IOException {
		part1 = mock(InputStream.class);
		when(part1.read()).thenReturn(1, -1);
		part2 = mock(InputStream.class);
		when(part2.read()).thenReturn(2, -1);
		part3 = mock(InputStream.class);
		when(part3.read()).thenReturn(3, -1);
		stream = new CompositeInputStream(part1, part2, part3);
	}

	@Test
	void testRead() throws IOException {
		assertEquals(1, stream.read());
		assertEquals(2, stream.read());
		assertEquals(3, stream.read());
		assertEquals(-1, stream.read());
	}

	@Test
	void testReadIOException() throws IOException {
		when(part2.read()).thenThrow(new IOException("test"));
		stream.read();
		assertThrows(IOException.class, () -> stream.read());
	}

	@Test
	void testClose() throws IOException {
		stream.close();
		verify(part1).close();
		verify(part2).close();
		verify(part3).close();
	}

	@Test
	void testCloseIOException() throws IOException {
		doThrow(new IOException("test")).when(part2).close();
		assertThrows(IOException.class, () -> stream.close());

		verify(part1).close();
		verify(part3).close();
	}
}
