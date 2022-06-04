/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global OV */
/* jshint maxlen: 6666 */

describe('Validators api', function () {
	'use strict';

	const TD = require('./mocks/testdata.json');

	const errorMessage = 'Bad server challenge';

	it('should parse a start voting key', function () {
		const testKey = TD.startVotingKey;
		const eeid = TD.eventId;
		const expected = {
			credentialId: TD.infoResponse.credentialData.id,
			keystoreSymmetricEncryptionKey: '4df144b2c04b0b45ec1035adbba89e22',
		};
		expect(OV.parseStartVotingKey(testKey, eeid)).toEqual(expected);
	});

	it('should parse a server serverChallengeMessage', function () {
		expect(function () {
			OV.parseServerChallenge({
				serverChallengeMessage: 'adsf',
			});
		}).toThrow(new Error(errorMessage));

		expect(function () {
			OV.parseServerChallenge({
				serverChallengeMessage: {
					signature: 'asdf',
				},
			});
		}).toThrow(new Error(errorMessage));

		expect(function () {
			OV.parseServerChallenge({
				serverChallengeMessage: {
					serverChallenge: '2190381097405701',
					timestamp: '1428486196320',
				},
			});
		}).toThrow(new Error(errorMessage));
	});
});
