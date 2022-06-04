/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, mocha:true */
'use strict';

const { expect } = require('chai');

const bitwise = require('../../src/bitwise');

describe('The bitwise utility ...', function () {
	const BYTE_ARRAY_1 = [105, 73, 99];
	const BYTE_ARRAY_2 = [86, 43, 215];
	const BYTE_ARRAY_3 = [33, 163, 41];
	const BYTE = 0x05;

	let _allBytesArray;
	let _prependedArray;
	let _appendedArray;
	let _bytes1;
	let _bytes2;
	let _bytes3;
	let _allBytes;

	beforeEach(function () {
		_allBytesArray = BYTE_ARRAY_1.concat(BYTE_ARRAY_2, BYTE_ARRAY_3);

		_prependedArray = [BYTE].concat(_allBytesArray);

		_appendedArray = _allBytesArray.concat([BYTE]);

		_bytes1 = new Uint8Array(BYTE_ARRAY_1);
		_bytes2 = new Uint8Array(BYTE_ARRAY_2);
		_bytes3 = new Uint8Array(BYTE_ARRAY_3);
		_allBytes = new Uint8Array(_allBytesArray);
	});

	describe('should be able to ..', function () {
		it('concatenate some bytes to some existing bytes', function () {
			const concatenated = bitwise.concatenate(_bytes1, _bytes2, _bytes3);

			expect(Array.apply([], concatenated)).to.deep.equal(_allBytesArray);
		});

		it('slice a subset of bytes from a concatenation of bytes', function () {
			let sliced = bitwise.slice(
				_allBytes, _bytes1.length, _bytes1.length + _bytes2.length);

			expect(Array.apply([], sliced)).to.deep.equal(BYTE_ARRAY_2);

			sliced = bitwise.slice(_allBytes, _bytes1.length);

			expect(Array.apply([], sliced))
				.to.deep.equal(BYTE_ARRAY_2.concat(BYTE_ARRAY_3));
		});

		it('prepend a byte to some existing bytes', function () {
			const prepended = bitwise.prepend(BYTE, _allBytes);

			expect(Array.apply([], prepended)).to.deep.equal(_prependedArray);
		});

		it('apppend a byte to some existing bytes', function () {
			const appended = bitwise.append(_allBytes, BYTE);

			expect(Array.apply([], appended)).to.deep.equal(_appendedArray);
		});
	});
});
