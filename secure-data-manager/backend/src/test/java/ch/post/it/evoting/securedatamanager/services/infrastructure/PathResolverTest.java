/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.securedatamanager.commons.Constants;

class PathResolverTest {

	private static final String electionEventId = "f28493b098604663b6a6969f53f51b56";
	private static final String ballotId = "f0642fdfe4864f4985ac07c057da54b7";
	private static final String ballotBoxId = "672b1b49ed0341a3baa953d611b90b74";
	private final PathResolver pathResolver = new PathResolver("unused");

	@Test
	void resolveBallotBoxPath() {
		final Path actualPath = pathResolver.resolveBallotBoxPath(electionEventId, ballotId, ballotBoxId);

		assertNotNull(actualPath);

		final Path endsWithPath = Paths.get(Constants.CONFIG_FILES_BASE_DIR, electionEventId, Constants.CONFIG_DIR_NAME_ONLINE,
				Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION, Constants.CONFIG_DIR_NAME_BALLOTS, ballotId, Constants.CONFIG_DIR_NAME_BALLOTBOXES,
				ballotBoxId);

		assertTrue(actualPath.endsWith(endsWithPath));
	}

}
