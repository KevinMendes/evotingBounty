/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication;

import java.util.Optional;

/**
 * Represents an entity with the necessary information to be provided to the extended authentication system
 */
public class ExtendedAuthInformation {

	private final AuthenticationDerivedElement authenticationId;

	private final AuthenticationDerivedElement authenticationPin;

	private final AuthenticationKey authenticationKey;

	private final String encryptedSVK;

	private final Optional<ExtendedAuthChallenge> extendedAuthChallenge;

	private ExtendedAuthInformation(final AuthenticationDerivedElement authenticationId, final AuthenticationDerivedElement authenticationPin,
			final AuthenticationKey authenticationKey, final String encryptedSVK, final Optional<ExtendedAuthChallenge> extendedAuthChallenge) {

		this.authenticationId = authenticationId;
		this.authenticationPin = authenticationPin;
		this.authenticationKey = authenticationKey;
		this.encryptedSVK = encryptedSVK;
		this.extendedAuthChallenge = extendedAuthChallenge;
	}

	public AuthenticationDerivedElement getAuthenticationId() {
		return authenticationId;
	}

	public AuthenticationDerivedElement getAuthenticationPin() {
		return authenticationPin;
	}

	public AuthenticationKey getAuthenticationKey() {
		return authenticationKey;
	}

	public String getEncryptedSVK() {
		return encryptedSVK;
	}

	public Optional<ExtendedAuthChallenge> getExtendedAuthChallenge() {
		return extendedAuthChallenge;
	}

	/**
	 * Inner Builder class, that will allow to have an inmmutable class
	 */
	public static class Builder {

		private AuthenticationDerivedElement authenticationId;

		private AuthenticationDerivedElement authenticationPin;

		private AuthenticationKey authenticationKey;

		private String encryptedSVK;

		private Optional<ExtendedAuthChallenge> extendedAuthChallenge;

		public Builder setAuthenticationId(final AuthenticationDerivedElement authenticationId) {
			this.authenticationId = authenticationId;
			return this;
		}

		public Builder setAuthenticationPin(final AuthenticationDerivedElement authenticationPin) {
			this.authenticationPin = authenticationPin;
			return this;
		}

		public Builder setAuthenticationKey(final AuthenticationKey authenticationKey) {
			this.authenticationKey = authenticationKey;
			return this;
		}

		public Builder setEncryptedSVK(final String encryptedSVK) {
			this.encryptedSVK = encryptedSVK;
			return this;
		}

		public Builder setDerivedChallenges(final Optional<ExtendedAuthChallenge> extendedAuthChallenge) {
			this.extendedAuthChallenge = extendedAuthChallenge;
			return this;
		}

		public ExtendedAuthInformation build() {
			return new ExtendedAuthInformation(authenticationId, authenticationPin, authenticationKey, encryptedSVK, extendedAuthChallenge);
		}

	}

}
