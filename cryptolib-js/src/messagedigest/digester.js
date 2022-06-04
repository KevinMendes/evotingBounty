/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const validator = require('../input-validator');
const cryptoPolicy = require('../cryptopolicy');
const codec = require('../codec');
const forge = require('node-forge');

module.exports = MessageDigester;

/**
 * @class MessageDigester
 * @classdesc The message digester API. To instantiate this object, use the
 *            method {@link MessageDigestService.newDigester}.
 * @hideconstructor
 * @param {Policy}
 *            policy The cryptographic policy to use.
 */
function MessageDigester(policy) {
	const _algorithm = policy.primitives.messageDigest.algorithm;
	const _digester = getDigester(_algorithm);
	let _updated = false;
	_digester.start();

    /**
     * Generates a message digest from the provided data. If there were any
     * prior calls to the method <code>update</code>, then the provided data
     * will be bitwise appended to the data provided to those calls before
     * digesting. If no data is provided here the digest will only by generated
     * for the data provided to prior calls to the method <code>update</code>.
     * The message digester will be automatically reinitialized after this
     * method completes.
     *
     * @function digest
     * @memberof MessageDigester
     * @param {Uint8Array|string}
     *            [data] The data from which to generate the message digest.
     *            <b>NOTE:</b> Data of type <code>string</code> will be UTF-8
     *            encoded.
     * @returns {Uint8Array} The generated message digest.
     * @throws {Error}
     *             If no data was provided as input and the update method was
     *             not previously called.
     */
    this.digest = function (data) {
        if (typeof data !== 'undefined') {
            if (typeof data === 'string') {
                data = codec.utf8Encode(data);
            }
            validator.checkIsInstanceOf(
                data, Uint8Array, 'Uint8Array', 'Data to digest');

            this.update(data);
        } else if (!_updated) {
            throw new Error(
                'Attempt to generate message digest without either providing data as input or having made previous call to method \'update\'');
        }

		const bytesHexEncoded = _digester.digest().toHex();

		// Reinitialize digester.
        _digester.start();
        _updated = false;

        return codec.hexDecode(bytesHexEncoded);
    };

    /**
     * Updates the message digester with the provided data. The data will be
     * internally bitwise appended to any data provided to previous calls to
     * this method, after the last call to the method <code>digest</code>.
     *
     * @function update
     * @memberof MessageDigester
     * @param {Uint8Array|string}
     *            data The data with which to update the message digester.
     *            <b>NOTE:</b> Data of type <code>string</code> will be UTF-8
     *            encoded.
     * @returns {MessageDigester} A reference to this object, to facilitate
     *          method chaining.
     * @throws {Error}
     *             If the input data validation fails.
     */
    this.update = function (data) {
        if (typeof data === 'string') {
            data = codec.utf8Encode(data);
        }
        validator.checkIsInstanceOf(
            data, Uint8Array, 'Uint8Array', 'Data to update message digester');

        _digester.update(codec.binaryEncode(data));
        _updated = true;

        return this;
    };

    /**
     * Creates an instance of a digester according to the specified
     * algorithm.
     *
     * @param {string} algorithm the name of the algorithm, coming
     *        from the policy.
     * @returns {forge.md.MessageDigest} a newly created digester.
     */
    function getDigester(algorithm) {
		const options = cryptoPolicy.options.primitives.messageDigest.algorithm;

		switch (algorithm) {
            case options.SHA256:
                return forge.md.sha256.create();
            case options.SHA512_224:
                return forge.md.sha512.sha224.create();
            default:
                throw new Error(
                    'Could not create new message digester for unrecognized hash algorithm \'' +
                    algorithm + '\'.');
        }
    }

}
