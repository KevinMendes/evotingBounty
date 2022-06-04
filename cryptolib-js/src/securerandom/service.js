/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const SecureRandomGenerator = require('./generator');

module.exports = SecureRandomService;

/**
 * @class SecureRandomService
 * @classdesc The secure random service API. To instantiate this object, use the
 *            method {@link newService}.
 * @hideconstructor
 */
function SecureRandomService() {
	/**
	 * Creates a new SecureRandomGenerator object.
	 * This object can be used for all supported types of random generation.
	 *
	 * @function newRandomGenerator
	 * @memberof SecureRandomService
	 * @param {Object}
	 *            [options] An object containing optional arguments.
	 * @param {number}
	 *            [options.maxNumBytes=512] The maximum number of bytes that can
	 *            randomly generated per method call.
	 * @param {number}
	 *            [options.maxNumDigits=512] The maximum number of BigInteger
	 *            digits that can randomly generated per method call.
	 * @returns {SecureRandomGenerator} The new SecureRandomGenerator object.
	 */
	this.newRandomGenerator = function (options) {
		return new SecureRandomGenerator(options);
	};
}
