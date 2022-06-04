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
const symmetricCipher = require('cryptolib-js/src/symmetric')
	.newService()
	.newCipher();

module.exports = (function () {
	'use strict';

	const Q = require('q');
	const config = require('./config.js');
	const XMLHttpRequest = XMLHttpRequest || require('xhr2');

	// derives a key from the Authentication Key and a salt.

	const derive = function (authenticationKey, salt) {
		const hashedSalt = digester.digest(salt);
		const derived = deriver.derive(authenticationKey, hashedSalt);
		return codec.hexEncode(derived);
	};

	// derives { authenticationId, extendedAuthenticationKeystoreSymmetricEncryptionKey }

	const deriveKey = function (authenticationKey, eeId) {
		return {
			authenticationId: derive(authenticationKey, 'authid' + eeId),
			extendedAuthenticationKeystoreSymmetricEncryptionKey: derive(
				authenticationKey,
				'authpassword' + eeId,
			),
		};
	};

	const decipherAndResolveAuthKey = function (deferred, authResponse, pin) {
		try {
			const encodedSVK = symmetricCipher
				.init(codec.hexDecode(pin))
				.decrypt(codec.base64Decode(authResponse.encryptedSVK));
			const svk = codec.utf8Decode(encodedSVK);
			deferred.resolve(svk);
		} catch (e) {
			deferred.reject('Could not decipher SVK');
		}
	};

	// retrieves a SVK from an authentication key and (optional) challenge

	const authenticate = function (authenticationKey, challenge, tenantId, eeId) {
		const deferred = Q.defer();

		const authObject = deriveKey(authenticationKey, eeId);

		// request extended auth endpoint

		const endpoint =
			config('host') +
			config('endpoints.authentication')
				.replace('{tenantId}', tenantId)
				.replace('{electionEventId}', eeId);

		const xhr = new XMLHttpRequest();
		xhr.open('POST', endpoint);
		xhr.onreadystatechange = function () {
			if (xhr.readyState === 4) {
				if (xhr.status === 200) {
					// check response. if 'numberOfRemainingAttempts', treat it as an error

					let response = null;
					try {
						response = JSON.parse(this.responseText);
						if (response.numberOfRemainingAttempts) {
							deferred.reject(response);
							return;
						}
					} catch (e) {
						deferred.reject({
							unparseable: this.responseText,
						});
						return;
					}

					decipherAndResolveAuthKey(
						deferred,
						response,
						authObject.extendedAuthenticationKeystoreSymmetricEncryptionKey,
					);
				} else {
					deferred.reject(xhr.status);
				}
			}
		};
		xhr.onerror = function (error) {
			try {
				console.log('** xhr.error', error);
				deferred.reject(xhr.status);
			} catch (e) {
				//This block is intentionally left blank
			}
		};
		xhr.setRequestHeader('Accept', 'application/json');
		xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
		xhr.send(
			JSON.stringify({
				authId: authObject.authenticationId,
				extraParam: challenge,
			}),
		);

		return deferred.promise;
	};

	return {
		authenticate: authenticate,
		deriveKey: deriveKey,
	};
})();
