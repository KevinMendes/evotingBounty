/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, mocha:true, expr:true */
'use strict';

const { assert, expect } = require('chai');

const KeyPair = require('./fixtures/key-pair');
const cryptoPolicy = require('../../src/cryptopolicy');
const asymmetric = require('../../src/asymmetric/index');
const secureRandom = require('../../src/securerandom');
const codec = require('../../src/codec');

describe('The asymmetric cryptography service should be able to ...', function () {
	const DATA_STRING = 'Ox2fUJq1gAbX';

	const DATA_STRING_FROM_JAVA = 'This is a string from Java.';

	let _publicKey;
	let _privateKey;
	let _signer;
	let _sigVerifier;
	let _data;
	let _signature;
	let _policy;
	let _invalidKeyPair;

	beforeEach(function () {
		const keyPair = new KeyPair();
		_publicKey = keyPair.getPublic();
        _privateKey = keyPair.getPrivate();

		const service = asymmetric.newService();
		_signer = service.newSigner().init(_privateKey);
        _sigVerifier = service.newSignatureVerifier().init(_publicKey);

        _data = codec.utf8Encode(DATA_STRING);
        _signature = _signer.sign(_data);

        _policy = cryptoPolicy.newInstance();
        _policy.asymmetric.keyPair.encryption.keyLengthBits = 128;
        _policy.asymmetric.keyPair.encryption.publicExponent = 1;
    });

    beforeEach(function () {
        _policy = cryptoPolicy.newInstance();
    });

    describe('create a signer/signature verifier pair that should be able to ..', function () {
        it('sign some data and verify the signature, using a specified secure random service object',
            function () {
				const service = asymmetric.newService(
					{secureRandomService: secureRandom});
				const signer = service.newSigner().init(_privateKey);
				const sigVerifier = service.newSignatureVerifier().init(_publicKey);

				const signature = signer.sign(_data);

				assert.isTrue(sigVerifier.verify(signature, _data));
            });

        it('sign some data and verify the signature, using RSA and SHA256',
            function () {
				const policy = cryptoPolicy.newInstance();
				policy.asymmetric.signer = {
                    algorithm: 'RSA',
                    hashAlgorithm: 'SHA256',
                    publicExponent: 65537,
                };

				const service = asymmetric.newService({policy: policy});
				const signer = service.newSigner().init(_privateKey);
				const sigVerifier = service.newSignatureVerifier().init(_publicKey);

				const signature = signer.sign(_data);

				assert.isTrue(sigVerifier.verify(signature, _data));
            });

        it('sign some data and verify the signature, using RSA, SHA256 and RSA-PSS padding',
            function () {
				const policy = cryptoPolicy.newInstance();
				policy.asymmetric.signer = {
                    algorithm: 'RSA',
                    hashAlgorithm: 'SHA256',
                    padding: {
                        name: 'PSS',
                        hashAlgorithm: 'SHA256',
                        maskGenerator: {name: 'MGF1', hashAlgorithm: 'SHA256'},
                        saltLengthBytes: 32
                    },
                    publicExponent: 65537,
                };

				const service = asymmetric.newService({policy: policy});
				const signer = service.newSigner().init(_privateKey);
				const sigVerifier = service.newSignatureVerifier().init(_publicKey);

				const signature = signer.sign(_data);

				assert.isTrue(sigVerifier.verify(signature, _data));
				assert.isFalse(sigVerifier.verify(signature, codec.utf8Encode('')));
            });

        it('sign an empty string and verify the signature, using SHA256 with RSA',
            function () {
				const data = codec.utf8Encode('');

				const signature = _signer.sign(data);

				assert.isTrue(_sigVerifier.verify(signature, data));
            });

        it('verify a signature that was generated using Java', function () {
            // Signature generated using Java.
			const signatureB64 = 'KuQPmBRE/JpNQ5mvh1Y1LvW3Jr/ZYlYqOgdbgXLVZp+gjVtXHDgFZzIrC8+S+VdTs9WINjxN8aIZlbiemZ+YvXZ7fDOoJsZJRboyrFN9VGz6sWWVXAv' +
				'28Xj1a2lPNlys8dO3uY0s5R77kDCvBHV4nJh0Hq2Ry+JRjRcrlPsIiDWzm9iTY7V6XzkVGCo58qWyQtloC0s73i8UM6V2nYu1oGDuHqUcvDbstElGAJrn7u4QtP2Kgzd1G' +
				'SkvbSKhsjR2RniiJ7cD278i27NAEPXY+qqmWQ4c+zt3BKG4Aj4EtKa/4AyARmLC78KjZpfiGPTUzQZbMQudAMmKgSTQhx7ONg==';

			// Policy used to generate signature.
			const policy = cryptoPolicy.newInstance();
			policy.asymmetric.signer = {
                algorithm: 'RSA',
                hashAlgorithm: 'SHA256',
                publicExponent: 65537,
            };

			const service = asymmetric.newService({policy: policy});
			const sigVerifier = service.newSignatureVerifier().init(_publicKey);

			// Verify that digital signature was successfully verified.
			assert.isTrue(sigVerifier.verify(
				codec.base64Decode(signatureB64),
				codec.utf8Encode(DATA_STRING_FROM_JAVA)));
        });

        it('verify a signature that was generated using Java and RSA-PSS padding',
            function () {
                // Signature generated in Java.
				const signatureB64 = 'JSYZ0i3Yz3L8J3bUOLOP/Kk83qkhoZ9NqBm9eUpaFoZY8gD42XHJxXyPmDYMnDFMr+J5GypfS9vheYtgn2hag8n3DcG86I80UCNuEZLEhyXpIsN' +
					'4mv3rDb/xjMXlnDLWiK6MTBTDVImKfwpX6x/xw9YtcG9ADACRefH4++H9fhpg8yXUfT9OacOPHgjoW/y4hJmaKVk+IVDgLAKfToNWwVm2xU0SnFryQ9PUJeR8KHttB' +
					'LR+gpZZpn+bQMcEvikw8bzAYGX4yXxGB9UgoIsz6xNyxY6P6UyJSal/JajURLuSwW7JpxjtthbyKS6SUXtH+NEg36ZpMhd8QC6EPzwrlQ==';

				// Policy used to generate signature.
				const policy = cryptoPolicy.newInstance();
				policy.asymmetric.signer = {
                    algorithm: 'RSA',
                    hashAlgorithm: 'SHA256',
                    padding: {
                        name: 'PSS',
                        hashAlgorithm: 'SHA256',
                        maskGenerator: {name: 'MGF1', hashAlgorithm: 'SHA256'},
                        saltLengthBytes: 32
                    },
                    publicExponent: 65537,
                };

				const service = asymmetric.newService({policy: policy});
				const sigVerifier = service.newSignatureVerifier().init(_publicKey);

				// Verify that digital signature was successfully verified.
				assert.isTrue(sigVerifier.verify(
					codec.base64Decode(signatureB64),
					codec.utf8Encode(DATA_STRING_FROM_JAVA)));
            });

        it('throw an error when being created, using the wrong public exponent',
            function () {
                _policy.asymmetric.signer.publicExponent = 'Wrong public exponent';
				const service = asymmetric.newService({policy: _policy});

				expect(function () {
                    service.newSigner();
                }).to.throw();

                expect(function () {
                    service.newSignatureVerifier();
                }).to.throw();
            });

        it('throw an error when being created, using the wrong signing algorithm',
            function () {
                _policy.asymmetric.signer.algorithm =
                    'Wrong digital signature algorithm';
				const service = asymmetric.newService({policy: _policy});

				expect(function () {
                    service.newSigner();
                }).to.throw();

                expect(function () {
                    service.newSignatureVerifier();
                }).to.throw();
            });

        it('throw an error when being created, using the wrong hash algorithm',
            function () {
                _policy.asymmetric.signer.hashAlgorithm =
                    'Wrong signer hash algorithm';
				const service = asymmetric.newService({policy: _policy});

				expect(function () {
                    service.newSigner();
                }).to.throw();

                expect(function () {
                    service.newSignatureVerifier();
                }).to.throw();
            });

        it('throw an error when being created, using the wrong padding name',
            function () {
                _policy.asymmetric.signer.padding.name = 'Wrong signer padding name';
				const service = asymmetric.newService({policy: _policy});

				expect(function () {
                    service.newSigner();
                }).to.throw();

                expect(function () {
                    service.newSignatureVerifier();
                }).to.throw();
            });

        it('throw an error when being created, using the wrong padding hash algorithm',
            function () {
                _policy.asymmetric.signer.padding.hashAlgorithm =
                    'Wrong signer padding hash algorithm';
				const service = asymmetric.newService({policy: _policy});

				expect(function () {
                    service.newSigner();
                }).to.throw();

                expect(function () {
                    service.newSignatureVerifier();
                }).to.throw();
            });

        it('throw an error when being created, using the wrong padding mask generation function',
            function () {
                _policy.asymmetric.signer.padding.maskGenerator.name =
                    'Wrong signer padding mask generation function';
				const service = asymmetric.newService({policy: _policy});

				expect(function () {
                    service.newSigner();
                }).to.throw();

                expect(function () {
                    service.newSignatureVerifier();
                }).to.throw();
            });

        it('throw an error when being created, using the wrong padding mask generation function hash algorithm',
            function () {
                _policy.asymmetric.signer.padding.maskGenerator.hashAlgorithm =
                    'Wrong signer padding mask generation function hash algorithm';
				const service = asymmetric.newService({policy: _policy});

				expect(function () {
                    service.newSigner();
                }).to.throw();

                expect(function () {
                    service.newSignatureVerifier();
                }).to.throw();
            });

        it('throw an error when signing data, using a private key with the wrong public exponent',
            function () {
                expect(function () {
                    _signer.sign(_invalidKeyPair.getPrivateKey().getPem(), _data);
                }).to.throw();
            });

        it('throw an error when verifying a signature, using a public key with the wrong public exponent',
            function () {
                expect(function () {
                    _sigVerifier.verify(
                        _signature, _invalidKeyPair.getPublicKey().getPem(), _data);
                }).to.throw();
            });
    });
});
