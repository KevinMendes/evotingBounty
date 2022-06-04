/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const {HashService} = require("crypto-primitives-ts/lib/cjs/hashing/hash_service");
const {stringToInteger} = require("crypto-primitives-ts/lib/cjs/conversions");
const {checkNotNull, checkArgument} = require("crypto-primitives-ts/lib/cjs/validation/preconditions");

module.exports = (function () {
	'use strict';

	/**
	 * Implements the CreateConfirmMessage algorithm described in the cryptographic protocol.
	 * Generates a confirmation key.
	 *
	 * @param {string} ballotCastingKey, BCK_id the ballot casting key
	 * @param {ZqElement} verificationCardSecretKey, k_id the verification card secret key
	 * @param {GqGroup} group, the encryption parameters context
	 * @returns {GqElement}, CK_id the confirmation key
	 */
	const createConfirmMessage = function (
		ballotCastingKey,
		verificationCardSecretKey,
		group
	) {
		const BCK_id = checkNotNull(ballotCastingKey);
		const k_id = checkNotNull(verificationCardSecretKey);
		checkNotNull(group);

		checkArgument(BCK_id.length === 9, "The ballot casting key length must be 9");
		checkArgument(BCK_id.match("^[0-9]+$") != null, "The ballot casting key must be a numeric value");
		checkArgument(BCK_id !== "000000000", "The ballot casting key must contain one non-zero element");

		const hashService = new HashService();
		const hBCK_id = hashService.hashAndSquare(stringToInteger(BCK_id), group);

		// CK_id
		return hBCK_id.exponentiate(k_id);
	};

	return createConfirmMessage;
})();
