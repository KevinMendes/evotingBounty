/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
/* jshint node:true, jasmine:true */
'use strict';

const secureRandom = require('../../src/securerandom');
const constants = require('../../src/securerandom/constants');


let _randomGenerator;
const _nonNumber = 'not a number';
const _nonPositiveNumber = 0;

const describeText = 'The secure random module should be able to ...';

describe(describeText, function () {

	it('return a valid service after requiring the module', function () {
		expect(function () {
			const generator = secureRandom.newRandomGenerator();
			expect(generator.nextBytes(3).length).toBe(3);
        }).not.toThrow();
    });

	it('create new services', function () {
		expect(function () {
			expect(secureRandom.newService()).toBeDefined();
		});
	});

	it('return a different service than the one obtained after requiring the module', function () {
		const otherService = secureRandom.newService();
		expect(otherService).not.toEqual(secureRandom);
	});

	it('return the same service after requiring the module twice', function () {
		const once = require('../../src/securerandom');
		const twice = require('../../src/securerandom');

		expect(once).toEqual(twice);
	});

});


describe(describeText, function () {
	describe('create a secure random service that should be able to ..', function () {
		beforeEach(function () {
			_randomGenerator = secureRandom.newService().newRandomGenerator();
		});
		describe('create a secure random generator that should be able to', function () {
			it('throw an exception when generating random bytes, using invalid input data',
				function () {
					expect(function () {
						_randomGenerator.nextBytes();
					}).toThrow();

					expect(function () {
						_randomGenerator.nextBytes(undefined);
					}).toThrow();

					expect(function () {
						_randomGenerator.nextBytes(null);
					}).toThrow();

					expect(function () {
						_randomGenerator.nextBytes(_nonNumber);
					}).toThrow();

					expect(function () {
						_randomGenerator.nextBytes(_nonPositiveNumber);
					}).toThrow();

					expect(function () {
						_randomGenerator.nextBytes(
							constants.SECURE_RANDOM_MAX_NUM_BYTES + 1);
					}).toThrow();
				});

			it('throw an exception when generating a random BigInteger by number of bits, using invalid input data',
				function () {
					expect(function () {
						_randomGenerator.nextBigInteger();
					}).toThrow();

					expect(function () {
						_randomGenerator.nextBigInteger(undefined);
					}).toThrow();

					expect(function () {
						_randomGenerator.nextBigInteger(null);
					}).toThrow();

					expect(function () {
						_randomGenerator.nextBigInteger(_nonNumber);
					}).toThrow();

					expect(function () {
						_randomGenerator.nextBigInteger(_nonPositiveNumber);
					}).toThrow();
				});

			it('throw an exception when generating a random BigInteger by number of digits, using invalid input data',
				function () {
					expect(function () {
						_randomGenerator.nextBigIntegerByDigits();
					}).toThrow();

					expect(function () {
						_randomGenerator.nextBigIntegerByDigits(undefined);
					}).toThrow();

					expect(function () {
						_randomGenerator.nextBigIntegerByDigits(null);
					}).toThrow();

					expect(function () {
						_randomGenerator.nextBigIntegerByDigits(_nonNumber);
					}).toThrow();

					expect(function () {
						_randomGenerator.nextBigIntegerByDigits(_nonPositiveNumber);
					}).toThrow();

					expect(function () {
						_randomGenerator.nextBigIntegerByDigits(
							constants.SECURE_RANDOM_MAX_NUM_DIGITS + 1);
					}).toThrow();
				});
		});
	});
});
