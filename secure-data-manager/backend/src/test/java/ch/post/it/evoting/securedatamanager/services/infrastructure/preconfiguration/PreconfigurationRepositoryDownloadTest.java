/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.preconfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ch.post.it.evoting.securedatamanager.services.infrastructure.clients.AdminPortalClient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

@ExtendWith(MockitoExtension.class)
class PreconfigurationRepositoryDownloadTest {

	private final Integer statusCodeNotFound = Status.NOT_FOUND.getStatusCode();

	@InjectMocks
	@Spy
	private final PreconfigurationRepository preconfigurationRepository = new PreconfigurationRepository();

	@Mock
	private AdminPortalClient clientMock;

	@BeforeEach
	void injectValueAnnotatedFields() {
		ReflectionTestUtils.setField(preconfigurationRepository, "tenantId", "tenant");
		ReflectionTestUtils.setField(preconfigurationRepository, "adminPortalBaseURL", "http://1.2.3");
	}

	@Test
	void downloadFailedToGet() throws IOException {
		@SuppressWarnings("unchecked")
		final Call<ResponseBody> exportCallMock = (Call<ResponseBody>) mock(Call.class);
		when(exportCallMock.execute()).thenReturn(Response.error(statusCodeNotFound,
				ResponseBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_JSON), "ok".getBytes(StandardCharsets.UTF_8))));

		doReturn(clientMock).when(preconfigurationRepository).getAdminPortalClient(anyString());
		when(clientMock.export(anyString())).thenReturn(exportCallMock);

		IOException ioException = assertThrows(IOException.class, () -> preconfigurationRepository.download(""));
		assertEquals(String.format("Error downloading data from administrator portal. Status code: %s.", statusCodeNotFound),
				ioException.getMessage());
	}
}

