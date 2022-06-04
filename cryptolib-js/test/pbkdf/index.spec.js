/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, jasmine:true, expr:true */
'use strict';

const { assert, expect } = require('chai');

const cryptoPolicy = require('../../src/cryptopolicy');
const pbkdf = require('../../src/pbkdf');
const codec = require('../../src/codec');

describe('The PBKDF module should be able to ...', function () {
	const PASSWORD = 'Test1234567890AB';
	const SALT = 'TestTestTestTestTestTestTestTest';

	let _salt;
	let _tooSmallsalt;
	let _deriver;
	let _key;
	let _policy;

	beforeEach(function () {
		_salt = codec.utf8Encode(SALT);
		_tooSmallsalt = codec.utf8Encode('1');
		_deriver = pbkdf.newService().newDeriver();
		_key = _deriver.derive(PASSWORD, _salt);
	});

	beforeEach(function () {
		_policy = cryptoPolicy.newInstance();
	});

	describe('create a PBKDF service that should be able to ..', function () {
		describe('create a PBKDF deriver that should be able to', function () {
			it('derive a key of the expected length, when using the default cryptographic policy.',
				function () {
					const key = _deriver.derive(PASSWORD, _salt);
					assert.exists(key);

					expect(key.length)
						.to.equal(_policy.primitives.keyDerivation.pbkdf.keyLengthBytes);
				});

			it('derive a key of the expected length, when using a specified cryptographic policy.',
				function () {
					_policy.primitives.keyDerivation.pbkdf.keyLengthBytes =
						cryptoPolicy.options.primitives.keyDerivation.pbkdf
							.keyLengthBytes.KL_32;
					const pbkdfService = pbkdf.newService({policy: _policy});

					const key = pbkdfService.newDeriver().derive(PASSWORD, _salt);
					assert.exists(key);

					expect(key.length)
						.to.equal(cryptoPolicy.options.primitives.keyDerivation.pbkdf
						.keyLengthBytes.KL_32);
				});

			it('derive the same key every time, for a given cryptographic policy.',
				function () {
					const key = _deriver.derive(PASSWORD, _salt);

					assert.exists(key);
					expect(key).to.deep.equal(_key);
				});

			it('derive the expected key if salt is provided as string instead of bytes',
				function () {
					const key = _deriver.derive(PASSWORD, SALT);

					assert.exists(key);
					expect(key).to.deep.equal(_key);
				});

			it('derive different keys when different numbers of iterations are specified.',
				function () {
					_policy.primitives.keyDerivation.pbkdf.numIterations =
						cryptoPolicy.options.primitives.keyDerivation.pbkdf.numIterations
							.I_1;
					const pbkdfService = pbkdf.newService({policy: _policy});

					const key = pbkdfService.newDeriver().derive(PASSWORD, _salt);

					assert.exists(key);
					expect(key).to.not.deep.equal(_key);
				});

			it('derive different keys when different passwords are used.',
				function () {
					const key = _deriver.derive(PASSWORD + 'a', _salt);

					assert.exists(key);
					expect(key).to.not.deep.equal(_key);
				});

			it('derive different keys when different salts are used.', function () {
				const key = _deriver.derive(PASSWORD, codec.utf8Encode(SALT + 'a'));

				assert.exists(key);
				expect(key).to.not.deep.equal(_key);
			});

			it('throw an error when a salt length less than the minimum value specified in the cryptographic policy is used',
				function () {
					expect(function () {
						_deriver.derive(PASSWORD, _tooSmallsalt);
					}).to.throw();
				});
		});
	});
});
