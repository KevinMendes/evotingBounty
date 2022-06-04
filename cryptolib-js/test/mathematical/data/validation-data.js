/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const CommonTestData = require('./common-data');
const forge = require('node-forge');

module.exports = ValidationTestData;

const BigInteger = forge.jsbn.BigInteger;

/**
 * Provides the input validation data needed by the mathematical service unit
 * tests.
 */
function ValidationTestData() {
	const commonTestData = new CommonTestData();

	const p = commonTestData.getP();
	const minCertaintyLevel = commonTestData.getMinimumCertaintyLevel();
	const multiGroupElementValues = commonTestData.multiGroupElementValues();

	const _nonObject = 999;
	const _emptyObject = {};

	const _nonBoolean = '';
	const _nonNumber = '';
	const _nonPositiveNumber = 0;
	const _nonJsonString = 'Not a JSON string';
	const _nonArray = '';
	const _nonBigInteger = 1;

	const _nonObjectArray = [];
	_nonObjectArray.push(1);
    _nonObjectArray.push(2);
    _nonObjectArray.push(3);
	const _emptyObjectArray = [];
	_emptyObjectArray.push({});
    _emptyObjectArray.push({});
    _emptyObjectArray.push({});

	const _tooSmallModulus = new BigInteger('2');
	const _tooSmallOrder = BigInteger.ZERO;
	const _tooLargeOrder = p;
	const _tooSmallGenerator = BigInteger.ONE;
	const _tooLargeGenerator = p;
	const _tooSmallElementValue = BigInteger.ZERO;
	const _tooLargeElementValue = p;

	const _tooSmallPBitLength = 1;
	const _tooLowCertaintyLevel = minCertaintyLevel - 50;
	const _tooLargeNumMembersRequired = multiGroupElementValues.length + 1;

	this.getNonObject = function () {
        return _nonObject;
    };

    this.getEmptyObject = function () {
        return _emptyObject;
    };

    this.getNonBoolean = function () {
        return _nonBoolean;
    };

    this.getNonNumber = function () {
        return _nonNumber;
    };

    this.getNonPositiveNumber = function () {
        return _nonPositiveNumber;
    };

    this.getNonJsonString = function () {
        return _nonJsonString;
    };

    this.getNonArray = function () {
        return _nonArray;
    };

    this.getNonBigInteger = function () {
        return _nonBigInteger;
    };

    this.getNonObjectArray = function () {
        return _nonObjectArray;
    };

    this.getEmptyObjectArray = function () {
        return _emptyObjectArray;
    };

    this.getTooSmallModulus = function () {
        return _tooSmallModulus;
    };

    this.getTooSmallOrder = function () {
        return _tooSmallOrder;
    };

    this.getTooLargeOrder = function () {
        return _tooLargeOrder;
    };

    this.getTooSmallGenerator = function () {
        return _tooSmallGenerator;
    };

    this.getTooLargeGenerator = function () {
        return _tooLargeGenerator;
    };

    this.getTooSmallElementValue = function () {
        return _tooSmallElementValue;
    };

    this.getTooLargeElementValue = function () {
        return _tooLargeElementValue;
    };

    this.getTooSmallModulusBitLength = function () {
        return _tooSmallPBitLength;
    };

    this.getTooLowCertaintyLevel = function () {
        return _tooLowCertaintyLevel;
    };

    this.getTooLargeNumMembersRequired = function () {
        return _tooLargeNumMembersRequired;
    };
}
