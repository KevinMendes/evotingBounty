/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

class KeystoreProperties {

	private static final String DEFAULT_FILENAME_PREFIX = "";
	private static final String DEFAULT_PRIVATE_KEYSTORE_TYPE = "PKCS12";
	private static final String DEFAULT_PRIVATE_KEYSTORE_FILENAME_SUFFIX = ".p12";
	private static final String DEFAULT_SIGNING_PRIVATE_KEY_ALIAS_SUFFIX = "-signing";
	private static final String DEFAULT_ENCRYPTION_PRIVATE_KEY_ALIAS_SUFFIX = "-encryption";
	private static final String DEFAULT_ROOT_CERTIFICATE_FILENAME = "rootCA.pem";
	private static final String DEFAULT_SIGNING_CERTIFICATE_FILENAME_SUFFIX = "_signing.pem";
	private static final String DEFAULT_ENCRYPTION_CERTIFICATE_FILENAME_SUFFIX = "_encryption.pem";
	private static final boolean DEFAULT_IS_EXTERNAL_PASSWORDS_FILE = false;
	private static final boolean DEFAULT_IS_ONE_TIME_PASSWORDS_FILE = false;

	private final String filenamePrefix;
	private final String privateKeystoreType;
	private final String privateKeystoreFilenameSuffix;
	private final String signingPrivateKeyAliasSuffix;
	private final String encryptionPrivateKeyAliasSuffix;
	private final String rootCertificateFilename;
	private final String signingCertificateFilenameSuffix;
	private final String encryptionCertificateFilenameSuffix;
	private final boolean isExternalPasswordsFile;
	private final boolean isOneTimePasswordsFile;

	private final Map<NodeIdentifier, NodeProperties> nodePropertiesMap = new EnumMap<>(NodeIdentifier.class);

