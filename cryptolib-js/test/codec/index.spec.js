/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, jasmine:true, expr:true */
'use strict';

const { assert, expect } = require('chai');

const codec = require('../../src/codec');
const forge = require('node-forge');

const BigInteger = forge.jsbn.BigInteger;

describe('The codec ...', function () {
	const testString = 'SwissPost';
	const testStringBytes = new Uint8Array([83, 119, 105, 115, 115, 80, 111, 115, 116]);
	const testStringNonAscii = '你好';
	const testStringNonAsciiBytes = new Uint8Array([228, 189, 160, 229, 165, 189]);
	const testStringBase64 = 'U3dpc3NQb3N0';
	const testNumberString = '1234';
	const testNumberOneBase64 = 'AQ==';
	const testNumberBase64 = 'BNI=';
	const testStringHex = '5377697373506f7374';
	const testNumberOneHex = '01';
	const testNumberHex = '04d2';
	const testNumberBytes = new Uint8Array([4, 210]);

	describe('should be able to ..', function () {
		it('UTF-8 encode a string', function () {
			expect(codec.utf8Encode(testString)).to.deep.equal(testStringBytes);
			expect(codec.utf8Encode(testStringNonAscii))
				.to.deep.equal(testStringNonAsciiBytes);
		});

		it('UTF-8 decode an encoded string', function () {
			expect(codec.utf8Decode(testStringBytes)).to.equal(testString);
			expect(codec.utf8Decode(testStringNonAsciiBytes))
				.to.equal(testStringNonAscii);
		});

		it('Convert some bytes to a BigInteger object', function () {
			assert.isTrue(codec.bytesToBigInteger(testNumberBytes)
				.equals(new BigInteger(testNumberString)));
		});

		it('Convert a BigInteger object to bytes', function () {
			expect(codec.bigIntegerToBytes(new BigInteger(testNumberString)))
				.to.deep.equal(testNumberBytes);
		});

		it('Base64 encode some bytes', function () {
			expect(codec.base64Encode(testStringBytes)).to.equal(testStringBase64);
		});

		it('Base64 encode a string', function () {
			expect(codec.base64Encode(testString)).to.equal(testStringBase64);
		});

		it('Base64 encode a BigInteger object', function () {
			expect(codec.base64Encode(BigInteger.ONE)).to.equal(testNumberOneBase64);
			expect(codec.base64Encode(new BigInteger(testNumberString)))
				.to.equal(testNumberBase64);
		});

		it('Base64 encode some bytes, using a specified output line length',
			function () {
				const base64 = codec.base64Encode(testStringBytes, {lineLength: 2});
				expect(base64.indexOf('\n')).to.equal(3);
			});

		it('Base64 encode a string, using a specified output line length',
			function () {
				const base64 = codec.base64Encode(testString, {lineLength: 2});
				expect(base64.indexOf('\n')).to.equal(3);
			});

		it('Base64 encode a BigInteger object, using a specified output line length',
			function () {
				const base64 = codec.base64Encode(
					new BigInteger(testNumberString), {lineLength: 2});
				expect(base64.indexOf('\n')).to.equal(3);
			});

		it('Base64 decode some encoded bytes', function () {
			expect(codec.base64Decode(testStringBase64)).to.deep.equal(testStringBytes);
		});

		it('Base64 decode a string', function () {
			expect(codec.utf8Decode(codec.base64Decode(testStringBase64)))
				.to.equal(testString);
		});

		it('Base64 decode an encoded BigInteger object', function () {
			assert.isTrue(codec.bytesToBigInteger(codec.base64Decode(testNumberOneBase64)).equals(BigInteger.ONE));
			assert.isTrue(codec.bytesToBigInteger(codec.base64Decode(testNumberBase64)).equals(new BigInteger(testNumberString)));
		});

		it('Hexadecimally encode some bytes', function () {
			expect(codec.hexEncode(testStringBytes)).to.equal(testStringHex);
		});

		it('Hexadecimally encode a string', function () {
			expect(codec.hexEncode(testString)).to.equal(testStringHex);
		});

		it('Hexadecimally encode a BigInteger object', function () {
			expect(codec.hexEncode(BigInteger.ONE)).to.equal(testNumberOneHex);
			expect(codec.hexEncode(new BigInteger(testNumberString)))
				.to.equal(testNumberHex);
		});

		it('Hexadecimally decode some encoded bytes', function () {
			expect(codec.hexDecode(testStringHex)).to.deep.equal(testStringBytes);
		});

		it('Hexadecimally decode an encoded string', function () {
			expect(codec.utf8Decode(codec.hexDecode(testStringHex))).to.equal(testString);
		});

		it('Hexadecimally decode an encoded BigInteger object', function () {
			assert.isTrue(codec.bytesToBigInteger(codec.hexDecode(testNumberOneHex)).equals(BigInteger.ONE));
			assert.isTrue(codec.bytesToBigInteger(codec.hexDecode(testNumberHex)).equals(new BigInteger(testNumberString)));
		});

		it('Binary encode some bytes', function () {
			expect(codec.binaryEncode(testStringBytes)).to.equal(testString);
		});

		it('Binary decode some encoded bytes', function () {
			expect(codec.binaryDecode(testString)).to.deep.equal(testStringBytes);
		});

		it('Base64 encode some binary decoded bytes', function () {
			expect(codec.base64Encode(codec.binaryDecode(testString)))
				.to.equal(testStringBase64);
		});

		it('Base64 encode some binary decoded bytes, using a specified output line length',
			function () {
				const base64 = codec.base64Encode(
					codec.binaryDecode(testString), {lineLength: 2});
				expect(base64.indexOf('\n')).to.equal(3);
			});

		it('Base64 decode some binary encoded bytes', function () {
			expect(codec.binaryEncode(codec.base64Decode(testStringBase64)))
				.to.equal(testString);
		});

		it('Hexadecimally encode some binary decoded bytes', function () {
			expect(codec.hexEncode(codec.binaryDecode(testString)))
				.to.equal(testStringHex);
		});

		it('Hexadecimally decode some binary decoded bytes', function () {
			expect(codec.binaryEncode(codec.hexDecode(testStringHex)))
				.to.equal(testString);
		});
	});
});
