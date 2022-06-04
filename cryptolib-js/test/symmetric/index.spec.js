/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, jasmine:true, expr:true */
'use strict';

const { assert, expect } = require('chai');

const symmetric = require('../../src/symmetric');
const validator = require('../../src/input-validator');
const cryptoPolicy = require('../../src/cryptopolicy');
const secureRandom = require('../../src/securerandom');
const bitwise = require('../../src/bitwise');
const codec = require('../../src/codec');

describe('The symmetric cryptography module should be able to ...', function () {
	const DATA = 'anystring';
	const OTHER_DATA_1 = DATA + 'a';
	const OTHER_DATA_2 = DATA + 'b';

	let _data;
	let _otherData1;
	let _otherData2;
	let _dataParts;
	let _service;
	let _policy = cryptoPolicy.newInstance();
	let _keyGenerator;
	let _encryptionKey;
	let _macKey;
	let _cipher;
	let _macHandler;
	let _encryptedData;
	let _mac;

	beforeEach(function () {
		_data = codec.utf8Encode(DATA);
		_otherData1 = codec.utf8Encode(OTHER_DATA_1);
		_otherData2 = codec.utf8Encode(OTHER_DATA_2);
		_dataParts = [_data, _otherData1, _otherData2];
		_service = symmetric.newService();
		_policy = cryptoPolicy.newInstance();
		_keyGenerator = _service.newKeyGenerator();
		_encryptionKey = _keyGenerator.nextEncryptionKey();
		_macKey = _keyGenerator.nextMacKey();
		_cipher = _service.newCipher().init(_encryptionKey);
		_macHandler = _service.newMacHandler().init(_macKey);
		_encryptedData = _cipher.encrypt(_data);
		_mac = _macHandler.generate(_data);
	});

	describe('create a symmetric cryptography service that should be able to ..', function () {
		describe('create a secret key generator that should be able to', function () {
			it('generate a secret key for encryption', function () {
				expect(_encryptionKey.length)
					.to.equal(_policy.symmetric.secretKey.encryption.lengthBytes);
			});

			it('generate a secret key for mac generation', function () {
				expect(_macKey.length)
					.to.equal(_policy.symmetric.secretKey.mac.lengthBytes);
			});

			it('generate secret keys, using a specified cryptographic policy',
				function () {
					const policy = cryptoPolicy.newInstance();
					policy.symmetric.secretKey.encryption.lengthBytes =
						cryptoPolicy.options.symmetric.secretKey.encryption.lengthBytes
							.KL_32;
					policy.symmetric.secretKey.mac.lengthBytes =
						cryptoPolicy.options.symmetric.secretKey.mac.lengthBytes.KL_64;
					const service = symmetric.newService({policy: policy});
					const keyGenerator = service.newKeyGenerator();

					const encryptionKey = keyGenerator.nextEncryptionKey();
					expect(encryptionKey.length)
						.to.equal(policy.symmetric.secretKey.encryption.lengthBytes);

					const macKey = keyGenerator.nextMacKey();
					expect(macKey.length)
						.to.equal(policy.symmetric.secretKey.mac.lengthBytes);
				});

			it('generate secret keys, using a specified secure random service object',
				function () {
					const secureRandomService = secureRandom;
					const service =
						symmetric.newService({secureRandomService: secureRandomService});
					const keyGenerator = service.newKeyGenerator();

					const encryptionKey = keyGenerator.nextEncryptionKey();
					expect(encryptionKey.length)
						.to.equal(_policy.symmetric.secretKey.encryption.lengthBytes);

					const macKey = keyGenerator.nextMacKey();
					expect(macKey.length)
						.to.equal(_policy.symmetric.secretKey.mac.lengthBytes);
				});
		});

		describe('create a symmetric cipher that should be able to ..', function () {
			it('encrypt and decrypt some data', function () {
				const encryptedData = _cipher.encrypt(_data);
				const decryptedData = _cipher.decrypt(encryptedData);

				expect(decryptedData).to.deep.equal(_data);
			});

			it('encrypt and decrypt some data of type string', function () {
				const encryptedData = _cipher.encrypt(DATA);
				const decryptedData = codec.utf8Decode(_cipher.decrypt(encryptedData));

				expect(decryptedData).to.deep.equal(DATA);
			});

			it('encrypt and decrypt some data, using a specified cryptographic policy',
				function () {
					const policy = cryptoPolicy.newInstance();
					policy.symmetric.secretKey.encryption.lengthBytes =
						cryptoPolicy.options.symmetric.secretKey.encryption.lengthBytes
							.KL_32;
					policy.symmetric.cipher.algorithm.keyLengthBytes =
						cryptoPolicy.options.symmetric.cipher.algorithm.AES_GCM
							.keyLengthBytes.KL_32;
					const service = symmetric.newService({policy: policy});

					const key = service.newKeyGenerator().nextEncryptionKey();
					const cipher = service.newCipher().init(key);

					const encryptedData = cipher.encrypt(_data);
					const decryptedData = cipher.decrypt(encryptedData);

					expect(decryptedData).to.deep.equal(_data);
				});

			it('encrypt and decrypt some data, using a specified secure random service object',
				function () {
					const secureRandomService = secureRandom;
					const service =
						symmetric.newService({secureRandomService: secureRandomService});

					const keyGenerator = service.newKeyGenerator();
					const key = keyGenerator.nextEncryptionKey();
					const cipher = service.newCipher().init(key);

					const encryptedData = cipher.encrypt(_data);
					const decryptedData = cipher.decrypt(encryptedData);

					expect(decryptedData).to.deep.equal(_data);
				});

			it('encrypt and decrypt data that contains characters and spaces',
				function () {
					const data = codec.utf8Encode('text with some spaces');

					const encryptedData = _cipher.encrypt(data);
					const decryptedData = _cipher.decrypt(encryptedData);

					expect(decryptedData).to.deep.equal(data);
				});

			it('encrypt and decrypt data that only contains spaces', function () {
				const data = codec.utf8Encode('    ');

				const encryptedData = _cipher.encrypt(data);
				const decryptedData = _cipher.decrypt(encryptedData);

				expect(decryptedData).to.deep.equal(data);
			});

			it('decrypt some data that was encrypted with the AES-GCM algorithm, using Java.',
				function () {
					const dataJava = codec.utf8Encode('mUm9EQC8mrDw');
					const encryptionKeyJava =
						codec.base64Decode('v3VagUVMuZJ+b+uFSjqGTw==');
					const initVectorJava = codec.base64Decode('LGF26O6hR2fgUsHd');
					const encryptedDataJava =
						codec.base64Decode('Cvpz8/L+mTv2eFvYpxNoxt+/KJwtAOzG7hl9Dw==');

					const initVectorAndEncryptedData =
						bitwise.concatenate(initVectorJava, encryptedDataJava);

					const cipher = _service.newCipher().init(encryptionKeyJava);
					const decryptedDataJava = cipher.decrypt(initVectorAndEncryptedData);

					expect(decryptedDataJava).to.deep.equal(dataJava);
				});

			it('throw an error when initializing a cipher, using a secret key with a length that differs from that of the cipher',
				function () {
					const policy = cryptoPolicy.newInstance();
					policy.symmetric.secretKey.encryption.lengthBytes =
						cryptoPolicy.options.symmetric.secretKey.encryption.lengthBytes
							.KL_16;
					policy.symmetric.cipher.algorithm.keyLengthBytes =
						cryptoPolicy.options.symmetric.cipher.algorithm.AES_GCM
							.keyLengthBytes.KL_32;
					const service = symmetric.newService({policy: policy});
					const key = service.newKeyGenerator().nextEncryptionKey();
					const cipher = service.newCipher();

					expect(function () {
						cipher.init(key);
					}).to.throw;
				});

			it('throw an error when encrypting some data before the cipher has been initialized with a secret key',
				function () {
					const cipher = _service.newCipher();

					expect(function () {
						cipher.encrypt(_data);
					}).to.throw;
				});

			it('throw an error when decrypting some encrypted data before the cipher has been initialized with a secret key',
				function () {
					const cipher = _service.newCipher();

					expect(function () {
						cipher.decrypt(_encryptedData);
					}).to.throw;
				});
		});

		describe('create a MAC handler that should be able to ..', function () {
			it('generate and verify a MAC', function () {
				const mac = _macHandler.generate(_data);
				const verified = _macHandler.verify(mac, _data);

				assert.isTrue(verified);
			});

			it('generate a MAC from multiple data parts', function () {
				for (let i = 0; i < _dataParts.length; i++) {
					_macHandler.update(_dataParts[i]);
				}
				let mac = _macHandler.generate();

				let verified = _macHandler.verify(
					mac, bitwise.concatenate(_data, _otherData1, _otherData2));
				assert.isTrue(verified);

				for (let j = 0; j < (_dataParts.length - 1); j++) {
					_macHandler.update(_dataParts[j]);
				}
				mac = _macHandler.generate(_otherData2);

				verified = _macHandler.verify(
					mac, bitwise.concatenate(_data, _otherData1, _otherData2));
				assert.isTrue(verified);
			});

			it('generate a MAC from multiple data parts, using method chaining',
				function () {
					let mac = _macHandler.update(_data)
						.update(_otherData1)
						.update(_otherData2)
						.generate();

					let verified = _macHandler.verify(
						mac, bitwise.concatenate(_data, _otherData1, _otherData2));
					assert.isTrue(verified);

					mac = _macHandler.update(_data)
						.update(_otherData1)
						.generate(_otherData2);

					verified = _macHandler.verify(
						mac, bitwise.concatenate(_data, _otherData1, _otherData2));
					assert.isTrue(verified);
				});

			it('verify a MAC generated from multiple data parts', function () {
				const mac = _macHandler.generate(
					bitwise.concatenate(_data, _otherData1, _otherData2));

				for (let i = 0; i < _dataParts.length; i++) {
					_macHandler.update(_dataParts[i]);
				}
				let verified = _macHandler.verify(mac);
				assert.isTrue(verified);

				for (let j = 0; j < (_dataParts.length - 1); j++) {
					_macHandler.update(_dataParts[j]);
				}
				verified = _macHandler.verify(mac, _otherData2);
				assert.isTrue(verified);
			});

			it('verify a MAC generated from multiple data parts, using method chaining',
				function () {
					const mac = _macHandler.generate(
						bitwise.concatenate(_data, _otherData1, _otherData2));

					let verified = _macHandler.update(_data)
						.update(_otherData1)
						.update(_otherData2)
						.verify(mac);
					assert.isTrue(verified);

					verified = _macHandler.update(_data)
						.update(_otherData1)
						.verify(mac, _otherData2);
					assert.isTrue(verified);
				});

			it('generate the expected MAC when using data of type string',
				function () {
					const mac1 = _macHandler.update(_data)
						.update(_otherData1)
						.generate(_otherData2);

					const mac2 = _macHandler.update(DATA)
						.update(OTHER_DATA_1)
						.generate(OTHER_DATA_2);

					expect(mac2).to.deep.equal(mac1);
				});

			it('verify a MAC when using data of type string', function () {
				const mac =
					_macHandler.update(_data).update(_otherData1).generate(_otherData2);

				const verified = _macHandler.update(DATA)
					.update(OTHER_DATA_1)
					.verify(mac, OTHER_DATA_2);

				assert.isTrue(verified);
			});

			it('be automatically reinitialized after MAC generation', function () {
				const mac1 = _macHandler.generate(_data);

				const mac2 = _macHandler.generate(_data);

				expect(mac1).to.deep.equal(mac2);
			});

			it('generate a MAC, using a specified cryptographic policy', function () {
				const policy = cryptoPolicy.newInstance();
				policy.symmetric.mac.hashAlgorithm =
					cryptoPolicy.options.symmetric.mac.hashAlgorithm.SHA512_224;
				const service = symmetric.newService({policy: policy});

				const key = service.newKeyGenerator().nextMacKey();
				const MacHandler = service.newMacHandler().init(key);

				const mac = MacHandler.generate(_data);
				const verified = MacHandler.verify(mac, _data);

				assert.isTrue(verified);
				expect(mac.length).to.equal(28);
			});

			it('generate a MAC, using a specified secure random service object',
				function () {
					const secureRandomService = secureRandom;
					const service =
						symmetric.newService({secureRandomService: secureRandomService});

					const key = service.newKeyGenerator().nextMacKey();
					const MacHandler = service.newMacHandler().init(key);

					const mac = MacHandler.generate(_data);

					const verified = MacHandler.verify(mac, _data);

					assert.isTrue(verified);
				});

			it('throw an error when updating a MAC before the MAC handler has been initialized with a secret key',
				function () {
					const macHandler = _service.newMacHandler();

					expect(function () {
						macHandler.update(_data);
					}).to.throw;
				});

			it('throw an error when generating a MAC before the MAC handler has been initialized with a secret key',
				function () {
					const macHandler = _service.newMacHandler();

					assert.throws(function () {
						macHandler.generate(_data);
					})
				});

			it('throw an error when verifying a MAC before the MAC handler has been initialized with a secret key',
				function () {
					const macHandler = _service.newMacHandler();

					assert.throws(function () {
						macHandler.verify(_mac, _data);
					});
				});

			it('throw an error when generating a MAC without either providing data or having previously updated the MAC handler with some data',
				function () {
					const macHandler = _service.newMacHandler().init(_macKey);

					assert.throws(function () {
						macHandler.generate();
					});
				});

			it('throw an error when verifynig a MAC without either providing data or having previously updated the MAC handler with some data',
				function () {
					const macHandler = _service.newMacHandler().init(_macKey);

					assert.throws(function () {
						macHandler.verify(_mac);
					});
				});
		});

		it('throw an error when initializing a cipher, using an algorithm that is not supported by the cryptographic policy',
			function () {
				const policy = cryptoPolicy.newInstance();
				policy.symmetric.secretKey.encryption.lengthBytes =
					cryptoPolicy.options.symmetric.secretKey.encryption.lengthBytes
						.KL_16;
				policy.symmetric.cipher.algorithm.name = 'Wrong algorithm';
				const service = symmetric.newService({policy: policy});
				const key = service.newKeyGenerator().nextEncryptionKey();
				const cipher = service.newCipher();

				assert.throws(function () {
					cipher.init(key);
				});
			});

		it('throw an error when validator receives invalid values',
			function () {
				const label = 'Invalid value';
				expect(function () {
					validator.checkIsInstanceOf([], Uint8Array, 'Uint8Array', label);
				}).to.throw('Invalid value is not an instance of Object Uint8Array');

				expect(function () {
					validator.checkIsObjectWithProperties(undefined, label);
				}).to.throw('Invalid value is undefined.');

				expect(function () {
					validator.checkIsObjectWithProperties(null, label);
				}).to.throw('Invalid value is null.');

				expect(function () {
					validator.checkIsObjectWithProperties(12, label);
				}).to.throw('Invalid value is not an object.');

				expect(function () {
					validator.checkIsObjectWithProperties({}, label);
				}).to.throw('Invalid value does not have any properties.');
			});
	});
});
