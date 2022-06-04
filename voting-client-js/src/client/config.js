/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
module.exports = (function () {
	'use strict';

	let cfg = {
		lib: 'crypto/ov-api.min.js',

		tenantId: '',
		electionEventId: '',

		host: '/ag-ws-rest/api/ov/voting/v1',

		'endpoints.authentication':
			'/tenant/{tenantId}/electionevent/{electionEventId}/extended_authenticate',
		'endpoints.informations':
			'/tenant/{tenantId}/electionevent/{electionEventId}/credential/{credentialId}/auth/request-token',
		'endpoints.tokens':
			'/tenant/{tenantId}/electionevent/{electionEventId}/credential/{credentialId}/auth/authenticate-token',
		'endpoints.votes':
			'/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}/vote',
		'endpoints.confirmations':
			'/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}/confirm',
		'endpoints.choicecodes':
			'/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}/choicecodes',
		'endpoints.votecastcodes':
			'/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}/castcode',
	};

	// get/set values in cfg

	const config = function (name, value) {
		if (value) {
			// if called with name and value args, asign value to name property
			return (cfg[name] = value);
		} else {
			switch (typeof name) {
				case 'object':
					// if called with and object, assign it as new full state
					return (cfg = name);

				case 'undefined':
					// if called with no args, return full state
					return cfg;

				default:
					// if called with a single string, return that property
					return cfg[name];
			}
		}
	};

	return config;
})();
