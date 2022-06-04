/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, mocha: true, expr:true */
'use strict';

const { expect } = require('chai');

const keyStore = require('../../src/extendedkeystore');

describe('The key store module that should be able to ...', function () {
    let _nonObject;
    let _emptyObject;

    beforeEach(function () {
        _nonObject = 999;
        _emptyObject = {};
    });

    describe('create a key store service that should be able to ...', function () {
        it('throw an error when being created, using an invalid cryptographic policy',
            function () {
                expect(function () {
                    keyStore.newService({policy: null});
                }).to.throw();

                expect(function () {
                    keyStore.newService({policy: _nonObject});
                }).to.throw();

                expect(function () {
                    keyStore.newService({policy: _emptyObject});
                }).to.throw();
            });

        it('throw an error when being created, using an invalid PBKDF service object',
            function () {
                expect(function () {
                    keyStore.newService({pbkdfService: null});
                }).to.throw();

                expect(function () {
                    keyStore.newService({pbkdfService: _nonObject});
                }).to.throw();

                expect(function () {
                    keyStore.newService({pbkdfService: _emptyObject});
                }).to.throw();
            });

        it('throw an error when being created, using an invalid symmetric cryptography service object',
            function () {
                expect(function () {
                    keyStore.newService({symmetricCryptographyService: null});
                }).to.throw();

                expect(function () {
                    keyStore.newService({symmetricCryptographyService: _nonObject});
                }).to.throw();

                expect(function () {
                    keyStore.newService({symmetricCryptographyService: _emptyObject});
                }).to.throw();
            });

        it('throw an error when being created, using an invalid ElGamal cryptography service object',
            function () {
                expect(function () {
                    keyStore.newService({elGamalCryptographyService: null});
                }).to.throw();

                expect(function () {
                    keyStore.newService({elGamalCryptographyService: _nonObject});
                }).to.throw();

                expect(function () {
                    keyStore.newService({elGamalCryptographyService: _emptyObject});
                }).to.throw();
            });
    });
});
