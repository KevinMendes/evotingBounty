/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global OV */

const jsrsasign = require('jsrsasign');
const codec = require('cryptolib-js/src/codec');
const {byteArrayToInteger} = require("crypto-primitives-ts/lib/cjs/conversions");
const {ElGamalMultiRecipientPublicKey} = require("crypto-primitives-ts/lib/cjs/elgamal/elgamal_multi_recipient_public_key");
const {GqElement} = require("crypto-primitives-ts/lib/cjs/math/gq_element");
const {GqGroup} = require("crypto-primitives-ts/lib/cjs/math/gq_group");
const {ImmutableBigInteger} = require("crypto-primitives-ts/lib/cjs/immutable_big_integer");
const {ImmutableUint8Array} = require("crypto-primitives-ts/lib/cjs/immutable_uint8Array");
const {ZqElement} = require("crypto-primitives-ts/lib/cjs/math/zq_element");
const {ZqGroup} = require("crypto-primitives-ts/lib/cjs/math/zq_group");

module.exports = (function () {
	'use strict';

	/**
	 * Computes the primitives parameters.
	 *
	 * @param {string} tokenResponse, the JSON containing the serialized parameters.
	 * @param {string} keystoreSymmetricEncryptionKey, the password to load the Extended key store.
	 * @returns {PrimitivesParams}, the primitives params object.
	 */
	function parsePrimitivesParams(tokenResponse, keystoreSymmetricEncryptionKey) {
		// Encryption group, we assume the signature has been checked and verified
		const ballotBox = tokenResponse.ballotBox;

		const p = ImmutableBigInteger.fromString(ballotBox.encryptionParameters.p);
		const q = ImmutableBigInteger.fromString(ballotBox.encryptionParameters.q);
		const g = ImmutableBigInteger.fromString(ballotBox.encryptionParameters.g);
		const encryptionParameters = new GqGroup(p, q, g);

		// Election public key
		const identityElementArr = [GqElement.fromValue(ImmutableBigInteger.ONE, encryptionParameters)]
		const electionPublicKey = new ElGamalMultiRecipientPublicKey(identityElementArr);

		// Verification card secret key
		const parsedVerificationCard = OV.parseVerificationCard(
			tokenResponse.verificationCard.verificationCardKeystore,
			keystoreSymmetricEncryptionKey
		);
		const verificationCardSecretKey = ZqElement.create(
			ImmutableBigInteger.fromString(parsedVerificationCard.toString()),
			ZqGroup.sameOrderAs(encryptionParameters)
		);

		/**
		 * @typedef {object} PrimitivesParams
		 * @property {ElGamalMultiRecipientPublicKey} electionPublicKey, the election public key
		 * @property {ZqElement} verificationCardSecretKey, the verification card secret key
		 * @property {GqGroup} encryptionGroup, the encryption group
		 */
		return {
			electionPublicKey: electionPublicKey,
			verificationCardSecretKey: verificationCardSecretKey,
			encryptionGroup: encryptionParameters
		};
	}

	return parsePrimitivesParams;
})();
