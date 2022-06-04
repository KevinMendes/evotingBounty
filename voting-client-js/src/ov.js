/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global require */
/* global self */
/* jshint -W020 */

const Q = require('q');

const model = require('./model/model');
const secureRandom = require('cryptolib-js/src/securerandom');

let secureRandomService;

// ov client api export
const OV = {

	initWorker: function (name, config) {
		'use strict';

		const deferred = Q.defer();

		if (self) {
			self.name = name;
		}

		secureRandomService = secureRandom.newService();

		OV.config(JSON.parse(config));
		deferred.resolve('ok');
		return deferred.promise;
	},

	// High level API

	// main vote cycle
	authenticate: require('./client/authenticate.js').authenticate,
	requestBallot: require('./client/request-ballot.js').requestBallot,
	sendVote: require('./client/send-vote.js').sendVote,
	confirmVote: require('./client/confirm-vote.js').confirmVote,

	// cycle completions
	requestChoiceCodes: require('./client/request-choicecodes.js'),
	requestVoteCastCode: require('./client/request-votecastcode.js'),

	// data models
	model: model,
	Ballot: model.ballot.Ballot,
	ListsAndCandidates: model.ballot.ListsAndCandidates,
	Options: model.ballot.Options,
	Question: model.ballot.Question,
	Option: model.ballot.Option,
	List: model.ballot.List,
	Candidate: model.ballot.Candidate,

	// session and config
	config: require('./client/config.js'),
	session: require('./client/session.js'),
	updateConfig: function (configData) {
		'use strict';
		OV.config(JSON.parse(configData));
	},
	getAuthentication: function () {
		'use strict';
		return OV.session('authenticationToken');
	},

	// message processing API / vote cycle
	processInformationsResponse: require('./client/request-ballot.js')
		.processInformationsResponse,
	processTokensResponse: require('./client/request-ballot.js')
		.processTokensResponse,
	createConfirmRequest: require('./client/confirm-vote.js').createConfirmRequest,
	processConfirmResponse: require('./client/confirm-vote.js')
		.processConfirmResponse,

	// message processing API / cycle completions
	processCastCodeResponse: require('./client/confirm-vote.js')
		.processConfirmResponse,

	// parsers

	BallotParser: require('./parsers/ballot-parser.js'),
	OptionsParser: require('./parsers/options.js'),
	ListsAndCandidatesParser: require('./parsers/lists-and-candidates.js'),
	parseStartVotingKey: require('./parsers/start-voting-key.js'),
	parseServerChallenge: require('./parsers/challenge.js'),
	parseBallotResponse: require('./parsers/ballot-response.js'),
	parseTokenResponse: require('./parsers/auth-token.js')
		.validateAuthTokenResponse,
	parseToken: require('./parsers/auth-token.js').validateAuthToken,
	parseCastResponse: require('./parsers/cast-response.js'),
	parseVerificationCard: require('./parsers/verification-card.js'),
	parseCredentials: require('./parsers/credential-extractor.js'),

	// certificate chain validation
	validateCertificateChain: require('./certificate/certificate-chain.js'),
};

if (typeof self !== 'undefined') {
	self.OV = OV;
} else {
	module.exports = OV;
}

/* jshint ignore: end */
