/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

/* global OV */
/* jshint maxlen: 6666 */

const parsePrimitivesParams = require("../../src/parsers/primitives-params")
const createConfirmMessage = require("../../src/protocol/create-confirm-message");

describe('Create confirm message', function () {

	const testData = require('../mocks/testdata.json');
	const startVotingKey = testData.startVotingKey;
	const eeId = testData.eventId;
	const derived = OV.parseStartVotingKey(startVotingKey, eeId);

	const primitivesParams = parsePrimitivesParams(testData.authResponse, derived.keystoreSymmetricEncryptionKey);

	it('should generate a confirmation key', function () {
		const confirmationKey = createConfirmMessage(
			'123456789',
			primitivesParams.verificationCardSecretKey,
			primitivesParams.encryptionGroup
		);

		expect(confirmationKey.value.toString()).toBe('42888064550063210033061727766644097233590060099110265826644284288298253513820182327352934163' +
			'73696358818216528113125305466736672454181011703639181236653340197583285438253079203253841422547560336896107454019119918818294374324723' +
			'74765120381367484913683374352357457327672622847806528414270588341920237611135386027516450524512318468689572312178833166093847932332794' +
			'09171633657353721371083383278756121306583455074687727145600693844149532186448328343487346545722852565347807079604118309687640920686305' +
			'12860283824564890257058740801683220620764512310059685031970942008127770115778948703468893510643916866864141902788078099671');
	});

	describe('should fail with', function () {

		const bck_length_error = "The ballot casting key length must be 9";
		const bck_numeric_error = "The ballot casting key must be a numeric value";

		const parameters = [
			{
				description: "a shorter ballot casting key",
				bck: "12345678",
				vcsk: primitivesParams.verificationCardSecretKey,
				group: primitivesParams.encryptionGroup,
				error: new Error(bck_length_error)
			},
			{
				description: "a longer ballot casting key",
				bck: "1234567890",
				vcsk: primitivesParams.verificationCardSecretKey,
				group: primitivesParams.encryptionGroup,
				error: new Error(bck_length_error)
			},
			{
				description: "a zero ballot casting key",
				bck: "000000000",
				vcsk: primitivesParams.verificationCardSecretKey,
				group: primitivesParams.encryptionGroup,
				error: new Error("The ballot casting key must contain one non-zero element")
			},
			{
				description: "a non-numeric ballot casting key",
				bck: "1A3456789",
				vcsk: primitivesParams.verificationCardSecretKey,
				group: primitivesParams.encryptionGroup,
				error: new Error(bck_numeric_error)
			},
			{
				description: "a space starting ballot casting key",
				bck: " 23456789",
				vcsk: primitivesParams.verificationCardSecretKey,
				group: primitivesParams.encryptionGroup,
				error: new Error(bck_numeric_error)
			},
			{
				description: "a space ending ballot casting key",
				bck: "12345678 ",
				vcsk: primitivesParams.verificationCardSecretKey,
				group: primitivesParams.encryptionGroup,
				error: new Error(bck_numeric_error)
			},
			{
				description: "a null ballot casting key",
				bck: null,
				vcsk: primitivesParams.verificationCardSecretKey,
				group: primitivesParams.encryptionGroup,
				error: new Error()
			},
			{
				description: "a null verification card secret key",
				bck: 123456789,
				vcsk: null,
				group: primitivesParams.encryptionGroup,
				error: new Error()
			},
			{
				description: "a null encryption params",
				bck: 123456789,
				vcsk: primitivesParams.verificationCardSecretKey,
				group: null,
				error: new Error()
			}
		];

		parameters.forEach((parameter) => {
			it(parameter.description, function () {
				expect(function () {
					createConfirmMessage(
						parameter.bck,
						parameter.vcsk,
						parameter.group
					)
				}).toThrow(parameter.error);
			});
		});
	});

});


