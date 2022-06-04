/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, mocha:true */
'use strict';

const { expect } = require('chai');

const KeyPair = require('./fixtures/key-pair');
const asymmetric = require('../../src/asymmetric/index');
const codec = require('../../src/codec');
const forge = require('node-forge');

const BigInteger = forge.jsbn.BigInteger;

const DATA_STRING = 'anystring';

let _publicKey;
let _privateKey;
let _service;
let _signer;
let _sigVerifier;
let _data;
let _signature;
let _keyArg;
let _nonObject;
let _emptyObject;
let _nonFunction;
let _nonString;
let _emptyString;
let _nonUint8Array;
let _nonBigInteger;
let _nonPositiveBigInteger;
let _nonCryptoObject;
let _invalidPemEncoding;

function beforeEachFunction() {
    const keyPair = new KeyPair();
    _publicKey = keyPair.getPublic();
    _privateKey = keyPair.getPrivate();

    _service = asymmetric.newService();
    _signer = _service.newSigner();
    _signer.init(_privateKey);
    _sigVerifier = _service.newSignatureVerifier();
    _sigVerifier.init(_publicKey);

    _data = codec.utf8Encode(DATA_STRING);
    _signature = _signer.sign(_data);

    _keyArg = BigInteger.ONE;
    _nonObject = 999;
    _emptyObject = {};
    _nonFunction = 999;
    _nonString = 999;
    _emptyString = '';
    _nonUint8Array = [];
    _nonBigInteger = asymmetric.newService();
    _nonPositiveBigInteger = BigInteger.ZERO;
    _nonCryptoObject = {'nonCrypto': 'value'};
    _invalidPemEncoding = '-----BEGIN';
}

const describeText1 = 'The asymmetric cryptography module should be able to ...';
const describeText2 = 'create an asymmetric cryptography service that should be able to ...';

describe(describeText1, function () {

    beforeEach(function () {
        beforeEachFunction();
    });

    describe(describeText2, function () {
        it('throw an error when being created, using invalid cryptographic policy',
            function () {
                expect(function () {
                    asymmetric.newService({policy: null});
                }).to.throw();

                expect(function () {
                    asymmetric.newService({policy: _nonObject});
                }).to.throw();

                expect(function () {
                    asymmetric.newService({policy: _emptyObject});
                }).to.throw();

                expect(function () {
                    asymmetric.newService({policy: _nonCryptoObject});
                }).to.throw();

                expect(function () {
                    asymmetric.newService({policy: {'asymmetric': undefined}});
                }).to.throw();

                expect(function () {
                    asymmetric.newService({policy: {'asymmetric': null}});
                }).to.throw();
            });

        it('throw an error when being created, using invalid secure random service object',
            function () {
                expect(function () {
                    asymmetric.newService({secureRandomService: null});
                }).to.throw();

                expect(function () {
                    asymmetric.newService({secureRandomService: _nonObject});
                }).to.throw();

                expect(function () {
                    asymmetric.newService({secureRandomService: _emptyObject});
                }).to.throw();

                expect(function () {
                    asymmetric.newService({secureRandomService: _nonCryptoObject});
                }).to.throw();

                expect(function () {
                    asymmetric.newService({secureRandomService: {'newRandomGenerator': _nonFunction}});
                }).to.throw();
            });
    });
});

describe(describeText1, function () {

    beforeEach(function () {
        beforeEachFunction();
    });

    describe(describeText2, function () {
        it('throw an error when creating a new RsaPublicKey object, using invalid input data',
            function () {
                expect(function () {
                    _service.newRsaPublicKey();
                }).to.throw();

                expect(function () {
                    _service.newRsaPublicKey(undefined);
                }).to.throw();

                expect(function () {
                    _service.newRsaPublicKey(null);
                }).to.throw();

                expect(function () {
                    _service.newRsaPublicKey(_nonObject);
                }).to.throw();

                expect(function () {
                    _service.newRsaPublicKey(_emptyObject);
                }).to.throw();

                expect(function () {
                    _service.newRsaPublicKey({pem: _emptyString});
                }).to.throw();

                expect(function () {
                    _service.newRsaPublicKey({pem: _invalidPemEncoding});
                }).to.throw();

                expect(function () {
                    _service.newRsaPublicKey({n: undefined});
                }).to.throw();

                expect(function () {
                    _service.newRsaPublicKey({n: null});
                }).to.throw();

                expect(function () {
                    _service.newRsaPublicKey({n: _nonBigInteger});
                }).to.throw();

                expect(function () {
                    _service.newRsaPublicKey({n: _nonPositiveBigInteger});
                }).to.throw();

                expect(function () {
                    _service.newRsaPublicKey({n: BigInteger.ONE, e: undefined});
                }).to.throw();

                expect(function () {
                    _service.newRsaPublicKey({n: BigInteger.ONE, e: null});
                }).to.throw();

                expect(function () {
                    _service.newRsaPublicKey({n: BigInteger.ONE, e: _nonBigInteger});
                }).to.throw();

                expect(function () {
                    _service.newRsaPublicKey(
                        _keyArg, {n: BigInteger.ONE, e: _nonPositiveBigInteger});
                }).to.throw();
            });
    });
});


