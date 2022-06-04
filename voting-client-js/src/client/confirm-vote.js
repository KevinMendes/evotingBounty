/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global require */
/* global OV */

const createConfirmMessage = require("../protocol/create-confirm-message");

module.exports = (function () {
	'use strict';

	const XMLHttpRequest = XMLHttpRequest || require('xhr2');
	const Q = require('q');
	const config = require('./config.js');
	const session = require('./session.js');
	const codec = require('cryptolib-js/src/codec');

	const createConfirmRequest = function (ballotCastingKey) {

		// generate confirmation key
		const primitivesParams = session('primitivesParams');

		const confirmationKey = createConfirmMessage(
			ballotCastingKey,
			primitivesParams.verificationCardSecretKey,
			primitivesParams.encryptionGroup
		);

		// encode the confirmation key in base 64
		const encodedConfirmationKey = codec.base64Encode(confirmationKey.value.toString());

		// prepare and return request

		return {
			credentialId: session('credentials').credentialId,
			confirmationMessage: {
				confirmationKey: encodedConfirmationKey,
			}
		};
	};

	const JSONparse = function (response) {
		let ret;
		try {
			ret = JSON.parse(response);
		} catch (ignore) {
			ret = {
				error: response,
			};
		}
		return ret;
	};

	const processConfirmResponse = function (confirmResponse) {
		return OV.parseCastResponse(
			confirmResponse,
			session('votingCardId'),
			config('electionEventId'),
			session('verificationCardId'),
		);
	};

	// generate the confirmation request and send it to the voting server

	const confirmVote = function (ballotCastingKey) {
		const deferred = Q.defer();

		const reqData = createConfirmRequest(ballotCastingKey);

		// send confirmation

		const endpoint = config('endpoints.confirmations')
			.replace('{tenantId}', config('tenantId'))
			.replace('{electionEventId}', config('electionEventId'))
			.replace('{votingCardId}', session('votingCardId'));

		const xhr = new XMLHttpRequest();
		xhr.open('POST', config('host') + endpoint);
		xhr.onreadystatechange = function () {
			let response;

			if (xhr.readyState === 4) {
				if (xhr.status === 200) {
					response = JSONparse(this.responseText);
					if (response.valid) {
						let result;
						try {
							result = processConfirmResponse(response);
							deferred.resolve(result);
						} catch (e) {
							deferred.reject(e.message);
						}
					} else {
						if (response.validationError) {
							deferred.reject(response);
						} else {
							deferred.reject('invalid vote');
						}
					}
				} else {
					const resp = this.responseText ? JSONparse(this.responseText) : null;
					if (resp && resp.validationError) {
						deferred.reject(resp);
					} else {
						deferred.reject(xhr.status);
					}
				}
			}
		};
		xhr.onerror = function () {
			try {
				deferred.reject(xhr.status);
			} catch (e) {
				//This block is intentionally left blank
			}
		};
		xhr.setRequestHeader('Accept', 'application/json');
		xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
		xhr.setRequestHeader(
			'authenticationToken',
			JSON.stringify(session('authenticationToken')),
		);
		xhr.send(JSON.stringify(reqData));

		return deferred.promise;
	};

	return {
		confirmVote: confirmVote,
		createConfirmRequest: createConfirmRequest,
		processConfirmResponse: processConfirmResponse,
	};
})();
