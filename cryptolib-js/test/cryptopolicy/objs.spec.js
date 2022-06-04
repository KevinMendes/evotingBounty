/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, jasmine:true, expr:true */
'use strict';

const { assert, expect } = require('chai');

const objs = require('../../src/cryptopolicy/objs');

describe('The object manipulation library', function () {
    it('should allow lean copies', function () {
		const obj = {};
		assert.exists(obj.hasOwnProperty);
		const leanObj = objs.leanCopy(obj);
		assert.isUndefined(leanObj.hasOwnProperty);
    });

    // Only run in supporting platforms
    if (Object.freeze) {
        it('should freeze objects', function () {
			const obj = objs.freeze({property: true});
			expect(function () {
                obj.property = false;
            }).to.throw();
        });
    }
});