describe(describeText1, function () {

    beforeEach(function () {
        beforeEachFunction();
    });

    describe(describeText2, function () {
        it('throw an error when creating a new RsaPrivateKey object, using an invalid params object, PEM string, modulus, public exponent or private exponent',
            function () {
                expect(function () {
                    _service.newRsaPrivateKey();
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey(undefined);
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey(null);
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey(_nonObject);
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey(_emptyObject);
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({pem: _emptyString});
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({pem: _invalidPemEncoding});
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({n: undefined});
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({n: null});
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({n: _nonBigInteger});
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({n: _nonPositiveBigInteger});
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({n: BigInteger.ONE, e: undefined});
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({n: BigInteger.ONE, e: null});
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({n: BigInteger.ONE, e: _nonBigInteger});
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey(
                        {n: BigInteger.ONE, e: _nonPositiveBigInteger});
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey(
                        {n: BigInteger.ONE, e: BigInteger.ONE, d: undefined});
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey(
                        {n: BigInteger.ONE, e: BigInteger.ONE, d: null});
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey(
                        {n: BigInteger.ONE, e: BigInteger.ONE, d: _nonBigInteger});
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: _nonPositiveBigInteger
                    });
                }).to.throw();
            });
    });
});

describe(describeText1, function () {

    beforeEach(function () {
        beforeEachFunction();
    });

    describe(describeText2, function () {
        it('throw an error when creating a new RsaPrivateKey object, using an invalid first or second prime',
            function () {
                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: undefined
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: null
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: _nonBigInteger
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: _nonPositiveBigInteger
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: undefined
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: null
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: _nonBigInteger
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: _nonPositiveBigInteger
                    });
                }).to.throw();
            });
    });
});

describe(describeText1, function () {

    beforeEach(function () {
        beforeEachFunction();
    });

    describe(describeText2, function () {
        it('throw an error when creating a new RsaPrivateKey object, using an invalid first exponent, second exponent or coefficient',
            function () {
                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: BigInteger.ONE,
                        dP: undefined
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: BigInteger.ONE,
                        dP: null
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: BigInteger.ONE,
                        dP: _nonBigInteger
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: BigInteger.ONE,
                        dP: _nonPositiveBigInteger
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: BigInteger.ONE,
                        dP: BigInteger.ONE,
                        dQ: undefined
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: BigInteger.ONE,
                        dP: BigInteger.ONE,
                        dQ: null
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: BigInteger.ONE,
                        dP: BigInteger.ONE,
                        dQ: _nonBigInteger
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: BigInteger.ONE,
                        dP: BigInteger.ONE,
                        dQ: _nonPositiveBigInteger
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: BigInteger.ONE,
                        dP: BigInteger.ONE,
                        dQ: BigInteger.ONE,
                        qInv: undefined
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: BigInteger.ONE,
                        dP: BigInteger.ONE,
                        dQ: BigInteger.ONE,
                        qInv: null
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: BigInteger.ONE,
                        dP: BigInteger.ONE,
                        dQ: BigInteger.ONE,
                        qInv: _nonBigInteger
                    });
                }).to.throw();

                expect(function () {
                    _service.newRsaPrivateKey({
                        n: BigInteger.ONE,
                        e: BigInteger.ONE,
                        d: BigInteger.ONE,
                        p: BigInteger.ONE,
                        q: BigInteger.ONE,
                        dP: BigInteger.ONE,
                        dQ: BigInteger.ONE,
                        qInv: _nonPositiveBigInteger
                    });
                }).to.throw();
            });
    });
});

