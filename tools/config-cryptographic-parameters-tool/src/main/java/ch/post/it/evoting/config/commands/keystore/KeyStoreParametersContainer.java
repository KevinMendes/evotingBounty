/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.keystore;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.file.Path;
import java.util.Date;
import java.util.Locale;

import ch.post.it.evoting.cryptoprimitives.signing.AuthorityInformation;

/**
 * Encapsulates the data.
 */
public final class KeyStoreParametersContainer {

	private final String alias;
	private final Path outputPath;
	private final Date validFrom;
	private final Date validUntil;
	private final AuthorityInformation authorityInformation;
	private final int passwordLength;

	private KeyStoreParametersContainer(final String alias, final Path outputPath, final Date validFrom, final Date validUntil,
			final AuthorityInformation authorityInformation, final int passwordLength) {
		checkNotNull(alias);
		checkNotNull(outputPath);
		checkNotNull(validFrom);
		checkNotNull(validUntil);
		checkNotNull(authorityInformation);

		checkArgument(!alias.isEmpty(), "alias must not be empty.");
		checkArgument(validFrom.before(validUntil), "validFrom must be set before validuntil.");
		checkArgument(passwordLength > 0, "passwordLength must be greater than 0.");

		this.alias = alias.toLowerCase(Locale.ROOT);
		this.outputPath = outputPath;
		this.validFrom = validFrom;
		this.validUntil = validUntil;
		this.authorityInformation = authorityInformation;
		this.passwordLength = passwordLength;
	}

	public String getAlias() {
		return alias;
	}

	public Path getOutputPath() {
		return outputPath;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public Date getValidUntil() {
		return validUntil;
	}

	public AuthorityInformation getAuthorityInformation() {
		return authorityInformation;
	}

	public int getPasswordLength() {
		return passwordLength;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String alias;
		private Path outputPath;
		private Date validFrom;
		private Date validUntil;
		private AuthorityInformation authorityInformation;
		private int passwordLength;

		public Builder setAlias(String alias) {
			this.alias = alias;
			return this;
		}

		public Builder setOutputPath(Path outputPath) {
			this.outputPath = outputPath;
			return this;
		}

		public Builder setValidFrom(Date validFrom) {
			this.validFrom = validFrom;
			return this;
		}

		public Builder setValidUntil(Date validUntil) {
			this.validUntil = validUntil;
			return this;
		}

		public Builder setAuthorityInformation(AuthorityInformation authorityInformation) {
			this.authorityInformation = authorityInformation;
			return this;
		}

		public Builder setPasswordLength(int passwordLength) {
			this.passwordLength = passwordLength;
			return this;
		}

		public KeyStoreParametersContainer build() {
			return new KeyStoreParametersContainer(alias, outputPath, validFrom, validUntil, authorityInformation, passwordLength);
		}
	}
}
