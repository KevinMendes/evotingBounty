/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.security.OUser;

/**
 * Factory of {@link DatabaseManager} for a single local database.
 *
 * <p>
 * It is the caller's responsibility to ensure that there is only one factory per database url.
 * </p>
 *
 * <p>
 * This implementation is thread safe.
 * </p>
 */
@Component
public final class DatabaseManagerFactory implements AutoCloseable {

	private static final int MIN_PASSWORD_LENGTH = 12;
	private static final String DEFAULT_USERNAME = OUser.ADMIN;

	private final OrientDB orientDB;
	private final ODatabaseType databaseType;
	private final String passwordFile;

	/**
	 * Create a DatabaseManagerFactory.
	 *
	 * @param databaseType a non-null string value representing the database type. It can take values from {@link ODatabaseType}.
	 * @param databasePath the non-null path to the directory where database instances are stored. Can be empty.
	 * @param passwordFile the non-null location of the file containing the new password for the admin user.
	 */
	public DatabaseManagerFactory(
			@Value("${database.type}")
			final String databaseType,
			@Value("${database.path}")
			final String databasePath,
			@Value("${database.password.location}")
			final String passwordFile) {
		checkNotNull(databaseType);
		checkNotNull(databasePath);
		checkNotNull(passwordFile);

		this.databaseType = ODatabaseType.valueOf(databaseType.toUpperCase());
		this.orientDB = new OrientDB(databaseType + ":" + databasePath, OrientDBConfig.defaultConfig());
		this.passwordFile = passwordFile;
	}

	@VisibleForTesting
	protected static boolean isPasswordValid(final String password) {
		final Predicate<String> isNotNull = s -> !Objects.isNull(s);
		final Predicate<String> isLongEnough = s -> s.length() >= MIN_PASSWORD_LENGTH;
		final Predicate<String> containsDigit = s -> s.chars().anyMatch(Character::isDigit);
		final Predicate<String> containsLowerCase = s -> s.chars().anyMatch(c -> Character.isAlphabetic(c) && Character.isLowerCase(c));
		final Predicate<String> containsUpperCase = s -> s.chars().anyMatch(c -> Character.isAlphabetic(c) && Character.isUpperCase(c));

		return isNotNull.and(isLongEnough).and(containsDigit).and(containsLowerCase).and(containsUpperCase).test(password);
	}

	/**
	 * Create a DatabaseManager for the given database.
	 *
	 * @param databaseName The name of the database to be managed.
	 * @return a DatabaseManager for the given database.
	 */
	public DatabaseManager newDatabaseManager(final String databaseName) {
		checkNotNull(databaseName);

		final Path path = Paths.get(passwordFile);
		final Properties properties = new Properties();
		try (final InputStream inputStream = Files.newInputStream(path)) {
			properties.load(inputStream);
		} catch (final IOException e) {
			throw new UncheckedIOException(String.format("Failed to read password file at location %s.", path), e);
		}

		final String password = properties.getProperty("password");
		if (!isPasswordValid(password)) {
			throw new IllegalArgumentException(String.format(
					"The password must be non-null, have at least one upper case letter, at least one lower case letter, one digit and be at least of size %d.",
					MIN_PASSWORD_LENGTH));
		}

		return new DatabaseManager(orientDB, databaseType, databaseName, DEFAULT_USERNAME, password);
	}

	/**
	 * Close the database connection. This will close all open databases.
	 */
	@Override
	public void close() {
		orientDB.close();
	}

}
