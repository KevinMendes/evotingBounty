/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const Exponent = require('./exponent');
const constants = require('./constants');
const validator = require('../input-validator');

module.exports = MathematicalRandomGenerator;

/**
 * @class MathematicalRandomGenerator
 * @classdesc The mathematical random generator API. To instantiate this object,
 *            use the method {@link MathematicalService.newRandomGenerator}.
 * @hideconstructor
 * @param {SeureRandomService}
 *            secureRandomService The secure random service to use.
 */
function MathematicalRandomGenerator(secureRandomService) {
	const _randomGenerator = secureRandomService.newRandomGenerator();

	/**
	 * Generates a random exponent associated with the Zp subgroup provided as
	 * input. The value of the exponent will be within the range <code>[0,
	 * q-1]</code>.
	 *
	 * @function nextExponent
	 * @memberof MathematicalRandomGenerator
	 * @param {ZpSubgroup}
	 *            group The Zp subgroup to which the exponent is to be
	 *            associated.
	 * @param {Object}
	 *            [options] An object containing optional arguments.
	 * @param {SecureRandomGenerator}
	 *            [options.secureRandomGenerator=Internal generator is used] The
	 *            secure random generator to use.
	 * @param {boolean}
	 *            [options.useShortExponent=false] If <code>true</code>, then
	 *            a short exponent is to be generated.
	 * @returns {Exponent} The generated exponent.
	 * @throws {Error}
	 *             If the input validation fails.
	 */
	this.nextExponent = function (group, options) {
		validator.checkIsObjectWithProperties(
			group,
			'Zp subgroup to which randomly generated exponent is to be associated');

		options = options || {};

		const randomGenerator = options.secureRandomGenerator || _randomGenerator;
		validator.checkIsObjectWithProperties(
			randomGenerator,
			'Random BigInteger generator for random exponent generation');

		const useShortExponent = options.useShortExponent || false;

		const q = group.q;
		const qBitLength = q.bitLength();
		let randomExponentBitLength;
		if (useShortExponent) {
			if (qBitLength < constants.SHORT_EXPONENT_BIT_LENGTH) {
				throw new Error(
					'Zp subgroup order bit length must be greater than or equal to short exponent bit length : ' +
					constants.SHORT_EXPONENT_BIT_LENGTH + '; Found ' + qBitLength);
			}
			randomExponentBitLength = constants.SHORT_EXPONENT_BIT_LENGTH;
		} else {
			randomExponentBitLength = qBitLength;
		}

		let randomExponentValue;
		let randomExponentFound = false;
		while (!randomExponentFound) {
			randomExponentValue =
				randomGenerator.nextBigInteger(randomExponentBitLength);
			if (randomExponentValue.compareTo(q) < 0) {
				randomExponentFound = true;
			}
		}

		return new Exponent(q, randomExponentValue);
	};
}
