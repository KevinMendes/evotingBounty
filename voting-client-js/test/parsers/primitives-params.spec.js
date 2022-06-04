/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global OV */

const parsePrimitivesParams = require("../../src/parsers/primitives-params");

describe('Primitives params parser', function () {
	'use strict';

	const testData = require('../mocks/testdata.json');

	it('should parse all the primitives', function () {
		const startVotingKey = testData.startVotingKey;
		const eeId = testData.eventId;
		const derived = OV.parseStartVotingKey(startVotingKey, eeId);

		expect( () => parsePrimitivesParams(testData.authResponse, derived.keystoreSymmetricEncryptionKey)).not.toThrow();
	});
});
