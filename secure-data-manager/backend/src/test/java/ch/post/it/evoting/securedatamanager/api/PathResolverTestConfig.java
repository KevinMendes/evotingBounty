/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.post.it.evoting.securedatamanager.commons.PathResolver;

/**
 * Declare a path resolver bean for tests.
 */
@Configuration
public class PathResolverTestConfig {

	@Bean
	PathResolver pathResolver() throws IOException {
		final Path basePath = Files.createTempDirectory("mdv-test");

		// Trivial implementation of PathResolver with an ad-hoc temporary path.
		return new PathResolver() {
			@Override
			public Path resolve(final String... strings) {
				final String firstString = strings[0];
				String[] otherStrings = null;
				final int newLength = strings.length - 1;
				if (newLength > 0) {
					otherStrings = new String[newLength];
					System.arraycopy(strings, 1, otherStrings, 0, newLength);
				}

				final Path pathToResolve = otherStrings == null ? Paths.get(firstString) : Paths.get(firstString, otherStrings);

				return basePath.resolve(pathToResolve);
			}

			/**
			 * Delete the temporary folder when the bean is destroyed.
			 *
			 * @throws IOException
			 */
			public void shutdown() throws IOException {
				Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}
				});
			}
		};
	}
}