	private KeystoreProperties(final Properties properties) {
		String filenamePrefix = properties.getProperty("filename.prefix");

		String privateKeystoreType = properties.getProperty("private-keystore.type");
		String privateKeystoreFilenameSuffix = properties.getProperty("private-keystore.filename.suffix");

		String signingPrivateKeyAliasSuffix = properties.getProperty("private-key.alias.suffix.signing");
		String encryptionPrivateKeyAliasSuffix = properties.getProperty("private-key.alias.suffix.encryption");

		String rootCertificateFilename = properties.getProperty("certificate.filename.full.root");
		String signingCertificateFilenameSuffix = properties.getProperty("certificate.filename.suffix.signing");
		String encryptionCertificateFilenameSuffix = properties.getProperty("certificate.filename.suffix.encryption");

		String isExternalPasswordsFile = properties.getProperty("passwords.externalfile");
		String isOneTimePasswordsFile = properties.getProperty("passwords.onetimefile");

		filenamePrefix = filenamePrefix == null ? "" : filenamePrefix.trim();
		privateKeystoreType = privateKeystoreType == null ? "" : privateKeystoreType.trim();
		privateKeystoreFilenameSuffix = privateKeystoreFilenameSuffix == null ? "" : privateKeystoreFilenameSuffix.trim();
		signingPrivateKeyAliasSuffix = signingPrivateKeyAliasSuffix == null ? "" : signingPrivateKeyAliasSuffix.trim();
		encryptionPrivateKeyAliasSuffix = encryptionPrivateKeyAliasSuffix == null ? "" : encryptionPrivateKeyAliasSuffix.trim();
		rootCertificateFilename = rootCertificateFilename == null ? "" : rootCertificateFilename.trim();
		signingCertificateFilenameSuffix = signingCertificateFilenameSuffix == null ? "" : signingCertificateFilenameSuffix.trim();
		encryptionCertificateFilenameSuffix = encryptionCertificateFilenameSuffix == null ? "" : encryptionCertificateFilenameSuffix.trim();
		isExternalPasswordsFile = isExternalPasswordsFile == null ? "" : isExternalPasswordsFile.trim();
		isOneTimePasswordsFile = isOneTimePasswordsFile == null ? "" : isOneTimePasswordsFile.trim();

		this.filenamePrefix = filenamePrefix.isEmpty() ? DEFAULT_FILENAME_PREFIX : filenamePrefix;
		this.privateKeystoreType = privateKeystoreType.isEmpty() ? DEFAULT_PRIVATE_KEYSTORE_TYPE : privateKeystoreType;
		this.privateKeystoreFilenameSuffix = privateKeystoreFilenameSuffix.isEmpty() ?
				DEFAULT_PRIVATE_KEYSTORE_FILENAME_SUFFIX :
				privateKeystoreFilenameSuffix;
		this.signingPrivateKeyAliasSuffix = signingPrivateKeyAliasSuffix.isEmpty() ?
				DEFAULT_SIGNING_PRIVATE_KEY_ALIAS_SUFFIX :
				signingPrivateKeyAliasSuffix;
		this.encryptionPrivateKeyAliasSuffix = encryptionPrivateKeyAliasSuffix.isEmpty() ?
				DEFAULT_ENCRYPTION_PRIVATE_KEY_ALIAS_SUFFIX :
				encryptionPrivateKeyAliasSuffix;
		this.rootCertificateFilename = rootCertificateFilename.isEmpty() ? DEFAULT_ROOT_CERTIFICATE_FILENAME : rootCertificateFilename;
		this.signingCertificateFilenameSuffix = signingCertificateFilenameSuffix.isEmpty() ?
				DEFAULT_SIGNING_CERTIFICATE_FILENAME_SUFFIX :
				signingCertificateFilenameSuffix;
		this.encryptionCertificateFilenameSuffix = encryptionCertificateFilenameSuffix.isEmpty() ?
				DEFAULT_ENCRYPTION_CERTIFICATE_FILENAME_SUFFIX :
				encryptionCertificateFilenameSuffix;
		this.isExternalPasswordsFile = isExternalPasswordsFile.isEmpty() ?
				DEFAULT_IS_EXTERNAL_PASSWORDS_FILE :
				"y".equalsIgnoreCase(isExternalPasswordsFile);
		this.isOneTimePasswordsFile = isOneTimePasswordsFile.isEmpty() ?
				DEFAULT_IS_ONE_TIME_PASSWORDS_FILE :
				"y".equalsIgnoreCase(isOneTimePasswordsFile);

		for (final NodeIdentifier nodeIdentifier : NodeIdentifier.values()) {
			final String nodeShortName = nodeIdentifier.getShortName();
			final String nodeAlias = nodeIdentifier.getAlias();

			String nodeFilenamePrefix = properties.getProperty("filename.prefix." + nodeAlias);
			String privateKeyAliasPrefix = properties.getProperty("private-key.alias.prefix." + nodeAlias);

			nodeFilenamePrefix = nodeFilenamePrefix == null ? "" : nodeFilenamePrefix.trim();
			privateKeyAliasPrefix = privateKeyAliasPrefix == null ? "" : privateKeyAliasPrefix.trim();

			nodeFilenamePrefix = nodeFilenamePrefix.isEmpty() ? nodeShortName : nodeFilenamePrefix;
			privateKeyAliasPrefix = privateKeyAliasPrefix.isEmpty() ? nodeAlias : privateKeyAliasPrefix;

			this.nodePropertiesMap.put(nodeIdentifier, new NodeProperties(nodeIdentifier, nodeFilenamePrefix, privateKeyAliasPrefix));
		}
	}

	/**
	 * Factory
	 *
	 * @param properties Properties to read from
	 * @return An instance
	 */
	static KeystoreProperties read(final Properties properties) {
		return new KeystoreProperties(properties);
	}

	String getFilenamePrefix() {
		return filenamePrefix;
	}

	String getPrivateKeystoreType() {
		return privateKeystoreType;
	}

	String getPrivateKeystoreFilenameSuffix() {
		return privateKeystoreFilenameSuffix;
	}

	String getSigningPrivateKeyAliasSuffix() {
		return signingPrivateKeyAliasSuffix;
	}

	String getEncryptionPrivateKeyAliasSuffix() {
		return encryptionPrivateKeyAliasSuffix;
	}

	String getRootCertificateFilename() {
		return rootCertificateFilename;
	}

	String getSigningCertificateFilenameSuffix() {
		return signingCertificateFilenameSuffix;
	}

	String getEncryptionCertificateFilenameSuffix() {
		return encryptionCertificateFilenameSuffix;
	}

	boolean isExternalPasswordsFile() {
		return isExternalPasswordsFile;
	}

	boolean isOneTimePasswordsFile() {
		return isOneTimePasswordsFile;
	}

	NodeProperties getNodeProperties(final NodeIdentifier nodeIdentifier) {
		return this.nodePropertiesMap.get(nodeIdentifier);
	}

