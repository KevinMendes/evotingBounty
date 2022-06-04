/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, jasmine:true, expr:true */
'use strict';

const { expect } = require('chai');

const symmetric = require('../../src/symmetric');
const codec = require('../../src/codec');

describe('The symmetric cryptography module should be able to ...', function () {
	const _data = codec.utf8Encode('anyString');

	const service = symmetric.newService();
	const keyGenerator = service.newKeyGenerator();
	const _cipher = service.newCipher().init(keyGenerator.nextEncryptionKey());
	const _macHandler = service.newMacHandler().init(keyGenerator.nextMacKey());
	const _mac = _macHandler.generate(_data);

	const _nonObject = 999;
	const _emptyObject = {};
	const _nonUint8Array = [];

	describe(
		'create a symmetric cryptography service that should be able to ..',
		function () {
			it('throw an error when being created, using an invalid cryptographic policy',
				function () {
					expect(function () {
						symmetric.newService({policy: null});
					}).to.throw();

					expect(function () {
						symmetric.newService({policy: _nonObject});
					}).to.throw();

					expect(function () {
						symmetric.newService({policy: _emptyObject});
					}).to.throw();
				});

			it('throw an error when being created, using an invalid secure random service object',
				function () {
					expect(function () {
						symmetric.newService({secureRandomService: null});
					}).to.throw();

					expect(function () {
						symmetric.newService({secureRandomService: _nonObject});
					}).to.throw();

					expect(function () {
						symmetric.newService({secureRandomService: _emptyObject});
					}).to.throw();
				});

			describe(
				'create a symmetric cipher that should be able to ...', function () {
					it('throw an error when being initialized, using invalid input data',
						function () {
							expect(function () {
								_cipher.init();
							}).to.throw();

							expect(function () {
								_cipher.init(undefined);
							}).to.throw();

							expect(function () {
								_cipher.init(null);
							}).to.throw();

							expect(function () {
								_cipher.init(_nonUint8Array);
							}).to.throw();
						});

					it('throw an error when encrypting, using invalid input data',
						function () {
							expect(function () {
								_cipher.encrypt();
							}).to.throw();

							expect(function () {
								_cipher.encrypt(undefined);
							}).to.throw();

							expect(function () {
								_cipher.encrypt(null);
							}).to.throw();

							expect(function () {
								_cipher.encrypt(_nonUint8Array);
							}).to.throw();
						});

					it('throw an error when decrypting, using invalid input data',
						function () {
							expect(function () {
								_cipher.decrypt();
							}).to.throw();

							expect(function () {
								_cipher.decrypt(undefined);
							}).to.throw();

							expect(function () {
								_cipher.decrypt(null);
							}).to.throw();

							expect(function () {
								_cipher.decrypt(_nonUint8Array);
							}).to.throw();
						});
				});

			describe('create a MAC handler that should be able to ...', function () {
				it('throw an error when being initialized, using invalid input data',
					function () {
						expect(function () {
							_macHandler.init();
						}).to.throw();

						expect(function () {
							_macHandler.init(undefined);
						}).to.throw();

						expect(function () {
							_macHandler.init(null);
						}).to.throw();

						expect(function () {
							_macHandler.init(_nonUint8Array);
						}).to.throw();
					});

				it('throw an error when generating a MAC, using invalid input data',
					function () {
						expect(function () {
							_macHandler.generate(null);
						}).to.throw();

						expect(function () {
							_macHandler.generate(_nonUint8Array);
						}).to.throw();
					});

				it('throw an error when verifying a MAC, using an invalid MAC',
					function () {
						expect(function () {
							_macHandler.verify();
						}).to.throw();

						expect(function () {
							_macHandler.verify(undefined, _data);
						}).to.throw();

						expect(function () {
							_macHandler.verify(null, _data);
						}).to.throw();

						expect(function () {
							_macHandler.verify(_nonUint8Array, _data);
						}).to.throw();
					});

				it('throw an error when verifying a MAC, using an invalid verification data',
					function () {
						expect(function () {
							_macHandler.verify(_mac, null);
						}).to.throw();

						expect(function () {
							_macHandler.verify(_mac, _nonUint8Array);
						}).to.throw();
					});

				it('throw an error when being updated, using invalid input data',
					function () {
						expect(function () {
							_macHandler.update();
						}).to.throw();

						expect(function () {
							_macHandler.update(undefined);
						}).to.throw();

						expect(function () {
							_macHandler.update(null);
						}).to.throw();

						expect(function () {
							_macHandler.update(_nonUint8Array);
						}).to.throw();
					});
			});
		});
});
