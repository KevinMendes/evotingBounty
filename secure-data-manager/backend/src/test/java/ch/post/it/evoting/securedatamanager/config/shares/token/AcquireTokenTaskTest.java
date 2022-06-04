/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.securedatamanager.config.shares.token;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.Slot;
import iaik.pkcs.pkcs11.Token;
import iaik.pkcs.pkcs11.TokenException;

@ExtendWith(MockitoExtension.class)
class AcquireTokenTaskTest {

	private AcquireTokenTask acquireTokenTask;

	@Mock
	private Module pkcs11Module;

	@Mock
	private GuiOutput guiOutput;

	@Mock
	private Slot slot;

	@BeforeEach
	void setUp() {
		acquireTokenTask = new AcquireTokenTask(pkcs11Module, guiOutput);
	}

	@Test
	void tokenNotFoundOnceAndAbort() throws TokenException {
		when(pkcs11Module.getSlotList(anyBoolean())).thenReturn(new Slot[0]);

		acquireTokenTask.abort();
		acquireTokenTask.call();

		verify(guiOutput).noTokenPresent();
		verify(guiOutput, times(0)).tooManyTokens(anyInt());
	}

	@Test
	void tokenNotFoundThenFound() throws TokenException {
		when(slot.getToken()).thenReturn(mock(Token.class));
		when(pkcs11Module.getSlotList(anyBoolean())).thenReturn(new Slot[0], new Slot[] { slot });

		final Token token = acquireTokenTask.call();

		verify(guiOutput).noTokenPresent();
		verify(guiOutput, times(0)).tooManyTokens(anyInt());
		assertNotNull(token);
	}

	@Test
	void tokenNotFoundTryOnceAndAbort() throws TokenException {
		when(pkcs11Module.getSlotList(anyBoolean())).thenReturn(new Slot[0]);

		acquireTokenTask = new AcquireTokenTask(pkcs11Module, guiOutput, false);

		acquireTokenTask.abort();
		acquireTokenTask.call();

		verify(guiOutput).noTokenPresent();
		verify(guiOutput, times(0)).tooManyTokens(anyInt());
	}

	@Test
	void tokenFoundOnce() throws TokenException {
		when(slot.getToken()).thenReturn(mock(Token.class));
		when(pkcs11Module.getSlotList(anyBoolean())).thenReturn(new Slot[] { slot });

		final Token token = acquireTokenTask.call();

		verify(guiOutput, times(0)).noTokenPresent();
		verify(guiOutput, times(0)).tooManyTokens(anyInt());
		assertNotNull(token);
	}

	@Test
	void tokenFoundThrowsExceptionOnce() throws TokenException {
		when(slot.getToken()).thenThrow(new TokenException());
		when(pkcs11Module.getSlotList(anyBoolean())).thenReturn(new Slot[] { slot });

		acquireTokenTask.abort();
		final Token token = acquireTokenTask.call();

		assertNull(token);
	}

	@Test
	void tooManyTokensOnceAndAbort() throws TokenException {
		when(pkcs11Module.getSlotList(anyBoolean())).thenReturn(new Slot[2]);

		acquireTokenTask.abort();
		acquireTokenTask.call();

		verify(guiOutput, times(1)).tooManyTokens(anyInt());
	}
}