describe(describeText1, function () {

    beforeEach(function () {
        beforeEachFunction();
    });

    describe(describeText2, function () {
        it('throw an error when creating a new KeyPair object, using invalid input data',
            function () {
                expect(function () {
                    _service.newKeyPair();
                }).to.throw();

                expect(function () {
                    _service.newKeyPair(undefined, _privateKey);
                }).to.throw();

                expect(function () {
                    _service.newKeyPair(null, _privateKey);
                }).to.throw();

                expect(function () {
                    _service.newKeyPair(_nonString, _privateKey);
                }).to.throw();

                expect(function () {
                    _service.newKeyPair(_emptyString, _privateKey);
                }).to.throw();

                expect(function () {
                    _service.newKeyPair(_invalidPemEncoding, _privateKey);
                }).to.throw();

                expect(function () {
                    _service.newKeyPair(_publicKey);
                }).to.throw();

                expect(function () {
                    _service.newKeyPair(_publicKey, undefined);
                }).to.throw();

                expect(function () {
                    _service.newKeyPair(_publicKey, null);
                }).to.throw();

                expect(function () {
                    _service.newKeyPair(_publicKey, _nonString);
                }).to.throw();

                expect(function () {
                    _service.newKeyPair(_publicKey, _emptyString);
                }).to.throw();

                expect(function () {
                    _service.newKeyPair(_publicKey, _invalidPemEncoding);
                }).to.throw();
            });
    });
});

describe(describeText1, function () {

    beforeEach(function () {
        beforeEachFunction();
    });

    describe(describeText2, function () {
        describe('create a signer that should be able to ...', function () {
            it('throw an error when being initialized, using invalid input data',
                function () {
                    expect(function () {
                        _signer.init();
                    }).to.throw();

                    expect(function () {
                        _signer.init(undefined);
                    }).to.throw();

                    expect(function () {
                        _signer.init(null);
                    }).to.throw();

                    expect(function () {
                        _signer.init(_nonString);
                    }).to.throw();

                    expect(function () {
                        _signer.init(_emptyString);
                    }).to.throw();
                });

            it('throw an error when being updated, using invalid input data',
                function () {
                    expect(function () {
                        _signer.update();
                    }).to.throw();

                    expect(function () {
                        _signer.update(undefined);
                    }).to.throw();

                    expect(function () {
                        _signer.update(null);
                    }).to.throw();

                    expect(function () {
                        _signer.update(_nonUint8Array);
                    }).to.throw();
                });

            it('throw an error when signing, using invalid input data', function () {
                expect(function () {
                    _signer.sign(null);
                }).to.throw();

                expect(function () {
                    _signer.sign(_nonUint8Array);
                }).to.throw();
            });
        });
    });
});

describe(describeText1, function () {

    beforeEach(function () {
        beforeEachFunction();
    });

    describe(describeText2, function () {
        describe('create a signature verifier that should be able to ...', function () {
            it('throw an error when being initialized, using invalid input data',
                function () {
                    expect(function () {
                        _sigVerifier.init();
                    }).to.throw();

                    expect(function () {
                        _sigVerifier.init(undefined);
                    }).to.throw();

                    expect(function () {
                        _sigVerifier.init(null);
                    }).to.throw();

                    expect(function () {
                        _sigVerifier.init(_nonString);
                    }).to.throw();

                    expect(function () {
                        _sigVerifier.init(_emptyString);
                    }).to.throw();
                });

            it('throw an error when being updated, using invalid input data',
                function () {
                    expect(function () {
                        _sigVerifier.update();
                    }).to.throw();

                    expect(function () {
                        _sigVerifier.update(undefined);
                    }).to.throw();

                    expect(function () {
                        _sigVerifier.update(null);
                    }).to.throw();

                    expect(function () {
                        _sigVerifier.update(_nonUint8Array);
                    }).to.throw();
                });

            it('throw an error when verifying a signature, using invalid signature',
                function () {
                    expect(function () {
                        _sigVerifier.verify(undefined, _data);
                    }).to.throw();

                    expect(function () {
                        _sigVerifier.verify(null, _data);
                    }).to.throw();

                    expect(function () {
                        _sigVerifier.verify(_nonUint8Array, _data);
                    }).to.throw();
                });

            it('throw an error when verifying a signature, using invalid data that was signed',
                function () {
                    expect(function () {
                        _sigVerifier.verify(_signature, null);
                    }).to.throw();

                    expect(function () {
                        _sigVerifier.verify(_signature, _nonUint8Array);
                    }).to.throw();
                });
        });
    });
});
