/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const MessageDigestService = require('./service');
const validator = require('../input-validator');

module.exports = {
	/**
	 * Creates a new MessageDigestService object, which encapsulates a message
	 * digiest service.
	 *
	 * @function newService
	 * @global
	 * @param {Object}
	 *            [options] An object containing optional arguments.
	 * @param {Policy}
	 *            [options.policy=Default policy] The cryptographic policy to
	 *            use.
	 * @returns {MessageDigestService} The new MessageDigestService object.
	 * @throws {Error}
	 *             If the input data validation fails.
	 */
	newService: function (options) {
		checkData(options);

		return new MessageDigestService(options);
	}
};

function checkData(options) {
	options = options || {};

	if (typeof options.policy !== 'undefined') {
		validator.checkIsObjectWithProperties(
			options.policy,
			'Cryptographic policy provided to message digest service');
	}
}
