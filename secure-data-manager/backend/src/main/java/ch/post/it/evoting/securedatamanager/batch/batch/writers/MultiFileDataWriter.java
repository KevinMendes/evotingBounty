/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.writers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.core.io.FileSystemResource;

import ch.post.it.evoting.securedatamanager.batch.batch.exceptions.GenerateVerificationCardCodesException;
import ch.post.it.evoting.securedatamanager.commons.Constants;

public class MultiFileDataWriter<T> extends FlatFileItemWriter<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MultiFileDataWriter.class);

	private final Path basePath;
	private final int maxNumCredentialsPerFile;
	private int numLinesRead;
	private int fileNumber;

	protected MultiFileDataWriter(final Path basePath, final int maxNumCredentialsPerFile) {
		this.basePath = basePath;
		this.maxNumCredentialsPerFile = maxNumCredentialsPerFile;
		validateInput();
		initialize();
	}

	private void changeResourceIfNeeded() {
		numLinesRead++;
		if (numLinesRead > maxNumCredentialsPerFile) {
			numLinesRead = 1;
			fileNumber++;
			close();
			setResource(getNextResource());
			open(new ExecutionContext());
		}
	}

	private void validateInput() {
		if (maxNumCredentialsPerFile < 1) {
			throw new IllegalArgumentException(
					"Expected maximum number of credentials per file to be a positive integer; Found " + maxNumCredentialsPerFile
							+ "; Check Spring configuration properties.");
		}
	}

	private FileSystemResource getNextResource() {
		final String basePathStr = basePath.toString();
		final String basePathPrefixStr = FilenameUtils.removeExtension(basePathStr);

		final String path = String.format("%s.%s%s", basePathPrefixStr, fileNumber, Constants.CSV);
		return new FileSystemResource(path);
	}

	private void deletePreExistingOutputFiles() {
		final Path baseParentPath = basePath.getParent();

		final File baseParentDirectory = new File(baseParentPath.toString());
		if (!baseParentDirectory.exists()) {
			return;
		}

		try (final Stream<Path> pathStream = Files.walk(baseParentPath)) {
			pathStream.map(Path::toFile)
					.filter(file -> (file.getName().startsWith(Constants.CONFIG_FILE_NAME_CREDENTIAL_DATA) && file.getName().endsWith(Constants.CSV)))
					.forEach(File::delete);
		} catch (final IOException e) {
			final String errorMsg = "Error - could not delete all pre-existing credential data files. " + e.getMessage();
			LOGGER.error(errorMsg);
			throw new GenerateVerificationCardCodesException(errorMsg);
		}
	}

	private void initialize() {
		numLinesRead = 0;
		fileNumber = 0;

		setLineAggregator(lineAggregator());
		setTransactional(false);
		setResource(getNextResource());

		// These lines are necessary for the Spring FlatFileItemWriter base
		// class to work properly after resetting the resource (for the case of
		// writing the credential data to multiple files).
		deletePreExistingOutputFiles();
		setAppendAllowed(true);
		setShouldDeleteIfExists(false);
	}

	protected String getLine(final T item) {
		return item.toString();
	}

	private LineAggregator<T> lineAggregator() {
		return item -> {
			changeResourceIfNeeded();
			return getLine(item);
		};
	}
}
