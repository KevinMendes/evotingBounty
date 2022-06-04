/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, jasmine:true, expr:true */
'use strict';

const { expect } = require('chai');

const cryptoPolicy = require('../../src/cryptopolicy');
const validator = require('../../src/input-validator');

describe('The cryptographic policy module should be able to ...', function () {
	describe(
		'create a cryptographic policy instance that should be able to ..',
		function () {
			it('provide access to policy options', function () {
				expect(function () {
					// It's `strict`, so it will fail if any of the values is not
					// present in the options object.
					const policy = cryptoPolicy.newInstance();
					const value = policy.asymmetric.keyPair.encryption;
					expect(value).to.throw();
				}).not.to.throw;
			});

			it('restore default settings', function () {
				// Load the policy
				let policy = cryptoPolicy.newInstance();
				// Check that the policy has default values.
				expect(policy.asymmetric.keyPair.encryption.algorithm).to.equal('RSA');
				// Change a value.
				policy.asymmetric.keyPair.encryption.algorithm = 'RSE';
				// Check that the value has changed.
				expect(policy.asymmetric.keyPair.encryption.algorithm).to.equal('RSE');
				// Get a new default policy.
				policy = cryptoPolicy.newInstance();
				// Check that the policy has default values again.
				expect(policy.asymmetric.keyPair.encryption.algorithm).to.equal('RSA');
			});

			it('provide access to policy options', function () {
				// Load the policy
				const policy = cryptoPolicy.newInstance();

				expect(function () {
					policy.asymmetric.keyPair.encryption.algorithm =
						cryptoPolicy.options.asymmetric.keyPair.encryption.algorithm
							.RSA;
				}).not.to.throw();
			});

			it('allow overriding of the default policy', function () {
				const otherValue = 'XYZ';
				// Load the policy
				const policy = cryptoPolicy.newInstance({
					asymmetric: {cipher: {algorithm: {name: otherValue}}},
					newOption: otherValue
				});

				expect(policy.asymmetric.cipher.algorithm.name).to.equal(otherValue);

			    expect(policy.newOption).to.throw;
			    expect(policy.newOption).to.equal(otherValue);
			});

			it('throw an error when being created with an invalid policy',
			    function () {
			        expect(function () {
			            cryptoPolicy.newInstance(null);
			        }).to.throw();

			        expect(function () {
			            cryptoPolicy.newInstance('non object');
			        }).to.throw();

			        expect(function () {
			            cryptoPolicy.newInstance({});
			        }).to.throw();
			    });

			it('throw an error when validator receives an invalid policy',
				function () {
					const label = 'Invalid Cryptopolicy';
					expect(function () {
						validator.checkIsObjectWithProperties(undefined, label);
					}).to.throw('Invalid Cryptopolicy is undefined.');

					expect(function () {
						validator.checkIsObjectWithProperties(null, label);
					}).to.throw('Invalid Cryptopolicy is null.');

					expect(function () {
						validator.checkIsObjectWithProperties(12, label);
					}).to.throw('Invalid Cryptopolicy is not an object.');

					expect(function () {
						validator.checkIsObjectWithProperties({}, label);
					}).to.throw('Invalid Cryptopolicy does not have any properties.');
				});
		});
});
