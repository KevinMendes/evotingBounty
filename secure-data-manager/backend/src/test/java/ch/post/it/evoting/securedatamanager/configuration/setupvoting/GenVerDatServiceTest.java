/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.securedatamanager.VotingCardSetServiceTestBase;

@ExtendWith(MockitoExtension.class)
class GenVerDatServiceTest extends VotingCardSetServiceTestBase {

	@InjectMocks
	private GenVerDatService genVerDatService;

	@Test
	void orderAllowList() {
		final List<String> allowList = asList("fm9i32f", "9sdfjl==", "as2sdf", "77sdfk");

		genVerDatService.order(allowList);

		final List<String> expectedOrder = asList("77sdfk", "9sdfjl==", "as2sdf", "fm9i32f");
		assertEquals(expectedOrder, allowList);
	}

}
