/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const validator = require('../input-validator');
const codec = require('../codec');
const forge = require('node-forge');

module.exports = MacHandler;

/**
 * @class MacHandler
 * @classdesc The MAC handler API. To instantiate this object, use the method
 *            {@link SymmetricCryptographyService.newMacHandler}.
 * @hideconstructor
 * @param {Policy}
 *            policy The cryptographic policy to use.
 */
function MacHandler(policy) {
	let _macHandler;
	let _keyByteBuffer;
	let _updated = false;

	/**
	 * Initializes the MAC handler with the provided secret key.
	 *
	 * @function init
	 * @memberof MacHandler
	 * @param {Uint8Array}
	 *            key The key with which to initialize the MAC handler.
	 * @returns {MacHandler} A reference to this object, to facilitate method
	 *          chaining.
	 * @throws {Error}
	 *             If the input data validation fails or the MAC handler could
	 *             not be initialized.
	 */
	this.init = function (key) {
		validator.checkIsInstanceOf(
			key, Uint8Array, 'Uint8Array',
			'Secret key with which to initialize MAC handler');

		try {
			_macHandler = forge.hmac.create();
			_keyByteBuffer = new forge.util.ByteBuffer(codec.binaryEncode(key));
			_macHandler.start(policy.symmetric.mac.hashAlgorithm, _keyByteBuffer);
		} catch (error) {
			throw new Error('MAC handler could not be initialized: ' + error.message);
		}

		return this;
	};

	/**
	 * Generates a MAC from the provided data. If there were any prior calls to
	 * the method <code>update</code>, then the provided data will be bitwise
	 * appended to the data provided to those calls. If no data is provided here
	 * the MAC will only by generated for the data provided to prior calls to
	 * the method <code>update</code>. The MAC handler will be automatically
	 * reinitialized after this method completes. Before using this method, the
	 * MAC handler must have been initialized with a secret key, via the method
	 * {@link MacHandler.init}.
	 *
	 * @function generate
	 * @memberof MacHandler
	 * @param {Uint8Array|string}
	 *            [data] The data from which to generate the MAC. <b>NOTE:</b>
	 *            Data of type <code>string</code> will be UTF-8 encoded.
	 * @returns {Uint8Array} The generated MAC.
	 * @throws {Error}
	 *             If the input data validation fails, the MAC handler was not
	 *             initialized, the MAC handler was not updated with any data or
	 *             the MAC generation process fails.
	 */
	this.generate = function (data) {
		if (typeof _keyByteBuffer === 'undefined') {
			throw new Error(
				'Could not generate MAC; MAC handler was not initialized with any secret key');
		}

		if (typeof data !== 'undefined') {
			if (typeof data === 'string') {
				data = codec.utf8Encode(data);
			}
			validator.checkIsInstanceOf(
				data, Uint8Array, 'Uint8Array', 'Data provided to MAC generator');
			this.update(data);
		} else if (!_updated) {
			throw new Error(
				'Attempt to generate MAC without either providing data as input or having made previous call to method \'update\'');
		}

		try {
			const macBinaryEncoded = _macHandler.digest().getBytes();

			_macHandler.start(policy.symmetric.mac.hashAlgorithm, _keyByteBuffer);
			_updated = false;

			return codec.binaryDecode(macBinaryEncoded);
		} catch (error) {
			throw new Error('MAC could not be generated: ' + error.message);
		}
	};

	function validateData(data) {
		if (typeof data !== 'undefined') {
			if (typeof data === 'string') {
				data = codec.utf8Encode(data);
			}
			validator.checkIsInstanceOf(
				data, Uint8Array, 'Uint8Array', 'Data provided to MAC verifier');
		} else if (!_updated) {
			throw new Error(
				'Attempt to verify MAC without either providing data as input or having made previous call to method \'update\'');
		}
	}

	/**
	 * Verfies a that MAC was generated from the provided data. If there were
	 * any prior calls to the method <code>update</code>, then the provided
	 * data will be bitwise appended to the data provided to those calls. If no
	 * data is provided here the MAC will only by verified for the data provided
	 * to prior calls to the method <code>update</code>. The MAC handler will
	 * be automatically reinitialized after this method completes. Before using
	 * this method, the MAC handler must have been initialized with a secret
	 * key, via the method {@link MacHandler.init}.
	 *
	 * @function verify
	 * @memberof MacHandler
	 * @param {Uint8Array}
	 *            mac The MAC to be verified.
	 * @param {Uint8Array}
	 *            [data] The data to check against the MAC. <b>NOTE:</b> Data
	 *            of type <code>string</code> will be UTF-8 encoded.
	 * @returns <code>True</code> if the MAC was verified, <code>false</code>
	 *          otherwise.
	 * @throws {Error}
	 *             If the input data validation fails, the MAC handler was not
	 *             initialized or the MAC handler was not updated with any data.
	 */
	this.verify = function (mac, data) {
		if (typeof _keyByteBuffer === 'undefined') {
			throw new Error(
				'Could not verify MAC; MAC handler was not initialized with any secret key');
		}

		validator.checkIsInstanceOf(
			mac, Uint8Array, 'Uint8Array', 'MAC provided to MAC verifier');
		validateData(data);

		const macFromData = this.generate(data);

		if (macFromData.length !== mac.length) {
			return false;
		}

		let verified = true;
		for (let i = 0; i < macFromData.length; i++) {
			if (macFromData[i] !== mac[i]) {
				verified = false;
			}
		}

		return verified;
	};

	/**
	 * Updates the MAC handler with the provided data. The data will be
	 * internally bitwise concatenated to any data provided via previous calls
	 * to this method, after the last call to the method <code>generate</code>
	 * or the method <code>verify</code>. Before using this method, the MAC
	 * handler must have been initialized with a secret key, via the method
	 * {@link MacHandler.init}.
	 *
	 * @function update
	 * @memberof MacHandler
	 * @param {Uint8Array}
	 *            data The data with which to update the MAC handler. <b>NOTE:</b>
	 *            Data of type <code>string</code> will be UTF-8 encoded.
	 * @returns {MacHandler} A reference to this object, to facilitate method
	 *          chaining.
	 * @throws {Error}
	 *             If the input data validation fails or the MAC could not be
	 *             updated.
	 */
	this.update = function (data) {
		if (typeof _keyByteBuffer === 'undefined') {
			throw new Error(
				'Could not update MAC; MAC handler was not initialized with any secret key');
		}

		if (typeof data === 'string') {
			data = codec.utf8Encode(data);
		}
		validator.checkIsInstanceOf(
			data, Uint8Array, 'Uint8Array',
			'Data with which to update MAC handler');

		try {
			_macHandler.update(codec.binaryEncode(data));
			_updated = true;
		} catch (error) {
			throw new Error('MAC handler could not be updated: ' + error.message);
		}

		return this;
	};
}
