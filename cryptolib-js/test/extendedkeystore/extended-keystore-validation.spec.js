/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, mocha: true, expr:true */
'use strict';

const { expect } = require('chai');

const keyStore = require('../../src/extendedkeystore');
const validator = require('../../src/input-validator');
const cryptoPolicy = require('../../src/cryptopolicy');

const PASSWORD = '01234567890abcdefghijk';
const PRIVATE_KEY_ALIAS = 'user1';
const SECRET_KEY_ALIAS = 'symmetricalias1';
const ELGAMAL_PRIVATE_KEY_ALIAS = 'elgamalprivatekeyalias1';

const EXTENDED_KEY_STORE = {
	'salt': 'AWnL9mtD1UoAQ58l+8GH9aHPlrDMgeMF8m+0wHyCPAI=',
	'secrets': {
		'symmetricalias1': '7mgsaxqwL1oBASLQ40bcP5btCcLF1kgo+382PsIeqkyw3JeQIYsEIdRuUxdmP/9lUqQj+2sxxlCk6XU=',
		'symmetricalias2': 'hF+vhbcCy0odpXrpPAohUKrnX3LP1sLv9t7QCaBgdStg8rvx55opMrV+l3+ywOwx6u3jKfhTbmL4tnM='
	},
	'store': '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' +
		'LJI5QICBAA='
};

const EXTENDED_KEY_STORE_WITH_ELGAMAL_KEYS = {
	'salt': '7Q951QtBynbVfljodmtQmg351nq/kmxcHfCwdrTUdng=',
	'egPrivKeys': {
		'elgamalprivatekeyalias1': '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',
		'elgamalprivatekeyalias2': 'vezAhuUI9Rqb9SNp9QXGpdiiKjJCGjK3MWmQbSC+NV+4HYEKKuCP6N9RuKAprssqmGYDWUc3ecYD6JsRlW2PNnbAv0oK/aGGogh3gqrVDLqBqSe' +
			't1hmnN3NImAmXscpioAAnfPd1RR4V2lv8+Lc3aUPKTvNwl1P3jATM3TViGzb0hxMhsCuUwFOX'
	},
	'store': 'MFUCAQMwEQYJKoZIhvcNAQcBoAQEAjAAMD0wITAJBgUrDgMCGgUABBR+zoG7NsLvE8zqUFX7HXeP/yK07gQUQaC4BDlbcjaPJxp4KTcJh9wSzMMCAgQA'
};

let keyStoreService;
let extendedKeyStore;
let extendedKeyStoreWithElGamalKeys;
let nonString;
let nonJsonString;
let wrongPassword;
let wrongAlias;
let wrongSubjectCn;
const keyStoreWithNoSalt = clone(EXTENDED_KEY_STORE);
delete keyStoreWithNoSalt.salt;
const keyStoreWithUndefinedSalt = clone(EXTENDED_KEY_STORE);
keyStoreWithUndefinedSalt.salt = null;
const keyStoreWithNoStore = clone(EXTENDED_KEY_STORE);
delete keyStoreWithNoStore.store;
const keyStoreWithUndefinedStore = clone(EXTENDED_KEY_STORE);
keyStoreWithUndefinedStore.store = null;

function beforeEachFunction () {
    let policy = cryptoPolicy.newInstance();
    policy.symmetric.secretKey.encryption.lengthBytes = 32;
    policy.primitives.keyDerivation.pbkdf.keyLengthBytes = 16;
    policy.primitives.keyDerivation.pbkdf.minSaltLengthBytes = 20;
    policy.primitives.keyDerivation.pbkdf.hashAlgorithm = 'SHA256';
    policy.primitives.keyDerivation.pbkdf.numIterations = 1;
    keyStoreService = keyStore.newService({policy: policy});
    extendedKeyStore =
        keyStoreService.newExtendedKeyStore(EXTENDED_KEY_STORE, PASSWORD);

    policy = cryptoPolicy.newInstance();
    policy.symmetric.secretKey.encryption.lengthBytes = 32;
    policy.primitives.keyDerivation.pbkdf.keyLengthBytes = 16;
    policy.primitives.keyDerivation.pbkdf.minSaltLengthBytes = 32;
    policy.primitives.keyDerivation.pbkdf.hashAlgorithm = 'SHA256';
    policy.primitives.keyDerivation.pbkdf.numIterations = 32000;
	const otherKeyStoreService = keyStore.newService({policy: policy});
	extendedKeyStoreWithElGamalKeys = otherKeyStoreService.newExtendedKeyStore(
        EXTENDED_KEY_STORE_WITH_ELGAMAL_KEYS, PASSWORD);

    nonString = 999;
    nonJsonString = 'Not a JSON string';
    wrongPassword = 'Wrong password';
    wrongAlias = 'Wrong alias';
    wrongSubjectCn = 'Wrong subject common name';
}


