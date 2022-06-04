/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.commons;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * {@link PathResolver} implementation that prepends a suffix to the provided path
 */
@Primary
@Component
public class PrefixPathResolver implements PathResolver {

	private static final Path ROOT = Paths.get(File.separator);

	private final Path prefixPath;

	private final String prefixCanonicalPathString;

	/**
	 * @param prefixPathString The prefix string should be absolute, if it doesn't start with '/', a '/' will be added
	 */
	public PrefixPathResolver(
			@Value("${user.home}")
			final String prefixPathString) {
		super();
		final Path path = Paths.get(prefixPathString.trim());
		if (path.isAbsolute() || path.startsWith(File.separator)) {
			prefixPath = path;
		} else {
			prefixPath = ROOT.resolve(path);
		}

		prefixCanonicalPathString = getCanonicalPathString(prefixPath);
	}

	@Override
	public Path resolve(final String... pathStrings) {
		final Path firstPath = Paths.get(pathStrings[0].trim());

		Path resolvedPath;
		if (firstPath.isAbsolute() || firstPath.startsWith(File.separator)) {
			resolvedPath = firstPath;
		} else {
			resolvedPath = Paths.get(prefixPath.toString(), firstPath.toString());
		}
		for (int i = 1; i < pathStrings.length; i++) {
			resolvedPath = Paths.get(resolvedPath.toString(), pathStrings[i].trim());
		}

		validate(resolvedPath);

		return resolvedPath;
	}

	private String getCanonicalPathString(final Path path) {
		try {
			return path.toFile().getCanonicalPath();
		} catch (final IOException e) {
			throw new PrefixPathResolverException("Could not retrieve canonical form of path " + path, e);
		}
	}

	private void validate(final Path path) {
		final String canonicalPathString = getCanonicalPathString(path);

		if (!canonicalPathString.startsWith(prefixCanonicalPathString)) {
			throw new PrefixPathResolverException(
					"The path " + canonicalPathString + " is not inside the expected parent path " + prefixCanonicalPathString);
		}
	}
}
