/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* jshint ignore: start */
/* zglobal require */
/* zglobal OV */

const createVote = require('../protocol/create-vote');
const {serializeElGamalCiphertext, serializeExponentiationProof, serializePlaintextEqualityProof} = require("../model/primitives-serializer");

module.exports = (function () {
	'use strict';

	const XMLHttpRequest = XMLHttpRequest || require('xhr2');
	const Q = require('q');
	const config = require('./config.js');
	const session = require('./session.js');

	// lenient json parse

	const jsonparse = function (x) {
		try {
			return JSON.parse(x);
		} catch (ignore) {
			return {};
		}
	};

	// create a vote request

	const createVoteRequest = function (
		encodedVotingOptionsInt,
		correctness
	) {

		const primitivesParams = session('primitivesParams');

		// Avoid type coercion in the voter's selections
		const votersSelections = encodedVotingOptionsInt.map(function (o) {
			return parseInt(o, 10);
		});

		const createVoteOutput = createVote(
			session('verificationCardId'),
			votersSelections,
			primitivesParams.electionPublicKey,
			primitivesParams.verificationCardSecretKey,
			config('electionEventId')
		);

		const correctnessIds = encodedVotingOptionsInt.map(function (o) {
			return correctness[o] || [];
		});

		// serialize vote elements

		const serializedEncryptedVote = serializeElGamalCiphertext(createVoteOutput.encryptedVote);
		const serializedEncryptedPartialChoiceReturnCodes = serializeElGamalCiphertext(createVoteOutput.encryptedPartialChoiceReturnCodes);
		const serializedExponentiatedEncryptedVote = serializeElGamalCiphertext(createVoteOutput.exponentiatedEncryptedVote);
		const serializedExponentiationProof = serializeExponentiationProof(createVoteOutput.exponentiationProof);
		const serializedPlaintextEqualityProof = serializePlaintextEqualityProof(createVoteOutput.plaintextEqualityProof);

		const serializedCorrectnessIds = JSON.stringify(correctnessIds);

		return {
			encryptedOptions: serializedEncryptedVote,
			encryptedPartialChoiceCodes: serializedEncryptedPartialChoiceReturnCodes,
			correctnessIds: serializedCorrectnessIds,
			credentialId: session('credentials').credentialId,
			exponentiationProof: serializedExponentiationProof,
			plaintextEqualityProof: serializedPlaintextEqualityProof,
			cipherTextExponentiations: serializedExponentiatedEncryptedVote
		};
	};

	// process vote response

	const processVoteResponse = function (response) {
		return response.choiceCodes.split(';');
	};

	// process vote

	const processVote = function (deferred, response) {
		if (response.valid && response.choiceCodes) {
			try {
				const result = processVoteResponse(response);
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
	};

	// encrypt and send the vote

	const sendVote = function (
		options,
		correctness
	) {
		const deferred = Q.defer();

		const voteRequestData = createVoteRequest(
			options,
			correctness
		);

		// send vote

		const endpoint = config('endpoints.votes')
			.replace('{tenantId}', config('tenantId'))
			.replace('{electionEventId}', config('electionEventId'))
			.replace('{votingCardId}', session('votingCardId'));

		const xhr = new XMLHttpRequest();
		xhr.open('POST', config('host') + endpoint);
		xhr.onreadystatechange = function () {
			if (xhr.readyState === 4) {
				if (xhr.status === 200) {
					processVote(deferred, JSON.parse(this.responseText));
				} else {
					let response = jsonparse(this.responseText);
					if (!response || typeof response !== 'object') {
						response = {};
					}
					response.httpStatus = xhr.status;
					response.httpStatusText = xhr.statusText;
					deferred.reject(response);
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
		xhr.send(JSON.stringify(voteRequestData));

		return deferred.promise;
	};

	return {
		sendVote: sendVote
	};
})();
