/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const validator = require('../input-validator');
const bitwise = require('../bitwise');
const codec = require('../codec');
const forge = require('node-forge');

module.exports = SymmetricCipher;

/**
 * @class SymmetricCipher
 * @classdesc The symmetric cipher API. To instantiate this object, use the
 *            method {@link SymmetricCryptographyService.newCipher}.
 * @hideconstructor
 * @param {Policy}
 *            policy The cryptographic policy to use.
 * @param {SecureRandomService}
 *            secureRandomService The secure random service to use.
 */
function SymmetricCipher(policy, secureRandomService) {
	const _keyLengthBytes = policy.symmetric.cipher.algorithm.keyLengthBytes;
	const _algorithm = policy.symmetric.cipher.algorithm.name;
	const _tagLengthBytes = policy.symmetric.cipher.algorithm.tagLengthBytes;
	const _ivLengthBytes = policy.symmetric.cipher.ivLengthBytes;

	const _randomGenerator = secureRandomService.newRandomGenerator();

	let _initialized = false;

	let _cipher;

	/**
	 * Initializes the symmetric cipher with the provided secret key.
	 *
	 * @function init
	 * @memberof SymmetricCipher
	 * @param {Uint8Array}
	 *            key The key with which to initialize the symmetric cipher.
	 * @returns {SymmetricCipher} A reference to this object, to facilitate
	 *          method chaining.
	 * @throws {Error}
	 *             If the input data validation fails.
	 */
	this.init = function (key) {
		checkInitData(key);

		_cipher = forge.cipher.createCipher(_algorithm, codec.binaryEncode(key));
		_initialized = true;

		return this;
	};

	/**
	 * Symmetrically encrypts some data. Before using this method, the cipher
	 * must have been initialized with a secret key, via the method
	 * {@link SymmetricCipher.init}.
	 *
	 * @function encrypt
	 * @memberof SymmetricCipher
	 * @param {Uint8Array}
	 *            data The data to encrypt. <b>NOTE:</b> Data of type
	 *            <code>string</code> will be UTF-8 encoded.
	 * @returns {Uint8Array} The bitwise concatenation of the initialization
	 *          vector and the encrypted data.
	 * @throws {Error}
	 *             If the input data validation fails, the cipher was not
	 *             initialized or the encryption process fails.
	 */
	this.encrypt = function (data) {
		if (!_initialized) {
			throw new Error(
				'Could not encrypt; Symmetric cipher was not initialized with any secret key');
		}

		if (typeof data === 'string') {
			data = codec.utf8Encode(data);
		}
		validator.checkIsInstanceOf(
			data, Uint8Array, 'Uint8Array', 'Data to symmetrically encrypt');

		try {
			const iv = codec.binaryEncode(_randomGenerator.nextBytes(_ivLengthBytes));

			// Create a byte buffer for data.
			const dataBuffer = new forge.util.ByteBuffer(codec.binaryEncode(data));

			// Only for the GCM mode
			const gcmAuthTagByteLength = _tagLengthBytes;

			if (typeof gcmAuthTagBitLength !== 'undefined') {
				_cipher.start({iv: iv, tagLength: gcmAuthTagByteLength});
			} else {
				_cipher.start({iv: iv});
			}

			_cipher.update(dataBuffer);
			_cipher.finish();

			let encryptedData = _cipher.output.getBytes().toString();

			if (typeof gcmAuthTagByteLength !== 'undefined') {
				const gcmAuthTag = _cipher.mode.tag.getBytes();
				encryptedData = encryptedData + gcmAuthTag.toString();
			}

			const initVectorAndEncryptedData = iv + encryptedData;

			return codec.binaryDecode(initVectorAndEncryptedData);
		} catch (error) {
			throw new Error(
				'Data could not be symmetrically encrypted: ' + error.message);
		}
	};

	/**
	 * Symmetrically decrypts some data, using the initialization vector
	 * provided with the encrypted data. Before using this method, the cipher
	 * must have been initialized with a secret key, via the method
	 * {@link SymmetricCipher.init}.
	 *
	 * @function decrypt
	 * @memberof SymmetricCipher
	 * @param {Uint8Array}
	 *            initVectorAndEncryptedData The bitwise concatenation of the
	 *            initialization vector and the encrypted data.
	 * @returns {Uint8Array} The decrypted data. <b>NOTE:</b> To retrieve data
	 *          of type <code>string</code>, apply method
	 *          <code>codec.utf8Decode</code> to result.
	 * @throws {Error}
	 *             If the input data validation or the decryption process fails.
	 */
	this.decrypt = function (initVectorAndEncryptedData) {
		if (!_initialized) {
			throw new Error(
				'Could not decrypt; Symmetric cipher was not initialized with any secret key');
		}

		validator.checkIsInstanceOf(
			initVectorAndEncryptedData, Uint8Array, 'Uint8Array',
			'Concatenation of initialization vector and encrypted data to symmetrically decrypt');

		try {
			const initVector =
				bitwise.slice(initVectorAndEncryptedData, 0, _ivLengthBytes);
			let encryptedData = bitwise.slice(
				initVectorAndEncryptedData, _ivLengthBytes,
				initVectorAndEncryptedData.length);

			// Only for the GCM mode
			const gcmAuthTagByteLength = _tagLengthBytes;

			if (typeof gcmAuthTagByteLength !== 'undefined') {
				const offset = encryptedData.length - gcmAuthTagByteLength;

				const gcmAuthTag =
					bitwise.slice(encryptedData, offset, encryptedData.length);

				encryptedData = bitwise.slice(encryptedData, 0, offset);

				const gcmAuthTagBitLength = gcmAuthTagByteLength * 8;

				_cipher.start({
					iv: codec.binaryEncode(initVector),
					tagLength: gcmAuthTagBitLength,
					tag: codec.binaryEncode(gcmAuthTag)
				});

			} else {
				_cipher.start({iv: initVector});
			}

			const encryptedDataBuffer =
				new forge.util.ByteBuffer(codec.binaryEncode(encryptedData));

			_cipher.update(encryptedDataBuffer);
			_cipher.finish();
			return codec.binaryDecode(_cipher.output.getBytes().toString());
		} catch (error) {
			throw new Error('Data could not be symmetrically decrypted; ' + error);
		}
	};

	function checkInitData(key) {
		validator.checkIsInstanceOf(
			key, Uint8Array, 'Uint8Array',
			'Secret key with which to initialize symmetric cipher');

		if (key.length !== _keyLengthBytes) {
			throw new Error(
				'Expected secret key byte length ' + _keyLengthBytes +
				' ; Found: ' + key.length);
		}
	}
}
