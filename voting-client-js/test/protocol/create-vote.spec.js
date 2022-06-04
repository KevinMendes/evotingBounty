/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global OV */

const parsePrimitivesParams = require("../../src/parsers/primitives-params")
const createVote = require('../../src/protocol/create-vote');

describe('Create vote', function () {

	const testData = require('../mocks/testdata.json');
	const startVotingKey = testData.startVotingKey;
	const eeId = testData.eventId;
	const derived = OV.parseStartVotingKey(startVotingKey, eeId);

	const primitivesParams = parsePrimitivesParams(testData.authResponse, derived.keystoreSymmetricEncryptionKey);

	it('should generate an output without error', function () {
		expect(() => createVote(
			testData.authResponse.verificationCard.id,
			[67, 127, 223],
			primitivesParams.electionPublicKey,
			primitivesParams.verificationCardSecretKey,
			testData.eventId
		)).not.toThrow();
	});
});


