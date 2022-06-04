/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global require */
/* global OV */

const codec = require('cryptolib-js/src/codec');
const XMLHttpRequest = XMLHttpRequest || require('xhr2');
const parsePrimitivesParams = require("../parsers/primitives-params");

module.exports = (function () {
	'use strict';

	const Q = require('q');
	const config = require('./config.js');
	const session = require('./session.js');

	const jsrsasign = require('jsrsasign');

	// 'informations' processing (client challenge)

	const processInformationsResponse = function (informationsResponse) {
		// parse credentials

		session(
			'credentials',
			OV.parseCredentials(
				informationsResponse.credentialData,
				session('keystoreSymmetricEncryptionKey'),
			),
		);
		session('certificates', informationsResponse.certificates);

		// validate certificate chain

		OV.validateCertificateChain(
			session('credentials').certificateAuth,
			informationsResponse.certificates,
		);

		// parse server challenge

		const dataToVerify = {
			challengeValue:
			informationsResponse.serverChallengeMessage.serverChallenge,
			timestamp: informationsResponse.serverChallengeMessage.timestamp,
			electionEventId: config('electionEventId'),
			credentialId: session('credentialId'),
		};

		const clientChallengeMessage = OV.parseServerChallenge(
			informationsResponse,
			dataToVerify,
			session('credentials'),
		);

		return {
			serverChallengeMessage: informationsResponse.serverChallengeMessage,
			clientChallengeMessage: clientChallengeMessage,
			certificate: session('credentials').certificateAuth,
			credentialId: session('credentialId'),
		};
	};

	const processTokensResponse = function (tokensResponse) {
		// verify and store authToken
		OV.parseTokenResponse(tokensResponse, session('certificates'), session('credentialId'), config('electionEventId'));

		// store voting card id to validate it later against the authToken credentialId
		session('votingCardId', tokensResponse.authenticationToken.voterInformation.votingCardId);

		session('authenticationToken', tokensResponse.authenticationToken);
		session('verificationCardId', tokensResponse.authenticationToken.voterInformation.verificationCardId);

		// parse credentials from verification card data
		session(
			'verificationCardSecret',
			OV.parseVerificationCard(
				tokensResponse.verificationCard.verificationCardKeystore,
				session('keystoreSymmetricEncryptionKey'),
			),
		);

		// parse ballot as it comes in string format
		const ballot = JSON.parse(
			jsrsasign.jws.JWS.parse(tokensResponse.ballot.signature).payloadObj
				.objectToSign,
		);

		// store the ballot box alphabet decoded
		if (typeof tokensResponse.ballotBox.writeInAlphabet === 'string') {
			ballot.writeInAlphabet = Buffer.from(
				codec.base64Decode(tokensResponse.ballotBox.writeInAlphabet.toString()),
			).toString('utf8');
			session('writeInAlphabet', ballot.writeInAlphabet);
		}

		const ballotTexts = tokensResponse.ballotTextsSignature.map(function (item) {
			return JSON.parse(
				jsrsasign.jws.JWS.parse(item.signedObject).payloadObj.objectToSign,
			);
		});

		// Primitives params
		session('primitivesParams', parsePrimitivesParams(tokensResponse, session('keystoreSymmetricEncryptionKey')));

		return {
			ballot: ballot,
			ballotTexts: ballotTexts,
			status: tokensResponse.votingCardState,
			validationError: tokensResponse.validationError,
		};
	};

	const processTokens = function (deferred, tokensResponse) {
		try {
			deferred.resolve(processTokensResponse(tokensResponse));
		} catch (e) {
			deferred.reject(e);
		}
	};

	const processInformations = function (deferred, informationsResponse) {
		try {
			const clientChallengeMessage = processInformationsResponse(
				informationsResponse,
			).clientChallengeMessage;

			// get the ballot

			const endpoint = config('endpoints.tokens')
				.replace('{tenantId}', config('tenantId'))
				.replace('{electionEventId}', config('electionEventId'))
				.replace('{credentialId}', session('credentialId'));

			const xhr = new XMLHttpRequest();
			xhr.open('POST', config('host') + endpoint);
			xhr.onreadystatechange = function () {
				if (xhr.readyState === 4) {
					if (xhr.status === 200) {
						const response = JSON.parse(this.responseText);
						if (response.authenticationToken) {
							processTokens(deferred, response);
						} else {
							deferred.reject(response.validationError ? response : xhr.status);
						}
					} else {
						deferred.reject(xhr.status);
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
			xhr.send(
				JSON.stringify({
					serverChallengeMessage: informationsResponse.serverChallengeMessage,
					clientChallengeMessage: clientChallengeMessage,
					certificate: session('credentials').certificateAuth,
					credentialId: session('credentialId'),
				}),
			);
		} catch (e) {
			deferred.reject(e);
		}
	};

	// perform c/s challenge and request ballot

	const requestBallot = function (startVotingKey) {

		const deferred = Q.defer();

		try {
			// derive startVotingKey

			const derived = OV.parseStartVotingKey(
				startVotingKey,
				config('electionEventId'),
			);
			session(
				'keystoreSymmetricEncryptionKey',
				derived.keystoreSymmetricEncryptionKey,
			);
			session('credentialId', derived.credentialId);

			// request 'informations'

			const endpoint = config('endpoints.informations')
				.replace('{tenantId}', config('tenantId'))
				.replace('{electionEventId}', config('electionEventId'))
				.replace('{credentialId}', session('credentialId'));

			const xhr = new XMLHttpRequest();
			xhr.open('GET', config('host') + endpoint);
			xhr.onreadystatechange = function () {
				if (xhr.readyState === 4) {
					if (xhr.status === 200) {
						processInformations(deferred, JSON.parse(this.responseText));
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
			xhr.send();
		} catch (e) {
			deferred.reject(e);
		}

		return deferred.promise;
	};

	return {
		requestBallot: requestBallot,
		processInformationsResponse: processInformationsResponse,
		processTokensResponse: processTokensResponse,
	};
})();
