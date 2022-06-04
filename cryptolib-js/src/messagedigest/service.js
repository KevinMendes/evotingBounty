/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const MessageDigester = require('./digester');
const cryptoPolicy = require('../cryptopolicy');

module.exports = MessageDigestService;

/**
 * @class MessageDigestService
 * @classdesc The message digest service API. To instantiate this object, use
 * the method {@link newService}.
 * @hideconstructor
 * @param {Object}
 *            [options] An object containing optional arguments.
 * @param {Policy}
 *            [options.policy=Default policy] The cryptographic policy to use.
 */
function MessageDigestService(options) {
    options = options || {};

	let policy;
	if (options.policy) {
        policy = options.policy;
    } else {
        policy = cryptoPolicy.newInstance();
    }

    /**
     * Creates a new MessageDigester object for generating message digests.
     *
     * @function newDigester
     * @memberof MessageDigestService
     * @returns {MessageDigester} The new MessageDigester object.
     */
    this.newDigester = function () {
        return new MessageDigester(policy);
    };
}
