/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import java.util.Properties;

import javax.security.auth.Destroyable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractKeystorePasswords implements KeystorePasswords {

	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractKeystorePasswords.class);
	private final KeystorePassword privateKeystorePw;
	private final KeystorePassword signingPrivateKeyPw;
	private final KeystorePassword encryptionPrivateKeyPw;
	private boolean destroyed;

	protected AbstractKeystorePasswords(final Properties properties) {
		String privateKeystorePassword = properties.getProperty("private-keystore.password");
		String signingPrivateKeyPassword = properties.getProperty("private-key.password.signing");
		String encryptionPrivateKeyPassword = properties.getProperty("private-key.password.encryption");

		privateKeystorePassword = privateKeystorePassword == null ? "" : privateKeystorePassword.trim();
		signingPrivateKeyPassword = signingPrivateKeyPassword == null ? privateKeystorePassword : signingPrivateKeyPassword.trim();
		encryptionPrivateKeyPassword = encryptionPrivateKeyPassword == null ? privateKeystorePassword : encryptionPrivateKeyPassword.trim();

		this.privateKeystorePw = this.getKeystorePasswordInstance(privateKeystorePassword);
		this.signingPrivateKeyPw = this.getKeystorePasswordInstance(signingPrivateKeyPassword);
		this.encryptionPrivateKeyPw = this.getKeystorePasswordInstance(encryptionPrivateKeyPassword);
	}

	String getPrivateKeystorePassword() {
		return this.checkDestroyed() ? null : this.privateKeystorePw.getValue();
	}

	String getSigningPrivateKeyPassword() {
		return this.checkDestroyed() ? null : this.signingPrivateKeyPw.getValue();
	}

	String getEncryptionPrivateKeyPassword() {
		return this.checkDestroyed() ? null : this.encryptionPrivateKeyPw.getValue();
	}

	@Override
	public String toString() {
		if (this.destroyed) {
			return "KeystorePasswords{--destroyed--}";
		} else {
			return "KeystorePasswords{--confidential--}";
		}
	}

	String getString() {
		if (this.destroyed) {
			return "KeystorePasswords{--destroyed--}";
		} else {
			return "KeystorePasswords{" + "privateKeystorePw='" + privateKeystorePw + '\'' + ", signingPrivateKeyPw='" + signingPrivateKeyPw + '\''
					+ ", encryptionPrivateKeyPw='" + encryptionPrivateKeyPw + '\'' + '}';
		}
	}

	@Override
	public void destroy() {
		if (this.destroyed) {
			final String errorMsg = "Object already destroyed";
			LOGGER.debug(errorMsg);
		} else {
			this.privateKeystorePw.destroy();
			this.signingPrivateKeyPw.destroy();
			this.encryptionPrivateKeyPw.destroy();
			this.destroyed = true;
		}
	}

	@Override
	public boolean isDestroyed() {
		return this.destroyed;
	}

	private boolean checkDestroyed() {
		if (this.destroyed) {
			final String errorMsg = "Object is destroyed";
			LOGGER.debug(errorMsg);
			return true;
		} else {
			return false;
		}
	}

	protected abstract KeystorePassword getKeystorePasswordInstance(final String value);

	protected interface KeystorePassword extends Destroyable {
		String getValue();

		@Override
		void destroy();
	}

	private abstract class AbstractKeystorePassword implements KeystorePassword {

		protected String value;
		private boolean destroyed;

		protected AbstractKeystorePassword(final String value) {
			this.value = value;
		}

		@Override
		public String getValue() {
			return this.checkDestroyed() ? null : this.value;
		}

		@Override
		public void destroy() {
			if (this.destroyed) {
				final String errorMsg = "Password already destroyed";
				LOGGER.debug(errorMsg);
			} else {
				this.value = null;
				this.destroyed = true;
			}
		}

		@Override
		public boolean isDestroyed() {
			return this.destroyed;
		}

		protected boolean checkDestroyed() {
			if (this.destroyed) {
				final String errorMsg = "Password is destroyed";
				LOGGER.debug(errorMsg);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			if (this.destroyed) {
				return "--destroyed--";
			} else {
				return this.value;
			}
		}
	}

	protected class KeystorePasswordImpl extends AbstractKeystorePassword {
		KeystorePasswordImpl(final String value) {
			super(value);
		}
	}

	protected class OneTimeKeystorePassword extends AbstractKeystorePassword {
		OneTimeKeystorePassword(final String value) {
			super(value);
		}

		/**
		 * Self-destroys after first read
		 */
		@Override
		public String getValue() {
			if (this.checkDestroyed()) {
				return null;
			} else {
				final String value = this.value;
				this.destroy();
				return value;
			}
		}
	}

}
