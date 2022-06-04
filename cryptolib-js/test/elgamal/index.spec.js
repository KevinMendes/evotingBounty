/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, mocha: true, expr:true */
'use strict';

const { assert, expect } = require('chai');

const CommonTestData = require('./data/common-data');
const elGamal = require('../../src/elgamal');
const mathematical = require('../../src/mathematical');
const secureRandom = require('../../src/securerandom');
const cryptoPolicy = require('../../src/cryptopolicy');

describe('The ElGamal cryptography module should be able to ...', function () {
	let _elGamalSmallService;
	let _secureRandomService;
	let _mathService;
	let _mathRandomGenerator;
	let _group;
	let _privateKey;
	let _privateKeyExponents;
	let _smallPolicy;

	beforeEach(function () {
		_smallPolicy = cryptoPolicy.newInstance();
		_smallPolicy.mathematical.groups.type =
			cryptoPolicy.options.mathematical.groups.type.ZP_2048_256;
		_elGamalSmallService = elGamal.newService({policy: _smallPolicy});
		_secureRandomService = secureRandom;
		_mathService =
			mathematical.newService({policy: _smallPolicy, secureRandomService: _secureRandomService});
		_mathRandomGenerator = _mathService.newRandomGenerator();

		const testData = new CommonTestData();
		_group = testData.getGroup();
		_privateKey = testData.getPrivateKey();
		_privateKeyExponents = testData.getPrivateKeyExponents();
	});

	describe('create an ElGamal cryptography service that should be able to ..', function () {

		it('serialize and deserialize an ElGamal private key', function () {
			const privateKeyJson = _privateKey.toJson();

			const privateKeyFromJson = _elGamalSmallService.newPrivateKey(privateKeyJson);

			validatePrivateKey(privateKeyFromJson, _group);
		});
	});

	function validatePrivateKey(privateKey, group) {
		const exponents = privateKey.exponents;
		const numExponents = exponents.length;
		expect(numExponents).to.equal(_privateKeyExponents.length);
		for (let i = 0; i < numExponents; i++) {
			expect(exponents[i].value.toString())
				.to.equal(_privateKeyExponents[i].value.toString());
		}

		assert.isTrue(privateKey.group.equals(group));
	}
});
