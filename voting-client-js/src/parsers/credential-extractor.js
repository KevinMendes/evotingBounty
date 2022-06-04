/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* jshint maxlen: 6666 */
const codec = require('cryptolib-js/src/codec');
const keyStoreService = require('cryptolib-js/src/extendedkeystore').newService();

module.exports = (function () {
	'use strict';

	// Extracts the credential ID authentication token signer key from the credential keystore.

	return function (credentialData, credentialKeystoreSymmetricKey) {
		let credentialKeystoreObject, credentialKeystore, authenticationTokenSignerCertificate;

		try {
			credentialKeystoreObject = JSON.parse(
				codec.utf8Decode(codec.base64Decode(credentialData.data)),
			);
		} catch (e) {
			throw new Error('Invalid credential data');
		}

		try {
			credentialKeystore = keyStoreService.newExtendedKeyStore(credentialKeystoreObject, credentialKeystoreSymmetricKey);
			authenticationTokenSignerCertificate = credentialKeystore.getCertificateBySubject('Auth ' + credentialData.id);
		} catch (e) {
			throw new Error('Could not access credential keystore');
		}

		return {
			authPrivateKey: credentialKeystore.getPrivateKey('auth_sign', credentialKeystoreSymmetricKey),
			certificateAuth: authenticationTokenSignerCertificate,
			credentialId: credentialData.id,
		};
	};
})();
