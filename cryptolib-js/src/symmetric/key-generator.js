/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

module.exports = SecretKeyGenerator;

/**
 * @class SecretKeyGenerator
 * @classdesc The secret key generator API. To instantiate this object, use the
 *            method {@link SymmetricCryptographyService.newKeyGenerator}.
 * @hideconstructor
 * @param {Policy}
 *            policy The cryptographic policy to use.
 * @param {SecureRandomService}
 *            secureRandomService The secure random service to use.
 */
function SecretKeyGenerator(policy, secureRandomService) {
	const _randomGenerator = secureRandomService.newRandomGenerator();

	/**
	 * Generates a secret key to use for symmetric encryption.
	 *
	 * @function nextEncryptionKey
	 * @memberof SecretKeyGenerator
	 * @returns {Uint8Array} The encryption secret key.
	 */
	this.nextEncryptionKey = function () {
		return _randomGenerator.nextBytes(
			policy.symmetric.secretKey.encryption.lengthBytes);
	};

	/**
	 * Generates a secret key to use for MAC generation.
	 *
	 * @function nextMacKey
	 * @memberof SecretKeyGenerator
	 * @returns {Uint8Array} The MAC secret key.
	 */
	this.nextMacKey = function () {
		return _randomGenerator.nextBytes(
			policy.symmetric.secretKey.mac.lengthBytes);
	};
}
