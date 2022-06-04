/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import static java.nio.file.Files.createTempFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class NodeKeyTestService {

	public static final String NODE_ALIAS = "ccncakey";

	public static IntermediateCAKeystore getOneIntermediateCAKeystore() throws IOException {
		return copyIntermediateCAKeystore(1).get(0);

	}

	public static List<IntermediateCAKeystore> copyIntermediateCAKeystore(int numberOfCopies)
			throws IOException {

		ResourceLoader resourceLoader = new DefaultResourceLoader();

		File node1CAKeystore = resourceLoader.getResource("classpath:keystore/CCN_C1.p12").getFile();
		File node1CAPassword = resourceLoader.getResource("classpath:keystore/CCN_C1.txt").getFile();

		List<IntermediateCAKeystore> intermediateCAkeystores = new ArrayList<>();
		for (int i = 0; i < numberOfCopies; i++) {
			Path keyStoreFile = createTempFile("intermediateCAkeystore", ".p12");
			Path passwordFile = createTempFile("password", ".txt");
			Files.copy(node1CAPassword.toPath(), passwordFile, StandardCopyOption.REPLACE_EXISTING);
			Files.copy(node1CAKeystore.toPath(), keyStoreFile, StandardCopyOption.REPLACE_EXISTING);
			intermediateCAkeystores.add(new IntermediateCAKeystore(keyStoreFile,passwordFile));
		}
		return intermediateCAkeystores;
	}

	public static class IntermediateCAKeystore {
		private Path keyStoreFile;
		private Path passwordFile;

		public IntermediateCAKeystore(Path keyStoreFile, Path passwordFile) {
			this.keyStoreFile = keyStoreFile;
			this.passwordFile = passwordFile;
		}

		public Path getKeyStoreFile() {
			return keyStoreFile;
		}

		public Path getPasswordFile() {
			return passwordFile;
		}
	}
}