function clone(object) {
    return JSON.parse(JSON.stringify(object));
}

const describeText1 = 'The key store module that should be able to ...';
const describeText2 = 'create a key store service that should be able to ...';
const describeText3 = 'load a Extended key store that should be able to ...';

// Loading a Extended key store
describe(describeText1, function () {

    beforeEach(function () {
        beforeEachFunction();
    });

    describe(describeText2, function () {
        it('throw an error when loading a Extended key store, using an invalid key store data object or an invalid JSON serialization',
            function () {
                expect(function () {
                    keyStoreService.newExtendedKeyStore(undefined, PASSWORD);
                }).to.throw();

                expect(function () {
                    keyStoreService.newExtendedKeyStore(null, PASSWORD);
                }).to.throw();

                expect(function () {
                    keyStoreService.newExtendedKeyStore({}, PASSWORD);
                }).to.throw();

                expect(function () {
                    keyStoreService.newExtendedKeyStore(keyStoreWithNoSalt, PASSWORD);
                }).to.throw();

                expect(function () {
                    keyStoreService.newExtendedKeyStore(
                        keyStoreWithUndefinedSalt, PASSWORD);
                }).to.throw();

                expect(function () {
                    keyStoreService.newExtendedKeyStore(keyStoreWithNoStore, PASSWORD);
                }).to.throw();

                expect(function () {
                    keyStoreService.newExtendedKeyStore(
                        keyStoreWithUndefinedStore, PASSWORD);
                }).to.throw();

                expect(function () {
                    keyStoreService.newExtendedKeyStore(nonJsonString, PASSWORD);
                }).to.throw();
            });

        it('throw an error when loading a Extended key store, using an invalid password',
            function () {
                expect(function () {
                    keyStoreService.newExtendedKeyStore(EXTENDED_KEY_STORE);
                }).to.throw();

                expect(function () {
                    keyStoreService.newExtendedKeyStore(EXTENDED_KEY_STORE, undefined);
                }).to.throw();

                expect(function () {
                    keyStoreService.newExtendedKeyStore(EXTENDED_KEY_STORE, null);
                }).to.throw();

                expect(function () {
                    keyStoreService.newExtendedKeyStore(EXTENDED_KEY_STORE, nonString);
                }).to.throw();

                expect(function () {
                    keyStoreService.newExtendedKeyStore(EXTENDED_KEY_STORE, wrongPassword);
                }).to.throw();
            });

		it('throw an error when validator receives invalid values',
			function () {
				const label = 'Invalid value';
				expect(function () {
					validator.checkIsType(12, 'object', label);
				}).to.throw('Expected Invalid value to have type \'object\' ; Found: \'number\'');

				expect(function () {
					validator.checkIsInstanceOf([], Uint8Array, 'Uint8Array', label);
				}).to.throw('Invalid value is not an instance of Object Uint8Array');

				expect(function () {
					validator.checkIsNonEmptyString('', label);
				}).to.throw('Invalid value is empty.');

				expect(function () {
					validator.checkIsJsonString(undefined, label);
				}).to.throw('Invalid value is undefined.');

				expect(function () {
					validator.checkIsJsonString(null, label);
				}).to.throw('Invalid value is null.');

				expect(function () {
					validator.checkIsJsonString(';', label);
				}).to.throw('Invalid value is not a valid JSON object.');
			});
    });
});

// Retrieving a private key
describe(describeText1, function () {

    beforeEach(function () {
        beforeEachFunction();
    });

    describe(describeText2, function () {
        describe(describeText3, function () {
            it('throw an error when retrieving a private key, using an invalid alias',
                function () {
                    expect(function () {
                        extendedKeyStore.getPrivateKey(undefined, PASSWORD);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getPrivateKey(null, PASSWORD);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getPrivateKey('', PASSWORD);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getPrivateKey(nonString, PASSWORD);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getPrivateKey(wrongAlias, PASSWORD);
                    }).to.throw();
                });

            it('throw an error when retrieving a private key, using an invalid password',
                function () {
                    expect(function () {
                        extendedKeyStore.getPrivateKey(PRIVATE_KEY_ALIAS);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getPrivateKey(PRIVATE_KEY_ALIAS, undefined);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getPrivateKey(PRIVATE_KEY_ALIAS, null);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getPrivateKey(PRIVATE_KEY_ALIAS, nonString);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getPrivateKey(PRIVATE_KEY_ALIAS, wrongPassword);
                    }).to.throw();
                });
        });
    });
});

