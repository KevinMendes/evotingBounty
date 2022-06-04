/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, jasmine:true, expr:true */
'use strict';

const { assert, expect } = require('chai');

const options = require('../../src/cryptopolicy/options');

describe('The policy options object', function () {
    it('should be immutable', function () {
        expect(function () {
            options.newProperty = '';
        }).to.throw(TypeError, /object is not extensible/);
    });

    it('should provide options for existing policies', function () {
        expect(function () {
			const value = options.asymmetric.keyPair.encryption;
			assert.exists(value);
        }).not.to.throw;

        expect(function () {
			const value = options.madeUpSection.subSection.ToMakeItCrash;
			assert.exists(value);
        }).not.to.throw;
    });
});
