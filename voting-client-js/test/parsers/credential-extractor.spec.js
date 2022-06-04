/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global OV */

describe('Options contest parser', function () {
	'use strict';

	const parseCredentials = OV.parseCredentials;

	const credentialData = require('./mocks/credentialData.json');

	const pass = '452e4cd9920e2ec84d48bb176183b66d';

	it('Should open the keystore successfully', function () {
		const actual = parseCredentials(credentialData, pass);

		expect(actual.credentialId).toBe(credentialData.id);
	});
});
