/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, mocha:true, expr:true */
/* eslint-disable no-unused-expressions */
'use strict';

const { assert, expect } = require('chai');

const KeyPair = require('./fixtures/key-pair');
const InvalidEncodingKeyPair =
    require('./fixtures/invalid/invalid-encoding-key-pair.js');
const InvalidExponentKeyPair =
    require('./fixtures/invalid/invalid-exponent-key-pair.js');
const asymmetric = require('../../src/asymmetric/index');
const constants = require('../../src/asymmetric/constants');
const bitwise = require('../../src/bitwise');
const codec = require('../../src/codec');

const DATA_STRING = 'anystring';
const OTHER_DATA_1_STRING = DATA_STRING + 'a';
const OTHER_DATA_2_STRING = DATA_STRING + 'b';

let _publicKey;
let _privateKey;
let _service;
let _signer;
let _sigVerifier;
let _data;
let _otherData1;
let _otherData2;
let _dataParts;
let _signature;
let _invalidEncodingKeyPair;
let _invalidExponentKeyPair;

function removeNewLineChars(str) {
    return str.replace(/(\r\n|\n|\r)/gm, '');
}

function beforeEachHook() {
    const keyPair = new KeyPair();
    _publicKey = keyPair.getPublic();
    _privateKey = keyPair.getPrivate();

    _service = asymmetric.newService();
    _signer = _service.newSigner().init(_privateKey);
    _signer.init(_privateKey);
    _sigVerifier = _service.newSignatureVerifier().init(_publicKey);
    _sigVerifier.init(_publicKey);

    _data = codec.utf8Encode(DATA_STRING);
    _otherData1 = codec.utf8Encode(OTHER_DATA_1_STRING);
    _otherData2 = codec.utf8Encode(OTHER_DATA_2_STRING);
    _dataParts = [_data, _otherData1, _otherData2];

    _signature = _signer.sign(_data);

    _invalidEncodingKeyPair = new InvalidEncodingKeyPair();
    _invalidExponentKeyPair = new InvalidExponentKeyPair();
}

const describeText1 = 'The asymmetric cryptography module should be able to ...';
const describeText2 = 'create an asymmetric cryptography service that should be able to ..';

describe(describeText1, function () {
    beforeEach(function () {
        beforeEachHook();
    });

    describe(describeText2, function () {
        it('create a new RsaPublicKey object', function () {
            const rsaPublicKey1 = _service.newRsaPublicKey({pem: _publicKey});
            const rsaPublicKey2 = _service.newRsaPublicKey({n: rsaPublicKey1.n, e: rsaPublicKey1.e});

            expect(rsaPublicKey2.n).to.equal(rsaPublicKey1.n);
            expect(rsaPublicKey2.e).to.equal(rsaPublicKey1.e);

            const rsaPublicKey1Pem = rsaPublicKey1.toPem();
            let offset = rsaPublicKey1Pem.indexOf('\n') + 1;
            expect(rsaPublicKey1Pem.indexOf('\n', offset)).to.equal(offset + constants.PEM_LINE_LENGTH + 1);

            const rsaPublicKey2Pem = rsaPublicKey1.toPem();
            offset = rsaPublicKey2Pem.indexOf('\n') + 1;
            expect(rsaPublicKey2Pem.indexOf('\n', offset)).to.equal(offset + constants.PEM_LINE_LENGTH + 1);

            expect(removeNewLineChars(rsaPublicKey1Pem)).to.equal(removeNewLineChars(_publicKey));
            expect(removeNewLineChars(rsaPublicKey2Pem)).to.equal(removeNewLineChars(_publicKey));
        });

        it('create a new RsaPrivateKey object', function () {
            const rsaPrivateKey1 = _service.newRsaPrivateKey({pem: _privateKey});
            const params = {
                n: rsaPrivateKey1.n,
                e: rsaPrivateKey1.e,
                d: rsaPrivateKey1.d,
                p: rsaPrivateKey1.p,
                q: rsaPrivateKey1.q,
                dP: rsaPrivateKey1.dP,
                dQ: rsaPrivateKey1.dQ,
                qInv: rsaPrivateKey1.qInv
            };

            const rsaPrivateKey2 = _service.newRsaPrivateKey(params);

            expect(rsaPrivateKey2.n).to.equal(rsaPrivateKey1.n);
            expect(rsaPrivateKey2.e).to.equal(rsaPrivateKey1.e);
            expect(rsaPrivateKey2.d).to.equal(rsaPrivateKey1.d);
            expect(rsaPrivateKey2.p).to.equal(rsaPrivateKey1.p);
            expect(rsaPrivateKey2.q).to.equal(rsaPrivateKey1.q);
            expect(rsaPrivateKey2.dP).to.equal(rsaPrivateKey1.dP);
            expect(rsaPrivateKey2.dQ).to.equal(rsaPrivateKey1.dQ);
            expect(rsaPrivateKey2.qInv).to.equal(rsaPrivateKey1.qInv);

            const rsaPrivateKey1Pem = rsaPrivateKey1.toPem();
            let offset = rsaPrivateKey1Pem.indexOf('\n') + 1;
            expect(rsaPrivateKey1Pem.indexOf('\n', offset)).to.equal(offset + constants.PEM_LINE_LENGTH + 1);

            const rsaPrivateKey2Pem = rsaPrivateKey2.toPem();
            offset = rsaPrivateKey2Pem.indexOf('\n') + 1;
            expect(rsaPrivateKey2Pem.indexOf('\n', offset)).to.equal(offset + constants.PEM_LINE_LENGTH + 1);

            expect(removeNewLineChars(rsaPrivateKey1Pem)).to.equal(removeNewLineChars(_privateKey));
            expect(removeNewLineChars(rsaPrivateKey2Pem)).to.equal(removeNewLineChars(_privateKey));
        });

        it('create a new KeyPair object', function () {
            const keyPair = _service.newKeyPair(_publicKey, _privateKey);

            expect(removeNewLineChars(keyPair.publicKey)).to.equal(removeNewLineChars(_publicKey));
            expect(removeNewLineChars(keyPair.privateKey)).to.equal(removeNewLineChars(_privateKey));
        });
    });
});


