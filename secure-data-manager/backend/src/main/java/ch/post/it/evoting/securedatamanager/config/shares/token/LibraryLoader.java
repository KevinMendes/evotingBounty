/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.securedatamanager.config.shares.token;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class LibraryLoader {

	/**
	 * index constants per OS as used in below arrays
	 */
	private static final int LINUX_INDEX = 0;

	private static final int WIN_INDEX = 1;

	private static final int MACOSX_INDEX = 2;

	/**
	 * file name per OS
	 */
	private static final String[] FILE_NAME = { "libaetpkss.so", "aetpkss1.dll", "libaetpkss.dylib" };

	private LibraryLoader() {
	}

	/**
	 * Tries to load the PKCS#11 safesign native library from the java.library.path
	 *
	 * @return the found file with the appropriate library for the current system.
	 */
	public static File loadPkcs11Library() throws FileNotFoundException {
		final String filename = FILE_NAME[getOS()];
		final Optional<File> location = findAnyMatchingInLibraryPath(filename);

		if (!location.isPresent()) {
			throw new FileNotFoundException(
					String.format("Unable to find the Safesign native library (%s) in the java.library.path. Is Safesign really installed?",
							filename));
		}

		return location.get();
	}

	private static Optional<File> findAnyMatchingInLibraryPath(final String filenameRegex) {
		return Arrays.stream(System.getProperty("java.library.path").split(String.valueOf(File.pathSeparatorChar)))
				.flatMap(d -> findMatchingFiles(d, (dir, name) -> name.matches(filenameRegex))).findAny();
	}

	private static Stream<File> findMatchingFiles(final String dirName, final FilenameFilter filter) {
		final File dir = Paths.get(dirName).toFile();
		if (dir.exists()) {
			final File[] files = dir.listFiles(filter);
			if (files != null) {
				return Stream.of(files);
			}
		}
		return Stream.empty();
	}

	/**
	 * Returns the index in the {@link LibraryLoader#FILE_NAME} array corresponding to the given OS name.
	 *
	 * @return index to be used with {@link LibraryLoader#FILE_NAME}
	 */
	private static int getOS() {
		final String osName = System.getProperty("os.name").toLowerCase();
		if (osName.startsWith("win")) {
			return WIN_INDEX;
		} else if (osName.startsWith("linux")) {
			return LINUX_INDEX;
		} else if (osName.startsWith("mac")) {
			return MACOSX_INDEX;
		} else {
			throw new IllegalStateException("No driver for this OS " + osName);
		}
	}
}
