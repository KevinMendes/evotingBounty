/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

/**
 * A utility for performing bitwise operations on data.
 */
module.exports = {
	/**
	 * Concatenates a list of bytes objects, in the order that they are provided
	 * as input.
	 *
	 * @function concatenate
	 * @param {...Uint8Array}
	 *            arguments A comma separated list of the bytes objects to
	 *            concatenate, in the order that they are provided.
	 * @returns {Uint8Array} The concatenation of the bytes objects.
	 */
	concatenate: function () {
		const arrays = Array.prototype.slice.call(arguments);

		let totalLength = 0;
		for (let i = 0; i < arrays.length; i++) {
			totalLength += arrays[i].length;
		}

		const concatenated = new Uint8Array(totalLength);
		let offset = 0;
		for (let j = 0; j < arrays.length; j++) {
			concatenated.set(arrays[j], offset);
			offset += arrays[j].length;
		}

		return concatenated;
	},

	/**
	 * Slices a subset of bytes from a concatenation of bytes.
	 *
	 * @function slice
	 * @param {Uint8Array}
	 *            concatenated The concatenation of bytes.
	 * @param {number}
	 *            start The index in the concatenation at which to start the
	 *            slice (inclusive).
	 * @param {number}
	 *            [end] The index in the concatenation at which to end the slice
	 *            (exclusive). If no end index is supplied then all elements
	 *            from the start index to the end of the concatenation will be
	 *            sliced.
	 * @returns {Uint8Array} The sliced subset of bytes.
	 */
	slice: function (concatenated, start, end) {
		const concatenatedArray = Array.apply([], concatenated);
		const slicedArray = concatenatedArray.slice(start, end);

		return new Uint8Array(slicedArray);
	},

	/**
	 * Prepends a byte to some existing bytes.
	 *
	 * @function prepend
	 * @param {byte}
	 *            byte The byte to prepend.
	 * @param {Uint8Array}
	 *            bytes The existing bytes.
	 * @returns {Uint8Array} The bytes after prepending.
	 */
	prepend: function (byte, bytes) {
		const prepended = new Uint8Array(1 + bytes.length);
		prepended.set(new Uint8Array([byte]));
		prepended.set(bytes, 1);

		return prepended;
	},

	/**
	 * Appends a byte to some existing bytes.
	 *
	 * @function append
	 * @param {Uint8Array}
	 *            bytes The existing bytes.
	 * @param {aByte}
	 *            aByte The byte to append.
	 * @returns {Uint8Array} The bytes after appending.
	 */
	append: function (bytes, aByte) {
		const appended = new Uint8Array(bytes.length + 1);
		appended.set(bytes);
		appended.set(new Uint8Array([aByte]), bytes.length);

		return appended;
	}
};
