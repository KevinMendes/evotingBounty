/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
const keyStoreService = require('cryptolib-js/src/extendedkeystore').newService();
const codec = require('cryptolib-js/src/codec');

module.exports = (function () {
	'use strict';

	// Extracts the verification card secret key
	/* jshint unused:false */
	const extractVerificationCard = function (credentialData, verificationCardKeystoreSymmetricKey) {
		let verificationCardKeystoreObject, verificationCardKeystore, voterSecretKey, aliases, verificationCardSecretExponent;

		try {
			verificationCardKeystoreObject = JSON.parse(
				codec.utf8Decode(codec.base64Decode(credentialData)),
			);
		} catch (e) {
			throw new Error('Invalid verification data: ' + e.message);
		}
		try {
			verificationCardKeystore = keyStoreService.newExtendedKeyStore(verificationCardKeystoreObject, verificationCardKeystoreSymmetricKey);
			aliases = Object.keys(verificationCardKeystoreObject.egPrivKeys);
			verificationCardSecretExponent = verificationCardKeystore.getElGamalPrivateKey(aliases[0], verificationCardKeystoreSymmetricKey);
			voterSecretKey = verificationCardSecretExponent.exponents[0].value;
		} catch (e) {
			throw new Error('Could not access verification card keystore: ' + e.message);
		}

		return voterSecretKey;
	};

	return extractVerificationCard;
})();
