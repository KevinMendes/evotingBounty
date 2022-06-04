/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const CommonTestData = require('./common-data');

module.exports = ValidationTestData;

/**
 * Provides the input validation data needed by the ElGamal service unit tests.
 */
function ValidationTestData() {
	const commonTestData = new CommonTestData();

	const _nonObject = 999;
	const _emptyObject = {};

	const _nonBoolean = '';
	const _nonJsonString = 'Not a JSON string';
	const _nonArray = '';
	const _nonObjectArray = [];
	_nonObjectArray.push(1);
	_nonObjectArray.push(2);
	_nonObjectArray.push(3);
	const _emptyObjectArray = [];
	_emptyObjectArray.push({});
	_emptyObjectArray.push({});
	_emptyObjectArray.push({});

	const _privateKeyFromAnotherGroup = commonTestData.getLargePrivateKey();
	const _privateKeyExponentsFromAnotherGroup = commonTestData.getExponentsFromLargeZpSubgroup();

	this.getNonObject = function () {
		return _nonObject;
	};

	this.getEmptyObject = function () {
		return _emptyObject;
	};

	this.getNonBoolean = function () {
		return _nonBoolean;
	};

	this.getNonJsonString = function () {
		return _nonJsonString;
	};

	this.getNonArray = function () {
		return _nonArray;
	};

	this.getNonObjectArray = function () {
		return _nonObjectArray;
	};

	this.getEmptyObjectArray = function () {
		return _emptyObjectArray;
	};

	this.getPrivateKeyExponentsFromAnotherGroup = function () {
		return _privateKeyExponentsFromAnotherGroup;
	};

	this.getPrivateKeyFromAnotherGroup = function () {
		return _privateKeyFromAnotherGroup;
	};
}
