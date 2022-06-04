/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.securedatamanager.services.domain.model.EntityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;

/**
 * JUnit for the class {@link ConfigurationEntityStatusService}.
 */
@ExtendWith(MockitoExtension.class)
class ConfigurationEntityStatusServiceTest {

	private final String newStatus = "";
	private final String id = "";
	@InjectMocks
	private final ConfigurationEntityStatusService configurationEntityStatusService = new ConfigurationEntityStatusService();
	private String updateResult = JsonConstants.EMPTY_OBJECT;
	@Mock
	private EntityRepository baseRepository;

	@Test
	void updateEmptyObjectReturned() {
		when(baseRepository.update(anyString())).thenReturn(updateResult);

		assertEquals(JsonConstants.EMPTY_OBJECT, configurationEntityStatusService.update(newStatus, id, baseRepository));
	}

	@Test
	void update() {
		updateResult = JsonConstants.RESULT_EMPTY;
		when(baseRepository.update(anyString())).thenReturn(updateResult);

		assertEquals(JsonConstants.RESULT_EMPTY, configurationEntityStatusService.update(newStatus, id, baseRepository));
	}
}
