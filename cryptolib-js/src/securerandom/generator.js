/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const validator = require('../input-validator');
const constants = require('./constants');
const codec = require('../codec');
const forge = require('node-forge');

const BigInteger = forge.jsbn.BigInteger;

module.exports = SecureRandomGenerator;

/**
 * @class SecureRandomGenerator
 * @classdesc The secure random generator API. To instantiate this object, use
 * the method {@link SecureRandomService.newRandomGenerator}.
 * @hideconstructor
 * @param {Object}
 *            [options] An object containing optional arguments.
 * @param {number}
 *            [options.maxNumBytes=512] The maximum number of bytes that can be
 *            randomly generated per call.
 * @param {number}
 *            [options.maxNumDigits=512] The maximum number of BigInteger digits
 *            that can be randomly generated per call.
 */
function SecureRandomGenerator(options) {
	options = options || {};

	const _maxNumBytes = options.maxNumBytes || constants.SECURE_RANDOM_MAX_NUM_BYTES;
	const _maxNumDigits = options.maxNumDigits || constants.SECURE_RANDOM_MAX_NUM_DIGITS;

	/**
	 * Generates some random bytes.
	 *
	 * @function nextBytes
	 * @memberof SecureRandomGenerator
	 * @param {number}
	 *            numBytes The number of bytes to generate.
	 * @return {Uint8Array} The random bytes.
	 * @throws {Error}
	 *             If the input data validation fails.
	 */
	this.nextBytes = function (numBytes) {
		checkData(numBytes, _maxNumBytes, 'Number of bytes to randomly generate');
		let array = new Uint8Array(numBytes);

		// User agent WebAPI Crypto getRandomValues method
		const windowObj = getWindowObject();
		if (windowObj.crypto && windowObj.crypto.getRandomValues) {
			return windowObj.crypto.getRandomValues(array);
		}
		if (typeof windowObj.msCrypto === 'object' && typeof windowObj.msCrypto.getRandomValues === 'function') {
			return windowObj.msCrypto.getRandomValues(array);
		}

		throw new Error('Web API Crypto getRandomValues is not available');
	};

	/**
	 * Generates a random positive BigInteger object, with a specified maximum
	 * number of bits.
	 *
	 * @function nextBigInteger
	 * @memberof SecureRandomGenerator
	 * @param {number}
	 *            maxNumBits The maximum number of bits in the BigInteger
	 *            object.
	 * @returns {BigInteger} The random BigInteger object.
	 * @throws {Error}
	 *             If the input data validation fails.
	 */
	this.nextBigInteger = function (maxNumBits) {
		validator.checkIsPositiveNumber(
			maxNumBits,
			'Maximum number of bits in BigInteger to randomly generate');

		// Find minimum number of bytes needed for maximum number of bits.
		const numBytes = Math.ceil(maxNumBits / 8);

		const numExcessBits = 8 - (maxNumBits % 8);

		const randomBytes = this.nextBytes(numBytes);
		const randomByteArray = Array.apply([], randomBytes);
		randomByteArray.unshift(0);
		const generatedBigInteger = codec.bytesToBigInteger(randomBytes);

		return generatedBigInteger.shiftRight(numExcessBits);
	};

	/**
	 * Generates a random positive BigInteger object, with a specified maximum
	 * number of digits.
	 *
	 * @function nextBigIntegerByDigits
	 * @memberof SecureRandomGenerator
	 * @param {number}
	 *            numDigits The maximum number of digits in the BigInteger
	 *            object value.
	 * @returns {BigInteger} The random BigInteger object.
	 * @throws {Error}
	 *             If the input data validation fails.
	 */
	this.nextBigIntegerByDigits = function (numDigits) {
		checkData(
			numDigits, _maxNumDigits,
			'Number of digits in BigInteger to randomly generate');

		// Variable n is largest possible number for given number of digits. For
		// instance, for 3 digits, n would be 999.
		const n = ((new BigInteger('10')).pow(numDigits)).subtract(BigInteger.ONE);

		// Get number of bits needed to represent n.
		const numBits = n.bitLength();

		// Get minimum number of bytes needed to represent n.
		const numBytes = Math.ceil(numBits / 8);

		// Continuously generate numbers until number is found that is smaller than
		// n. Note: This will take just one round if number of bits is a multiple
		// of 8.
		let generatedBigInteger;
		do {
			const randomBytes = this.nextBytes(numBytes);
			const randomByteArray = Array.apply([], randomBytes);
			randomByteArray.unshift(0);
			generatedBigInteger = codec.bytesToBigInteger(randomBytes);
		} while ((generatedBigInteger.compareTo(BigInteger.ZERO) <= 0) ||
		(generatedBigInteger.compareTo(n) > 0));

		return generatedBigInteger;
	};

	/**
	 * Implementation of the method <code>getBytesSync</code>.
	 *
	 * NOTE:    The Forge Javascript library requires this method, since Forge disables native APIs and expects a
	 *          getBytesSync method.
	 *          Therefore, we must define this method even if the application does not invoke getBytesSync directly.
	 *
	 * @function getBytesSync
	 * @memberof SecureRandomGenerator
	 * @private
	 * @param {number} numBytes The number of bytes to generate.
	 * @returns {string} The random bytes, in binary encoded format.
	 */
	this.getBytesSync = function (numBytes) {
		return codec.binaryEncode(this.nextBytes(numBytes));
	};

	function checkData(num, max, label) {
		validator.checkIsPositiveNumber(num, label);

		if (num > max) {
			throw new Error(
				'Expected ' + label + ' to be less than or equal to ' + max +
				' ; Found: ' + num);
		}
	}

	function getWindowObject() {
		if (typeof window !== "undefined") {
			return window;
		} else if (typeof global !== "undefined") {
			return global;
		} else {
			return {};
		}
	}
}