describe(describeText1, function () {
    beforeEach(function () {
        beforeEachHook();
    });

    describe(describeText2, function () {
        describe('create a signer/signature verifier pair that should be able to ..', function () {
            it('sign some data and verify the signature', function () {
                const signature = _signer.sign(_data);

                assert.isTrue(_sigVerifier.verify(signature, _data));
            });

            it('sign some data consisting of multiple parts', function () {
                // Call sign with no arguments.
                for (let i = 0; i < _dataParts.length; i++) {
                    _signer.update(_dataParts[i]);
                }
                let signature = _signer.sign();

                let verified = _sigVerifier.verify(
                    signature, bitwise.concatenate(_data, _otherData1, _otherData2));
                assert.isTrue(verified);

                // Call sign with argument consisting of last data part.
                for (let j = 0; j < (_dataParts.length - 1); j++) {
                    _signer.update(_dataParts[j]);
                }
                signature = _signer.sign(_otherData2);

                verified = _sigVerifier.verify(
                    signature, bitwise.concatenate(_data, _otherData1, _otherData2));
                assert.isTrue(verified);
            });

            it('sign some data consisting of multiple parts, using method chaining',
                function () {
                    // Call sign with no arguments.
                    let signature = _signer.update(_data)
                        .update(_otherData1)
                        .update(_otherData2)
                        .sign();

                    let verified = _sigVerifier.verify(
                        signature, bitwise.concatenate(_data, _otherData1, _otherData2));
                    assert.isTrue(verified);

                    // Call sign with argument consisting of last data part.
                    signature =
                        _signer.update(_data).update(_otherData1).sign(_otherData2);

                    verified = _sigVerifier.verify(
                        signature, bitwise.concatenate(_data, _otherData1, _otherData2));
                    assert.isTrue(verified);
                });

            it('verify a signature of data consisting of multiple parts', function () {
                const signature =
                    _signer.sign(bitwise.concatenate(_data, _otherData1, _otherData2));

                // Call verify with no arguments.
                for (let i = 0; i < _dataParts.length; i++) {
                    _sigVerifier.update(_dataParts[i]);
                }
                let verified = _sigVerifier.verify(signature);
                assert.isTrue(verified);

                // Call verify with argument consisting of last data part.
                for (let j = 0; j < (_dataParts.length - 1); j++) {
                    _sigVerifier.update(_dataParts[j]);
                }
                verified = _sigVerifier.verify(signature, _otherData2);
                assert.isTrue(verified);
            });

            it('verify a signature of data consisting of multiple parts, using method chaining',
                function () {
                    const signature = _signer.sign(
                        bitwise.concatenate(_data, _otherData1, _otherData2));

                    // Call verify with no arguments.
                    let verified = _sigVerifier.update(_data)
                        .update(_otherData1)
                        .update(_otherData2)
                        .verify(signature);
                    assert.isTrue(verified);

                    // Call verify with argument consisting of last data part.
                    verified = _sigVerifier.update(_data)
                        .update(_otherData1)
                        .verify(signature, _otherData2);
                    assert.isTrue(verified);
                });

            it('sign some data of type string and verify the signature', function () {
                const signature = _signer.update(DATA_STRING)
                    .update(OTHER_DATA_1_STRING)
                    .sign(OTHER_DATA_2_STRING);

                const verified = _sigVerifier.update(DATA_STRING)
                    .update(OTHER_DATA_1_STRING)
                    .verify(signature, OTHER_DATA_2_STRING);

                assert.isTrue(verified);
            });

            it('sign some data and verify the signature, using RsaPrivateKey and RsaPublicKey objects',
                function () {
                    const rsaPrivateKey =
                        new _service.newRsaPrivateKey({pem: _privateKey});
                    const signer = _service.newSigner().init(rsaPrivateKey.toPem());

                    const rsaPublicKey = new _service.newRsaPublicKey({pem: _publicKey});
                    const sigVerifier =
                        _service.newSignatureVerifier().init(rsaPublicKey.toPem());

                    const signature = signer.sign(_data);
                    assert.isTrue(sigVerifier.verify(signature, _data));
                });
        });
    });
});

