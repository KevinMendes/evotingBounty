/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

import org.apache.commons.cli.Option;

/**
 * Represents a parameter of a command. A CommandParameter contains the following parts:
 * <ul>
 * <li>A parameter name.</li>
 * <li>A required indicator.</li>
 * <li>A number of values.</li>
 * </ul>
 * Each CommandParameter is capable to generate a {@link Option} to be used by the command parser.
 */
public enum CommandParameter {
	P12_PATH("p12_path", true, 1) {
		@Override
		public Option generateOption() {
			return Option.builder(getParameterName()).longOpt("p12_path").desc("P12 path. Can be absolute or relative.")
					.numberOfArgs(getNumberOfArgs()).build();
		}
	},
	ENCRYPTION_PARAMS("params_path", true, 1) {
		@Override
		public Option generateOption() {
			return Option.builder(getParameterName()).longOpt("params_path").desc("Encryption Parameters signed JSON. Can be absolute or relative.")
					.numberOfArgs(getNumberOfArgs()).build();
		}
	},
	SEED_PATH("seed_path", true, 1) {
		@Override
		public Option generateOption() {
			return Option.builder(getParameterName()).longOpt("seed_path").desc("Path for file with the seed. Can be absolute or relative.")
					.numberOfArgs(getNumberOfArgs()).build();
		}
	},
	SEED_SIG_PATH("seed_sig_path", true, 1) {
		@Override
		public Option generateOption() {
			return Option.builder(getParameterName()).longOpt("seed_sig_path")
					.desc("Path for file with the seed signature. Can be absolute or relative.").numberOfArgs(getNumberOfArgs()).build();
		}
	},
	OUT("out", false, 1, "./output/") {
		@Override
		public Option generateOption() {
			return Option.builder(getParameterName()).longOpt("outPath")
					.desc(format("Output path. Can be absolute or relative. Default value: '%s'.", getDefaultValue())).numberOfArgs(getNumberOfArgs())
					.build();
		}
	},
	TRUSTED_CA_PATH("trusted_ca_path", false, 1) {
		@Override
		public Option generateOption() {
			return Option.builder(getParameterName()).desc("Path for the file where the trusted CA is.").numberOfArgs(getNumberOfArgs()).build();
		}
	},
	ALIAS("alias", false, 1) {
		@Override
		public Option generateOption() {
			return Option.builder(getParameterName()).desc("Name to use to create the keystore").numberOfArgs(getNumberOfArgs()).build();
		}
	},
	VALID_FROM("valid_from", true, 1) {
		@Override
		public Option generateOption() {
			return Option.builder(getParameterName()).desc("Start of certificate validity. Format: 'dd/MM/yyyy'.").numberOfArgs(getNumberOfArgs())
					.build();
		}
	},
	VALID_UNTIL("valid_until", true, 1) {
		@Override
		public Option generateOption() {
			return Option.builder(getParameterName()).desc("End of certificate validity. Format: 'dd/MM/yyyy'.").numberOfArgs(getNumberOfArgs())
					.build();
		}
	},
	CERTIFICATE_COMMON_NAME("certificate_common_name", true, 1) {
		@Override
		public Option generateOption() {
			return Option.builder(getParameterName()).desc("Information of the certificate: Common name.").numberOfArgs(getNumberOfArgs()).build();
		}
	},
	CERTIFICATE_COUNTRY("certificate_country", true, 1) {
		@Override
		public Option generateOption() {
			return Option.builder(getParameterName()).desc("Information of the certificate: Country.").numberOfArgs(getNumberOfArgs()).build();
		}
	},
	CERTIFICATE_STATE("certificate_state", true, 1) {
		@Override
		public Option generateOption() {
			return Option.builder(getParameterName()).desc("Information of the certificate: State.").numberOfArgs(getNumberOfArgs()).build();
		}
	},
	CERTIFICATE_LOCALITY("certificate_locality", true, 1) {
		@Override
		public Option generateOption() {
			return Option.builder(getParameterName()).desc("Information of the certificate: Locality.").numberOfArgs(getNumberOfArgs()).build();
		}
	},
	CERTIFICATE_ORGANISATION("certificate_organisation", true, 1) {
		@Override
		public Option generateOption() {
			return Option.builder(getParameterName()).desc("Information of the certificate: Organisation.").numberOfArgs(getNumberOfArgs()).build();
		}
	},
	PASSWORD_LENGTH("password_length", false, 1, "20") {
		@Override
		public Option generateOption() {
			return Option.builder(getParameterName()).desc(format("Length of the password. Default value: '%s'", getDefaultValue()))
					.numberOfArgs(getNumberOfArgs()).build();
		}
	};

	private final String parameterName;

	private final boolean required;

	private final int numberOfArgs;

	private final String defaultValue;

	CommandParameter(final String parameterName, final boolean required, final int numberOfArgs) {
		this(parameterName, required, numberOfArgs, null);
	}

	CommandParameter(final String parameterName, final boolean required, final int numberOfArgs, final String defaultValue) {
		this.parameterName = parameterName;
		this.required = required;
		this.numberOfArgs = numberOfArgs;
		this.defaultValue = defaultValue;

		checkArgument(defaultValue == null || !required,
				format("Required parameter %s cannot have a default value. %s", parameterName, defaultValue));
	}

	/**
	 * Gets parameter name.
	 *
	 * @return the parameter name.
	 */
	public String getParameterName() {
		return parameterName;
	}

	/**
	 * Is required.
	 *
	 * @return true if the parameter is required, false otherwise.
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Gets number of args.
	 *
	 * @return the number of args.
	 */
	public int getNumberOfArgs() {
		return numberOfArgs;
	}

	/**
	 * Generate {@link Option} to be used by the command parser.
	 *
	 * @return the option to be used by the command parser.
	 */
	public abstract Option generateOption();

	/**
	 * Gets the default value.
	 *
	 * @return the default value.
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
}
