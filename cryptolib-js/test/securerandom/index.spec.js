/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
/* jshint node:true, jasmine:true */
'use strict';

const secureRandom = require('../../src/securerandom');
const constants = require('../../src/securerandom/constants');
const forge = require('node-forge');

const BigInteger = forge.jsbn.BigInteger;

let _service;
let _randomGenerator;

const beforeEachHook = function () {
	_service = secureRandom.newService();
	_randomGenerator = _service.newRandomGenerator();
};

describe('The secure random module should be able to create a secure random generator that should be able to ...', function () {

	beforeEach(function () {
		beforeEachHook();
	});

	it('generate 10 random bytes', function () {
		const numBytes = 10;
		const bytes = _randomGenerator.nextBytes(numBytes);
		expect(bytes).toBeDefined();
		expect(bytes.length).not.toBeGreaterThan(numBytes);
	});

	it('generate a specified number of random bytes', function () {
		const numBytes = 16;
		const bytes = _randomGenerator.nextBytes(numBytes);
		expect(bytes).toBeDefined();
		expect(bytes.numBytes).not.toBeGreaterThan(numBytes);
	});

	it('generate a random BigInteger of a specified maximum bit length',
		function () {
			for (let maxNumBits = 1; maxNumBits <= 20; maxNumBits++) {
				const randomBigInteger = _randomGenerator.nextBigInteger(maxNumBits);
				expect(randomBigInteger).not.toBeLessThan(0);
				expect(randomBigInteger.bitLength())
					.not.toBeGreaterThan(maxNumBits);
			}
		});

	it('generate a random BigInteger of a specified number of digits',
		function () {
			let maxValue;

			for (let numDigits = 1; numDigits <= 10; numDigits++) {
				maxValue = Math.pow(10, numDigits);
				const randomBigInteger = _randomGenerator.nextBigIntegerByDigits(numDigits);
				expect(randomBigInteger.compareTo(BigInteger.ZERO))
					.toBeGreaterThan(0);
				expect(randomBigInteger.compareTo(
					new BigInteger(maxValue.toString())))
					.toBeLessThan(0);
			}
		});

	it('be created by specifying a maximum allowed number of bytes',
		function () {
			const maxNumBytes = constants.SECURE_RANDOM_MAX_NUM_BYTES + 1;
			const _randomGenerator = _service.newRandomGenerator({maxNumBytes: maxNumBytes});
			const randomBytes = _randomGenerator.nextBytes(maxNumBytes);
			expect(randomBytes).toBeDefined();
			expect(randomBytes.length).not.toBeGreaterThan(maxNumBytes);
			expect(function () {
				_randomGenerator.nextBytes(maxNumBytes + 1);
			}).toThrow();
		});

	it('be created by specifying maximum allowed number of digits',
		function () {
			const maxNumDigits = constants.SECURE_RANDOM_MAX_NUM_BYTES + 1;
			const _randomGenerator = _service.newRandomGenerator({maxNumDigits: maxNumDigits});
			const randomBigInteger = _randomGenerator.nextBigIntegerByDigits(maxNumDigits);
			expect(randomBigInteger).toBeDefined();
			expect(randomBigInteger.compareTo(BigInteger.ZERO)).toBeGreaterThan(0);
			expect(randomBigInteger.compareTo(new BigInteger('10').pow(maxNumDigits))).toBeLessThan(0);

			expect(function () {
				_randomGenerator.nextBigIntegerByDigits(maxNumDigits + 1);
			}).toThrow();
		});

	it('throw an exception when a negative input argument is passed',
		function () {
			expect(function () {
				_randomGenerator.nextBytes(-1);
			}).toThrow();

			expect(function () {
				_randomGenerator.nextBigInteger(-1);
			}).toThrow();

			expect(function () {
				_randomGenerator.nextBigIntegerByDigits(-1);
			}).toThrow();
		});

	it('throw an exception when a non-numeric input argument is passed',
		function () {
			expect(function () {
				_randomGenerator.nextBytes('abc');
			}).toThrow();

			expect(function () {
				_randomGenerator.nextBigInteger('abc');
			}).toThrow();

			expect(function () {
				_randomGenerator.nextBigIntegerByDigits('abc');
			}).toThrow();
		});

	it('throw an exception when a too-large input argument is passed',
		function () {
			expect(function () {
				_randomGenerator.nextBytes(constants.SECURE_RANDOM_MAX_NUM_BYTES + 1);
			}).toThrow();

			expect(function () {
				_randomGenerator.nextBigIntegerByDigits(constants.SECURE_RANDOM_MAX_NUM_DIGITS + 1);
			}).toThrow();
		});
});