// Retrieving a certificate
describe(describeText1, function () {

    beforeEach(function () {
        beforeEachFunction();
    });

    describe(describeText2, function () {
        describe(describeText3, function () {
            it('throw an error when retrieving a certificate, using invalid input data',
                function () {
                    expect(function () {
                        extendedKeyStore.getCertificate();
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getCertificate(undefined);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getCertificate(null);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getCertificate('');
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getCertificate(nonString);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getCertificate(wrongAlias);
                    }).to.throw();
                });

            it('throw an error when retrieving a certificate by subject, using invalid input data',
                function () {
                    expect(function () {
                        extendedKeyStore.getCertificateBySubject();
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getCertificateBySubject(undefined);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getCertificateBySubject(null);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getCertificateBySubject('');
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getCertificateBySubject(nonString);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getCertificateBySubject(wrongSubjectCn);
                    }).to.throw();
                });

            it('throw an error when retrieving a certificate chain, using invalid input data',
                function () {
                    expect(function () {
                        extendedKeyStore.getCertificateChain();
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getCertificateChain(undefined);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getCertificateChain(null);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getCertificateChain('');
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getCertificateChain(nonString);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getCertificateChain(wrongAlias);
                    }).to.throw();
                });
        });
    });
});

// Retrieving a secret key
describe(describeText1, function () {

    beforeEach(function () {
        beforeEachFunction();
    });

    describe(describeText2, function () {
        describe(describeText3, function () {
            it('throw an error when retrieving a secret key, using an invalid alias',
                function () {
                    expect(function () {
                        extendedKeyStore.getSecretKey(undefined, PASSWORD);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getSecretKey(null, PASSWORD);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getSecretKey('', PASSWORD);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getSecretKey(nonString, PASSWORD);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getSecretKey(wrongAlias, PASSWORD);
                    }).to.throw();
                });

            it('throw an error when retrieving a secret key, using an invalid password',
                function () {
                    expect(function () {
                        extendedKeyStore.getSecretKey(SECRET_KEY_ALIAS);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getSecretKey(SECRET_KEY_ALIAS, undefined);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getSecretKey(SECRET_KEY_ALIAS, null);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getSecretKey(SECRET_KEY_ALIAS, nonString);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStore.getSecretKey(SECRET_KEY_ALIAS, wrongPassword);
                    }).to.throw();
                });
        });
    });
});

// Retrieving an ElGamal private key
describe(describeText1, function () {

    beforeEach(function () {
        beforeEachFunction();
    });

    describe(describeText2, function () {
        describe(describeText3, function () {
            it('throw an error when retrieving an ElGamal private key, using an invalid alias',
                function () {
                    expect(function () {
                        extendedKeyStoreWithElGamalKeys.getElGamalPrivateKey(
                            undefined, PASSWORD);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStoreWithElGamalKeys.getElGamalPrivateKey(null, PASSWORD);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStoreWithElGamalKeys.getElGamalPrivateKey('', PASSWORD);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStoreWithElGamalKeys.getElGamalPrivateKey(
                            nonString, PASSWORD);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStoreWithElGamalKeys.getElGamalPrivateKey(
                            wrongAlias, PASSWORD);
                    }).to.throw();
                });

            it('throw an error when retrieving an ElGamal private key, using an invalid password',
                function () {
                    expect(function () {
                        extendedKeyStoreWithElGamalKeys.getElGamalPrivateKey(
                            ELGAMAL_PRIVATE_KEY_ALIAS);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStoreWithElGamalKeys.getElGamalPrivateKey(
                            ELGAMAL_PRIVATE_KEY_ALIAS, undefined);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStoreWithElGamalKeys.getElGamalPrivateKey(
                            ELGAMAL_PRIVATE_KEY_ALIAS, null);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStoreWithElGamalKeys.getElGamalPrivateKey(
                            ELGAMAL_PRIVATE_KEY_ALIAS, nonString);
                    }).to.throw();

                    expect(function () {
                        extendedKeyStoreWithElGamalKeys.getElGamalPrivateKey(
                            ELGAMAL_PRIVATE_KEY_ALIAS, wrongPassword);
                    }).to.throw();
                });
        });
    });
});