	@Override
	public String toString() {
		return "KeystoreProperties{" + "filenamePrefix='" + filenamePrefix + '\'' + ", privateKeystoreType='" + privateKeystoreType + '\''
				+ ", privateKeystoreFilenameSuffix='" + privateKeystoreFilenameSuffix + '\'' + ", signingPrivateKeyAliasSuffix='"
				+ signingPrivateKeyAliasSuffix + '\'' + ", encryptionPrivateKeyAliasSuffix='" + encryptionPrivateKeyAliasSuffix + '\''
				+ ", rootCertificateFilename='" + rootCertificateFilename + '\'' + ", signingCertificateFilenameSuffix='"
				+ signingCertificateFilenameSuffix + '\'' + ", encryptionCertificateFilenameSuffix='" + encryptionCertificateFilenameSuffix + '\''
				+ ", isExternalPasswordsFile=" + isExternalPasswordsFile + ", isOneTimePasswordsFile=" + isOneTimePasswordsFile
				+ ", nodePropertiesMap=" + nodePropertiesMap + '}';
	}

	class NodeProperties {

		private final NodeIdentifier nodeIdentifier;

		private final String filenamePrefix;
		private final String privateKeyAliasPrefix;

		private String privateKeystoreFilename;
		private String signingPrivateKeyAlias;
		private String encryptionPrivateKeyAlias;
		private String signingCertificateFilename;
		private String encryptionCertificateFilename;

		private NodeProperties(final NodeIdentifier nodeIdentifier, final String filenamePrefix, final String privateKeyAliasPrefix) {
			this.nodeIdentifier = nodeIdentifier;
			this.filenamePrefix = filenamePrefix;
			this.privateKeyAliasPrefix = privateKeyAliasPrefix;
		}

		String getPrivateKeystoreType() {
			return KeystoreProperties.this.getPrivateKeystoreType();
		}

		String getPrivateKeystoreFilename() {
			if (this.privateKeystoreFilename == null) {
				this.privateKeystoreFilename = KeystoreProperties.this.getFilenamePrefix() + this.filenamePrefix + KeystoreProperties.this
						.getPrivateKeystoreFilenameSuffix();
			}
			return this.privateKeystoreFilename;
		}

		String getSigningPrivateKeyAlias() {
			if (this.signingPrivateKeyAlias == null) {
				this.signingPrivateKeyAlias = this.privateKeyAliasPrefix + KeystoreProperties.this.getSigningPrivateKeyAliasSuffix();
			}
			return this.signingPrivateKeyAlias;
		}

		String getEncryptionPrivateKeyAlias() {
			if (this.encryptionPrivateKeyAlias == null) {
				this.encryptionPrivateKeyAlias = this.privateKeyAliasPrefix + KeystoreProperties.this.getEncryptionPrivateKeyAliasSuffix();
			}
			return this.encryptionPrivateKeyAlias;
		}

		String getRootCertificateFilename() {
			return KeystoreProperties.this.getRootCertificateFilename();
		}

		String getSigningCertificateFilename() {
			if (this.signingCertificateFilename == null) {
				this.signingCertificateFilename = KeystoreProperties.this.getFilenamePrefix() + this.filenamePrefix + KeystoreProperties.this
						.getSigningCertificateFilenameSuffix();
			}
			return this.signingCertificateFilename;
		}

		String getEncryptionCertificateFilename() {
			if (this.encryptionCertificateFilename == null) {
				this.encryptionCertificateFilename = KeystoreProperties.this.getFilenamePrefix() + this.filenamePrefix + KeystoreProperties.this
						.getEncryptionCertificateFilenameSuffix();
			}
			return this.encryptionCertificateFilename;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			NodeProperties that = (NodeProperties) o;
			return nodeIdentifier == that.nodeIdentifier;
		}

		@Override
		public int hashCode() {
			return nodeIdentifier.hashCode();
		}

		@Override
		public String toString() {
			return "NodeProperties{" + "nodeIdentifier=" + nodeIdentifier.getName() + ", privateKeystoreFilename='" + this
					.getPrivateKeystoreFilename() + '\'' + ", signingPrivateKeyAlias='" + this.getSigningPrivateKeyAlias() + '\''
					+ ", encryptionPrivateKeyAlias='" + this.getEncryptionPrivateKeyAlias() + '\'' + ", rootCertificateFilename='" + this
					.getRootCertificateFilename() + '\'' + ", signingCertificateFilename='" + this.getSigningCertificateFilename() + '\''
					+ ", encryptionCertificateFilename='" + this.getEncryptionCertificateFilename() + '\'' + '}';
		}
	}

}
