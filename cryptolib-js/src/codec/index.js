/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const forge = require('node-forge');

/**
 * Codec for converting between various types of data.
 */
module.exports = {
	/**
	 * UTF-8 encodes a string.
	 *
	 * @function utf8Encode
	 * @param {string}
	 *            str The string to UTF-8 encode.
	 * @returns {Uint8Array} The UTF-8 encoded string.
	 */
	utf8Encode: function (str) {
		return this.binaryDecode(forge.util.encodeUtf8(str));
	},

	/**
	 * UTF-8 decodes an encoded string.
	 *
	 * @function utf8Decode
	 * @param {Uint8Array}
	 *            encoded The encoded string to UTF-8 decode.
	 * @returns {string} The UTF-8 decoded string.
	 */
	utf8Decode: function (encoded) {
		return forge.util.decodeUtf8(this.binaryEncode(encoded));
	},

	/**
	 * Converts a BigInteger object to bytes.
	 *
	 * @function bigIntegerToBytes
	 * @param {forge.jsbn.BigInteger}
	 *            bigInt The BigInteger object to convert.
	 * @returns {Uint8Array} The bytes from the conversion.
	 */
	bigIntegerToBytes: function (bigInt) {
		return new Uint8Array(bigInt.toByteArray());
	},

	/**
	 * Converts some bytes to a BigInteger object.
	 *
	 * @function bytesToBigInteger
	 * @param {Uint8Array}
	 *            bytes The bytes to convert.
	 * @returns {forge.jsbn.BigInteger} The BigInteger object from the
	 *          conversion.
	 */
	bytesToBigInteger: function (bytes) {
		const hex = this.hexEncode(bytes);

		return new forge.jsbn.BigInteger(hex, 16);
	},

	/**
	 * Base64 encodes some data.
	 * <p>
	 * <b>NOTE:</b> Input of type <code>string</code> will be UTF-8 encoded.
	 *
	 * @function base64Encode
	 * @param {Uint8Array|string|BigInteger}
	 *            data The data to Base64 encode.
	 * @param {Object}
	 *            [options] An object containing optional arguments.
	 * @param {number}
	 *            [options.lineLength=No line breaks] The line length of the
	 *            result.
	 * @returns {string} The Base64 encoded data.
	 */
	base64Encode: function (data, options) {
		options = options || {};

		if (typeof data === 'string') {
			data = this.utf8Encode(data);
		} else if (!(data instanceof Uint8Array)) {
			data = this.bigIntegerToBytes(data);
		}

		if (typeof options.lineLength === 'undefined') {
			return forge.util.binary.base64.encode(data);
		} else {
			return forge.util.binary.base64.encode(data, options.lineLength);
		}
	},

	/**
	 * Base64 decodes some encoded data.
	 * <p>
	 * <b>NOTE:</b> To retrieve data of type <code>string</code>, apply method
	 * <code>utf8Decode</code> to result.
	 * <p>
	 * <b>NOTE:</b> To retrieve data of type <code>BigInteger</code>, apply method
	 * <code>bytesToBigInteger</code> to result.
	 *
	 * @function base64Decode
	 * @param {string}
	 *            baseB64 The encoded data to Base64 decode.
	 * @returns {Uint8Array} The Base64 decoded data.
	 */
	base64Decode: function (baseB64) {
		return new Uint8Array(forge.util.binary.base64.decode(baseB64));
	},

	/**
	 * Hexadecimally encodes some data.
	 * <p>
	 * <b>NOTE:</b> Input of type <code>string</code> will be UTF-8 encoded.
	 *
	 * @function hexEncode
	 * @param {Uint8Array|string|BigInteger}
	 *            data The data to hexadecimally encode.
	 * @returns {string} The hexadecimally encoded data.
	 */
	hexEncode: function (data) {
		if (data instanceof Uint8Array) {
			return forge.util.binary.hex.encode(data);
		} else if (typeof data === 'string') {
			return forge.util.binary.hex.encode(this.utf8Encode(data));
		} else {
			const bytes = this.bigIntegerToBytes(data);
			return forge.util.binary.hex.encode(bytes);
		}
	},

	/**
	 * Hexadecimally decodes some encoded data.
	 * <p>
	 * <b>NOTE:</b> To retrieve data of type <code>string</code>, apply method
	 * <code>utf8Decode</code> to result.
	 * <p>
	 * <b>NOTE:</b> To retrieve data of type <code>BigInteger</code>, apply method
	 * <code>bytesToBigInteger</code> to result.
	 *
	 * @function hexDecode
	 * @param {string}
	 *            hex The encoded data to hexadecimally decode.
	 * @returns {Uint8Array} The hexadecimally decoded data.
	 */
	hexDecode: function (hex) {
		return forge.util.binary.hex.decode(hex);
	},

	/**
	 * Binary encodes some bytes. This method is only intended for internal use.
	 *
	 * @function binaryEncode
	 * @private
	 * @param {Uint8Array|byte[]}
	 *            byteArray The bytes to binary encode.
	 * @returns {string} The binary encoded bytes.
	 */
	binaryEncode: function (bytes) {
		let encoded = '';
		for (let i = 0; i < bytes.length; i++) {
			encoded += String.fromCharCode(bytes[i]);
		}

		return encoded;
	},

	/**
	 * Binary decodes some encoded bytes. This method is only intended for
	 * internal use.
	 *
	 * @function binaryDecode
	 * @private
	 * @param {string}
	 *            encoded The encoded bytes to binary decode.
	 * @returns {Uint8Array} The binary decoded bytes.
	 */
	binaryDecode: function (encoded) {
		const byteArray = [];
		for (let i = 0; i < encoded.length; i++) {
			byteArray.push(encoded.charCodeAt(i));
		}

		return new Uint8Array(byteArray);
	}
};
