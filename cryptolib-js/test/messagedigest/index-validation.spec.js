/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, jasmine:true */
'use strict';

const { expect } = require('chai');

const messageDigest = require('../../src/messagedigest');
const cryptoPolicy = require('../../src/cryptopolicy');

describe('The message digest module should be able to ...', function () {
    let _digester;
    let _nonUint8Array;

    beforeEach(function () {
        _digester = messageDigest.newService().newDigester();
        _nonUint8Array = 999;
    });

    describe('create a message digest service that should be able to ..', function () {
        it('throw an error when being created, using an invalid cryptographic policy',
            function () {
				const nonObject = 999;
				const emptyObject = {};

				expect(function () {
                    messageDigest.newService({policy: null});
                }).to.throw();

                expect(function () {
                    messageDigest.newService({policy: nonObject});
                }).to.throw();

                expect(function () {
                    messageDigest.newService({policy: emptyObject});
                }).to.throw();

                expect(function () {
					const invalidPolicy = cryptoPolicy.newInstance();
					invalidPolicy.primitives.messageDigest.algorithm = 'MadeUpAlgorithm';
                    messageDigest.newService({policy: invalidPolicy}).newDigester();
                }).to.throw('Could not create new message digester for unrecognized hash algorithm \'MadeUpAlgorithm\'.');
            });

        describe('create a message digester that should be able to', function () {
            it('throw an error when digesting, using invalid input data', function () {
                expect(function () {
                    _digester.digest(null);
                }).to.throw();

                expect(function () {
                    _digester.digest(_nonUint8Array);
                }).to.throw();
            });

            it('throw an error when updating, using invalid input data', function () {
                expect(function () {
                    _digester.update();
                }).to.throw();

                expect(function () {
                    _digester.update(undefined);
                }).to.throw();

                expect(function () {
                    _digester.update(null);
                }).to.throw();

                expect(function () {
                    _digester.update(_nonUint8Array);
                }).to.throw();
            });
        });
    });
});
