/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.securedatamanager.config.shares.token;

import static com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LibraryLoaderTest {

	private final String[][] PLATFORMS = { { "windows", "aetpkss1.dll" }, { "linux", "libaetpkss.so" }, { "mac", "libaetpkss.dylib" } };
	private final int INDEX_OS = 0;
	private final int INDEX_FILE = 1;

	@TempDir
	Path tempDir;

	@Test
	void testExisting() throws Exception {
		restoreSystemProperties(() -> assertDoesNotThrow(() -> {
			for (final String[] platform : PLATFORMS) {
				final String filename = platform[INDEX_FILE];
				Files.createFile(tempDir.resolve(filename));

				setSystemProperties(tempDir.toString(), platform[INDEX_OS]);

				final File library = assertDoesNotThrow(LibraryLoader::loadPkcs11Library);

				assertEquals(filename, library.getName());
			}
		}));
	}

	@Test
	void testNotExisting() throws Exception {
		restoreSystemProperties(() -> {
			for (final String[] platform : PLATFORMS) {

				setSystemProperties(tempDir.toString(), platform[INDEX_OS]);

				final FileNotFoundException exception = assertThrows(FileNotFoundException.class, LibraryLoader::loadPkcs11Library);

				assertEquals(String.format("Unable to find the Safesign native library (%s) in the java.library.path. Is Safesign really installed?",
						platform[INDEX_FILE]), exception.getMessage());
			}
		});
	}

	private void setSystemProperties(final String javaLibraryPath, final String osName) {
		System.setProperty("java.library.path", javaLibraryPath);
		System.setProperty("os.name", osName);
	}
}
