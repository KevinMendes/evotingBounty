/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const { expect } = require('chai');

const CommonTestData = require('./data/common-data');
const ValidationTestData = require('./data/validation-data');
const elGamal = require('../../src/elgamal');
const cryptoPolicy = require('../../src/cryptopolicy');

const expectedThrownMessage = 'Expected Q to have a length of 256 for group type ZP_2048_256; Found 2047';

describe('The ElGamal cryptography module should be able to ...', function () {
	let _elGamalService;

	let _group;
	let _anotherGroup;
	let _privateKey;
	let _privateKeyExponents;
	let _privateKeyExponentsFromAnotherGroup;

	let _nonObject;
	let _emptyObject;
	let _nonBoolean;
	let _nonJsonString;
	let _nonArray;
	let _nonObjectArray;
	let _emptyObjectArray;
	let _privateKeyFromAnotherGroup;

	beforeEach(function () {
		const commonTestData = new CommonTestData();
		_group = commonTestData.getGroup();
		_anotherGroup = commonTestData.getLargeGroup();
		_privateKey = commonTestData.getPrivateKey();
		_privateKeyExponents = commonTestData.getPrivateKeyExponents();

		const validationTestData = new ValidationTestData();
		_nonObject = validationTestData.getNonObject();
		_emptyObject = validationTestData.getEmptyObject();
		_nonBoolean = validationTestData.getNonBoolean();
		_nonJsonString = validationTestData.getNonJsonString();
		_nonArray = validationTestData.getNonArray();
		_nonObjectArray = validationTestData.getNonObjectArray();
		_emptyObjectArray = validationTestData.getEmptyObjectArray();
		_privateKeyExponentsFromAnotherGroup =
			validationTestData.getPrivateKeyExponentsFromAnotherGroup();
		_privateKeyFromAnotherGroup =
			validationTestData.getPrivateKeyFromAnotherGroup();

		const policy = cryptoPolicy.newInstance();
		policy.mathematical.groups.type =
			cryptoPolicy.options.mathematical.groups.type.ZP_2048_256;
		_elGamalService = elGamal.newService({policy: policy});
	});

	describe('create an ElGamal cryptography service that should be able to ..', function () {
		it('throw an error when being created, using an invalid secure random service object',
			function () {
				expect(function () {
					elGamal.newService({secureRandomService: null});
				}).to.throw();

				expect(function () {
					elGamal.newService({secureRandomService: _nonObject});
				}).to.throw();

				expect(function () {
					elGamal.newService({secureRandomService: _emptyObject});
				}).to.throw();
			});

		it('throw an error when being created, using an invalid mathematical service object',
			function () {
				expect(function () {
					elGamal.newService({mathematicalService: null});
				}).to.throw();

				expect(function () {
					elGamal.newService({mathematicalService: _nonObject});
				}).to.throw();

				expect(function () {
					elGamal.newService({mathematicalService: _emptyObject});
				}).to.throw();
			});

		it('throw an error when creating a new ElGamalPrivateKey object, using invalid input data',
			function () {
				expect(function () {
					_elGamalService.newPrivateKey(undefined, _privateKeyExponents);
				}).to.throw();

				expect(function () {
					_elGamalService.newPrivateKey(null, _privateKeyExponents);
				}).to.throw();

				expect(function () {
					_elGamalService.newPrivateKey(_nonObject, _privateKeyExponents);
				}).to.throw();

				expect(function () {
					_elGamalService.newPrivateKey(_emptyObject, _privateKeyExponents);
				}).to.throw();

				expect(function () {
					_elGamalService.newPrivateKey(_group);
				}).to.throw();

				expect(function () {
					_elGamalService.newPrivateKey(_group, undefined);
				}).to.throw();

				expect(function () {
					_elGamalService.newPrivateKey(_group, null);
				}).to.throw();

				expect(function () {
					_elGamalService.newPrivateKey(_group, _nonArray);
				}).to.throw();

				expect(function () {
					_elGamalService.newPrivateKey(_group, _nonObjectArray);
				}).to.throw();

				expect(function () {
					_elGamalService.newPrivateKey(_group, _emptyObjectArray);
				}).to.throw();

				expect(function () {
					_elGamalService.newPrivateKey(_anotherGroup, _privateKeyExponentsFromAnotherGroup);
				}).to.throw(expectedThrownMessage);

				expect(function () {
					_elGamalService.newPrivateKey(_nonJsonString);
				}).to.throw();
			});
	});
});
