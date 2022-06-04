/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global require */
const codec = require('cryptolib-js/src/codec');
const deriver = require('cryptolib-js/src/pbkdf')
	.newService()
	.newDeriver();
const digester = require('cryptolib-js/src/messagedigest')
	.newService()
	.newDigester();

module.exports = (function () {
	'use strict';

	const session = require('../client/session.js');

	const derive = function (startVotingKey, salt) {
		const hashedSalt = digester.digest(salt);
		const derived = deriver.derive(startVotingKey, hashedSalt);
		return codec.hexEncode(derived);
	};

	// Takes as input the Start Voting Key and the Election Event ID.
	// returns: { credentialId, keystoreSymmetricEncryptionKey } if valid

	const validateStartVotingKey = function (startVotingKey, eeid) {
		// derive voting card Id
		const credentialId = derive(startVotingKey, 'credentialid' + eeid);

		// derive keystoreSymmetricEncryptionKey
		const keystoreSymmetricEncryptionKey = derive(
			startVotingKey,
			'keystorepin' + eeid,
		);

		session('keystoreSymmetricEncryptionKey', keystoreSymmetricEncryptionKey);
		session('credentialId', credentialId);

		return {
			credentialId: credentialId,
			keystoreSymmetricEncryptionKey: keystoreSymmetricEncryptionKey,
		};
	};

	return validateStartVotingKey;
})();