describe(describeText1, function () {
    beforeEach(function () {
        beforeEachHook();
    });

    describe(describeText2, function () {
        describe('create a signer/signature verifier pair that should be able to ..', function () {
            it('throw an error when initializing the signer, using a private key with invalid PEM formatting.',
                function () {
                    expect(function () {
                        _signer.init(_invalidEncodingKeyPair.getPrivate());
                    }).to.throw();
                });

            it('throw an error when initializing the signer, using a private key with an invalid public exponent.',
                function () {
                    expect(function () {
                        _signer.init(_invalidExponentKeyPair.getPrivate());
                    }).to.throw();
                });

            it('throw an error when updating the signer with some data before the signer has been initialized with a private key',
                function () {
                    const signer = _service.newSigner();

                    expect(function () {
                        signer.update(_data);
                    }).to.throw();
                });

            it('throw an error when signing some data before the signer has been initialized with a private key',
                function () {
                    const signer = _service.newSigner();

                    expect(function () {
                        signer.sign(_data);
                    }).to.throw();
                });

            it('throw an error when initializing the signature verifier, using a public key with invalid PEM formatting.',
                function () {
                    expect(function () {
                        _sigVerifier.init(_invalidEncodingKeyPair.getPublic());
                    }).to.throw();
                });

            it('throw an error when initializing the signature verifier, using a public key with an invalid public exponent.',
                function () {
                    expect(function () {
                        _sigVerifier.init(_invalidExponentKeyPair.getPublic());
                    }).to.throw();
                });

            it('throw an error when updating the signature verifier with some data before the verifier has been initialized with a public key',
                function () {
                    const sigVerifier = _service.newSignatureVerifier();

                    expect(function () {
                        sigVerifier.update(_data);
                    }).to.throw();
                });

            it('throw an error when verifying a signature before the signature verifier has been initialized with a public key',
                function () {
                    const sigVerifier = _service.newSignatureVerifier();

                    expect(function () {
                        sigVerifier.verify(_signature, _data);
                    }).to.throw();
                });

            it('throw an error when signing without either providing data or having previously updated the signer with some data',
                function () {
                    const signer = _service.newSigner().init(_privateKey);

                    expect(function () {
                        signer.sign();
                    }).to.throw();
                });

            it('throw an error when verifying a signature without either providing data or having previously updated the verifier with some data',
                function () {
                    const sigVerifier = _service.newSignatureVerifier().init(_publicKey);

                    expect(function () {
                        sigVerifier.verify(_signature);
                    }).to.throw();
                });
        });
    });
});


