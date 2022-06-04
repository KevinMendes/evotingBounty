/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const Policy = require('../cryptopolicy');
const validator = require('../input-validator');
const codec = require('../codec');
const sjcl = require('sjcl');

module.exports = PbkdfDeriver;

/**
 * @class PbkdfDeriver
 * @classdesc The PBKDF deriver API. To instantiate this object, use the method
 *            {@link PbkdfService.newDeriver}.
 * @hideconstructor
 * @param {Policy}
 *            policy The cryptographic policy to use.
 */
function PbkdfDeriver(policy) {
	const _keyLength = policy.primitives.keyDerivation.pbkdf.keyLengthBytes;
	const _minSaltLength = policy.primitives.keyDerivation.pbkdf.minSaltLengthBytes;
	const _numIterations = policy.primitives.keyDerivation.pbkdf.numIterations;
	const _hashAlgorithm = policy.primitives.keyDerivation.pbkdf.hashAlgorithm;
	const _minPBKDFPasswordLength = policy.primitives.keyDerivation.pbkdf.minPasswordLength;
	const _maxPBKDFPasswordLength = policy.primitives.keyDerivation.pbkdf.maxPasswordLength;

	/**
	 * Derives a key, using a PBKDF.
	 *
	 * @function derive
	 * @memberof PbkdfDeriver
	 * @param {string}
	 *            password The password from which to derive the key.
	 * @param {Uint8Array|string}
	 *            salt The salt from which to derive the key. <b>NOTE:</b> Salt
	 *            of type <code>string</code> will be UTF-8 encoded.
	 * @returns {Uint8Array} The derived key.
	 * @throws {Error}
	 *             If the input data validation fails.
	 */
	this.derive = function (password, salt) {
		if (typeof salt === 'string') {
			salt = codec.utf8Encode(salt);
		}
		checkData(password, salt);

		const generateHmac = function (key) {
			const generator = new sjcl.misc.hmac(key, getDigester(_hashAlgorithm));
			this.encrypt = function () {
				return generator.encrypt.apply(generator, arguments);
			};
		};

		// NOTE: The following Base64 encoding/decoding should be replaced with the
		// use of 'sjcl.codec.bytes.toBits/fromBits' when/if the latter becomes part
		// of the default sjcl build.
		const derivedKey = sjcl.misc.pbkdf2(
			password, sjcl.codec.base64.toBits(codec.base64Encode(salt)),
			_numIterations, _keyLength * 8, generateHmac);

		return codec.base64Decode(sjcl.codec.base64.fromBits(derivedKey));
	};

	function getDigester(algorithm) {
		if (algorithm ===
			Policy.options.primitives.keyDerivation.pbkdf.hashAlgorithm.SHA256) {
			return sjcl.hash.sha256;
		} else {
			throw new Error(
				'Hash algorithm \'' + algorithm + '\' is not recognized.');
		}
	}

	function checkData(password, salt) {
		validator.checkIsType(
			password, 'string', 'Password from which to derive key');
		validator.checkIsInstanceOf(
			salt, Uint8Array, 'Uint8Array', 'Salt from which to derive key');

		if (password.length < _minPBKDFPasswordLength) {
			throw new Error('The password length ' + password.length +
				' is less than the minimum allowed password length ' + _minPBKDFPasswordLength);
		}

		if (password.length > _maxPBKDFPasswordLength) {
			throw new Error('The password length ' + password.length +
				' is more than the maximum allowed password length ' + _maxPBKDFPasswordLength);
		}

		if (salt.length < _minSaltLength) {
			throw new Error(
				'The salt byte length ' + salt.length +
				' is less than the minimum allowed salt length ' + _minSaltLength +
				' set by the cryptographic policy.');
		}
	}
}
