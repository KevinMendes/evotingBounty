/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const cryptoPolicy = require('../cryptopolicy');
const PbkdfDeriver = require('./deriver');

module.exports = PbkdfService;

/**
 * @class PbkdfService
 * @classdesc The PBKDF service API. To instantiate this object, use the method
 * {@link newService}.
 * @hideconstructor
 * @param {Object}
 *            [options] An object containing optional arguments.
 * @param {Policy}
 *            [options.policy=Default policy] The cryptographic policy to use.
 */
function PbkdfService(options) {
	options = options || {};

	let policy;
	if (options.policy) {
		policy = options.policy;
	} else {
		policy = cryptoPolicy.newInstance();
	}

	/**
	 * Creates a new PbkdfDeriver object for deriving keys using a PBKDF.
	 *
	 * @function newDeriver
	 * @memberof PbkdfService
	 * @returns {PbkdfDeriver} The new PbkdfDeriver object.
	 */
	this.newDeriver = function () {
		return new PbkdfDeriver(policy);
	};
}
