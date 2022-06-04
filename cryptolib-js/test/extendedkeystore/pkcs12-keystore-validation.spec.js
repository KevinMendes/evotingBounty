/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, mocha: true, expr:true */
'use strict';

const { expect } = require('chai');

const keyStore = require('../../src/extendedkeystore');

const PASSWORD = 'PM6XtkOTbefJmhHGFgbz';
const PRIVATE_KEY_ALIAS = 'root_rsa_private_key';
const PKCS12_DER_B64 = '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';

let _keyStoreService;
let _pkcs12KeyStore;
let _nonUint8Array;
let _nonString;
let _emptyString;
let _wrongPassword;
let _wrongAlias;
let _wrongSubjectCn;

describe('The key store module that should be able to ...', function () {

    beforeEach(function () {
        _keyStoreService = keyStore.newService();
        _pkcs12KeyStore =
            _keyStoreService.newPkcs12KeyStore(PKCS12_DER_B64, PASSWORD);
        _nonUint8Array = [1, 2, 3];
        _nonString = 0;
        _emptyString = '';
        _wrongPassword = 'Wrong password';
        _wrongAlias = 'Wrong alias';
        _wrongSubjectCn = 'Wrong subject common name';
    });

    describe('create a key store service that should be able to ..', function () {
        it('throw an error when loading a PKCS12 key store, using an invalid store',
            function () {
                expect(function () {
                    _keyStoreService.newPkcs12KeyStore(undefined, PASSWORD);
                }).to.throw();

                expect(function () {
                    _keyStoreService.newPkcs12KeyStore(null, PASSWORD);
                }).to.throw();

                expect(function () {
                    _keyStoreService.newPkcs12KeyStore(_nonUint8Array, PASSWORD);
                }).to.throw();

                expect(function () {
                    _keyStoreService.newPkcs12KeyStore(_emptyString, PASSWORD);
                }).to.throw();
            });

        it('throw an error when loading a PKCS12 key store, using an invalid password',
            function () {
                expect(function () {
                    _keyStoreService.newPkcs12KeyStore(PKCS12_DER_B64);
                }).to.throw();

                expect(function () {
                    _keyStoreService.newPkcs12KeyStore(PKCS12_DER_B64, undefined);
                }).to.throw();

                expect(function () {
                    _keyStoreService.newPkcs12KeyStore(PKCS12_DER_B64, null);
                }).to.throw();

                expect(function () {
                    _keyStoreService.newPkcs12KeyStore(PKCS12_DER_B64, _nonString);
                }).to.throw();

                expect(function () {
                    _keyStoreService.newPkcs12KeyStore(PKCS12_DER_B64, _wrongPassword);
                }).to.throw();
            });

        describe('load a PKCS12 key store that should be able to', function () {
            it('throw an error when retrieving a private key, using an invalid alias',
                function () {
                    expect(function () {
                        _pkcs12KeyStore.getPrivateKey(undefined, PASSWORD);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getPrivateKey(null, PASSWORD);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getPrivateKey('', PASSWORD);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getPrivateKey(_nonString, PASSWORD);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getPrivateKey(_wrongAlias, PASSWORD);
                    }).to.throw();
                });

            it('throw an error when retrieving a private key, using an invalid password',
                function () {
                    expect(function () {
                        _pkcs12KeyStore.getPrivateKey(PRIVATE_KEY_ALIAS);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getPrivateKey(PRIVATE_KEY_ALIAS, undefined);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getPrivateKey(PRIVATE_KEY_ALIAS, null);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getPrivateKey(PRIVATE_KEY_ALIAS, _nonString);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getPrivateKey(PRIVATE_KEY_ALIAS, _wrongPassword);
                    }).to.throw();
                });

            it('throw an error when retrieving a certificate, using invalid input data',
                function () {
                    expect(function () {
                        _pkcs12KeyStore.getCertificate();
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getCertificate(undefined);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getCertificate(null);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getCertificate('');
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getCertificate(_nonString);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getCertificate(_wrongAlias);
                    }).to.throw();
                });

            it('throw an error when retrieving a certificate by subject, using invalid input data',
                function () {
                    expect(function () {
                        _pkcs12KeyStore.getCertificateBySubject();
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getCertificateBySubject(undefined);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getCertificateBySubject(null);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getCertificateBySubject('');
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getCertificateBySubject(_nonString);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getCertificateBySubject(_wrongSubjectCn);
                    }).to.throw();
                });

            it('throw and error when retrieving a certificate chain, using invalid input data',
                function () {
                    expect(function () {
                        _pkcs12KeyStore.getCertificateChain();
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getCertificateChain(undefined);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getCertificateChain(null);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getCertificateChain('');
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getCertificateChain(_nonString);
                    }).to.throw();

                    expect(function () {
                        _pkcs12KeyStore.getCertificateChain(_wrongAlias);
                    }).to.throw();
                });
        });
    });
});
