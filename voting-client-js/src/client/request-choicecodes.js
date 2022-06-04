/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global require */

module.exports = (function () {
	'use strict';

	const XMLHttpRequest = XMLHttpRequest || require('xhr2');
	const Q = require('q');
	const config = require('./config.js');
	const session = require('./session.js');

	// request choice codes
	return function () {
		const deferred = Q.defer();

		const endpoint = config('endpoints.choicecodes')
			.replace('{tenantId}', config('tenantId'))
			.replace('{electionEventId}', config('electionEventId'))
			.replace('{votingCardId}', session('votingCardId'));

		const xhr = new XMLHttpRequest();
		xhr.open('GET', config('host') + endpoint);
		xhr.onreadystatechange = function () {
			if (xhr.readyState === 4) {
				const response = this.responseText
					? JSON.parse(this.responseText)
					: null;
				if (xhr.status === 200) {
					const parsedResponse = JSON.parse(this.responseText);
					if (parsedResponse.valid && parsedResponse.choiceCodes) {
						deferred.resolve(parsedResponse.choiceCodes.split(';'));
					} else {
						if (parsedResponse.validationError) {
							deferred.reject(response);
						} else {
							deferred.reject('invalid vote');
						}
					}
				} else {
					if (response && response.validationError) {
						deferred.reject(response);
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
		xhr.setRequestHeader(
			'authenticationToken',
			JSON.stringify(session('authenticationToken')),
		);
		xhr.send();

		return deferred.promise;
	};
})();
