/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, jasmine:true */
'use strict';

const pbkdf = require('../../src/pbkdf');
const codec = require('../../src/codec');

const { expect } = require('chai');

describe('The PBKDF module should be able to ...', function () {
	const PASSWORD = 'Test';
	const SALT = 'TestTestTestTestTestTestTestTest';

	let _salt;
	let _deriver;
	const _nonObject = 999;
	const _emptyObject = {};
	const _nonString = 999;

	beforeEach(function () {
		_salt = codec.utf8Encode(SALT);
		_deriver = pbkdf.newService().newDeriver();
	});

	describe('create a PBKDF service that should be able to ..', function () {
		it('throw an error when being created, using an invalid cryptographic policy',
			function () {
				expect(function () {
					pbkdf.newService({policy: null});
				}).to.throw();

				expect(function () {
					pbkdf.newService({policy: _nonObject});
				}).to.throw();

				expect(function () {
					pbkdf.newService({policy: _emptyObject});
				}).to.throw();
			});

		describe('create a PBKDF deriver that should be able to', function () {
			it('throw an error when deriving a key, using an invalid password',
				function () {
					expect(function () {
						_deriver.derive(undefined, _salt);
					}).to.throw();

					expect(function () {
						_deriver.derive(null, _salt);
					}).to.throw();

					expect(function () {
						_deriver.derive(_nonString, _salt);
					}).to.throw();

					expect(function () {
						_deriver.derive('lessThanMinLen', _salt);
					}).to.throw('The password length 14 is less than the minimum allowed password length 16');

					expect(function () {
						const moreThanMaxLen = 'a'.repeat(1001);

						_deriver.derive(moreThanMaxLen, _salt);
					}).to.throw('The password length 1001 is more than the maximum allowed password length 1000');
				});

			it('throw an error when deriving a key, using invalid salt', function () {
				expect(function () {
					_deriver.derive(PASSWORD);
				}).to.throw();

				expect(function () {
					_deriver.derive(PASSWORD, undefined);
				}).to.throw();

				expect(function () {
					_deriver.derive(PASSWORD, null);
				}).to.throw();

				expect(function () {
					_deriver.derive(PASSWORD, '');
				}).to.throw();

				expect(function () {
					_deriver.derive(PASSWORD, _nonString);
				}).to.throw();
			});
		});
	});
});
