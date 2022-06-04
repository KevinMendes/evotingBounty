/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const Q = require('q');
const _ = require('lodash');

require('./ov.js');
require('core-js/es/array');

const isWorker = (typeof window === 'undefined');

if (isWorker) {
	self.onmessage = function (message) {
		const msg = JSON.parse(message.data);
		let result = '';
		let operation = msg.op;

		try {
			result = OV[msg.op].apply(
				OV,
				_.isArray(msg.args) ? msg.args : [msg.args],
			);
		} catch (e) {
			console.log('Error calling ' + msg.op, e);
			result = e.message || e.name || e.description || 'error';
			operation = 'error';
		}

		if (result && result.inspect) {
			result.then(
				function (promiseResult) {
					self.postMessage(
						JSON.stringify({
							op: operation,
							args: JSON.stringify(promiseResult),
							from: self.name,
						}),
					);
				},
				function (promiseError) {
					self.postMessage(
						JSON.stringify({
							op: 'error',
							args:
								promiseError instanceof Error
									? promiseError.toString()
									: JSON.stringify(promiseError),
							from: self.name,
						}),
					);
				},
				function (promiseProgress) {
					self.postMessage(
						JSON.stringify({
							op: 'progress',
							args: JSON.stringify(promiseProgress),
							from: self.name,
						}),
					);
				},
			);
		} else {
			self.postMessage(
				JSON.stringify({
					op: operation,
					args: JSON.stringify(result),
					from: self.name,
				}),
			);
		}
	};
} else {
	// cryptolib enabled callable worker

	const CryptoWorker = function (script) {
		const _this = this;
		this.deferred = null;

		try {
			this.worker = new Worker(script);
		} catch (e) {
			_this.workerFailed = true;
		}

		this.worker.onerror = function () {
			_this.workerFailed = true;
		};

		this.worker.onmessage = function (message) {
			const msg = JSON.parse(message.data);

			if (!msg || !msg.op || !_this.deferred) {
				console.log('Worker says:', message);
			} else {
				let result;
				switch (msg.op) {
					case 'error':
						result = msg.args;
						try {
							result = JSON.parse(result);
						} catch (e) {
							//This block is intentionality left blank
						}
						_this.deferred.reject(result);
						break;
					case 'progress':
						_this.deferred.notify(msg.args);
						break;
					default:
						result = msg.args;
						try {
							result = JSON.parse(result);
						} catch (e) {
							//This block is intentionality left blank
						}
						_this.deferred.resolve(result);
						break;
				}
			}
		};
	};

	CryptoWorker.prototype.invoke = function (operation) {
		this.deferred = Q.defer();

		if (this.worker.failed) {
			this.deferred.reject('bad worker');
		} else {
			this.worker.postMessage(
				JSON.stringify({
					op: operation,
					args: [].slice.call(arguments, 1),
				}),
			);
		}

		return this.deferred.promise;
	};

	CryptoWorker.prototype.invokeAndForget = function (operation) {
		this.worker.postMessage(
			JSON.stringify({
				op: operation,
				args: [].slice.call(arguments, 1),
			}),
		);
	};

	const bootstrap = function (workers) {
		// create worker
		workers.main = new CryptoWorker(OV.config('lib'));

		const deferred = Q.defer();

		// check if worker instantiated ok
		if (!workers.main || workers.main.workerFailed) {
			deferred.reject('workerError:instantiate');
			return;
		}

		// seed worker
		workers.main
			.invoke(
				'initWorker',
				'main',
				JSON.stringify(OV.config()),
			)
			.then(
				function (response) {
					deferred.resolve('ok: ' + response);
				},
				function () {
					if (deferred.promise && deferred.promise.isPending()) {
						deferred.reject('workerError:seed');
					}
				},
			);

		return deferred.promise;
	};

	// ----------------------------------------------------------------
	// OvApi: simple Online Voting API

	/**
     @global
     @class OvApi
     @classdesc Simple online voting client interface
     @constructor
     @param {configuration_options} [options] Configuration options
     @example
     window.ovApi = new OvApi({
 lib: 'my/custom/path/ov-api.js',
 lang: 'fr',
 tenantId: 'someTenantId',
 electionEventId: $stateParams.eeid
});
     ovApi.init().then(function() {
 $scope.ovReady = true;
 console.log('Online voting ready!');
});
	 */

	/**
	 * @typedef {Object} Ballot
	 * @property {string} id The ID of the ballot
	 * @property {string} title The title of the ballot
	 * @property {string} description The description of the ballot
	 * @property {Contest[]} contests An array of the contests in the ballot
	 * @property {Object} correctnessIds A map that maps each representation (prime) in the ballot to an array of correctness identifiers
	 */

	/**
	 * @typedef {Object} RequestBallotResponse
	 * @property {AuthenticationToken} authenticationToken The authentication token
	 * @property {Object} validationError Error status
	 * @property {string} votingCardState The state of the voting card
	 * @property {UnparsedBallot} ballot The unparsed ballot
	 * @property {string} ballot.id The ballot ID
	 * @property {Object} ballot.electionEvent Related election event
	 * @property {string} ballot.defaultTitle The ballot default title
	 * @property {string} ballot.defaultDescription The ballot default description
	 * @property {string} ballot.alias The ballot alias
	 * @property {Contest[]} ballot.contests An array of the contests in the ballot
	 * @property {string} ballot.ballotBoxes The ballot boxes
	 * @property {string} ballot.signature The signature of the ballot
	 * @property {Object[]} ballotTexts An array of ballot texts, one per locale
	 * @property {Object[]} ballotTextsSignature An array of signatures, one per ballot text
	 * @property {Object} ballotBox The ballot box
	 * @property {Object} verificationCard The verification card
	 * @property {Object} verificationCardSet The verification card set
	 */

	self.OvApi = function (options) {
		let workers = {};

		if (options) {
			OV.config(_.assign(OV.config(), options));
		}

		return {
			/**
             OvApi initialization
             @memberof! OvApi
             @returns {promise|void}
             @example
             ovApi.init().then(function() {
   console.log('Online voting ready!');
});
			 */
			init: function () {
				return bootstrap(workers);
			},

			/**
             OvApi termination
             @memberof! OvApi
             @returns {void}
			 */
			terminate: function () {
				if (workers) {
					_.each(workers, function (worker) {
						try {
							worker.worker.terminate();
						} catch (e) {
							//This block is intentionality left blank
						}
					});
				}
				workers = {};
			},

			/**
             Sets/overrides configuration parameters supplied in the options object.
             @memberof! OvApi
             @param {configuration_options} [options] Configuration options
             @returns {promise|void}
			 */
			configure: function (options) {
				OV.config(_.assign(OV.config(), options));
				return workers.main.invoke('updateConfig', JSON.stringify(OV.config()));
			},

			/**
             Parses the 'start voting key' and derives its components.
             @memberof! OvApi
             @param {string} startVotingKey The start voting key
             @returns {promise|Object} An object of type {keystoreSymmetricEncryptionKey: '', credentialId: ''}
			 */

			parseStartVotingKey: function (startVotingKey) {
				return workers.main.invoke(
					'parseStartVotingKey',
					startVotingKey,
					OV.config('electionEventId'),
				);
			},

			/**
             First step in the extended authentication process. Takes as input an initialization code (20-digits)
             and an extended authentication factor.
             @memberof! OvApi
             @param {string} authenticationKey An initialization code for the extended authentication service
             @param {string} challenge An extended authentication factor, such as a year of birth OR date of birth
             @returns {promise|Object} The Start Voting Key to be used in subsequent requests
			 */

			authenticate: function (authenticationKey, challenge) {
				return workers.main.invoke(
					'authenticate',
					authenticationKey,
					challenge || '',
					OV.config('tenantId'),
					OV.config('electionEventId'),
				);
			},

			/**
             Performs the second part of the authentication process and, if
             successful, returns the ballot.
             @memberof! OvApi
             @param {string} startVotingKey The 'start voting' key
             @returns {promise|RequestBallotResponse} Multiple data structures including an unparsed ballot. That unparsed ballot can be parsed to obtain a parsed ballot
			 */
			requestBallot: function (startVotingKey) {
				return workers.main.invoke('requestBallot', startVotingKey);
			},

			/**
             Parses a serialized ballot. The parse ballot contains an id, title, description, array of contests and a correctnessIds map
             @memberof! OvApi
             @param {Object} ballotResponse The serialized ballot.json and
             ballotText.json as delivered by the voting platform
             @returns {promise|Ballot} The parsed ballot
			 */
			parseBallot: function (ballotResponse) {
				const ballot = OV.parseBallotResponse(ballotResponse);
				ballot.status = ballotResponse.status;
				return ballot;
			},

			/**
             Selects the i18n texts associated to the locale on the supplied ballot
             @memberof! OvApi
             @param {Ballot} ballot The ballot object
             @param {string} lang The supported language code.
             @returns {void}
			 */
			translateBallot: function (ballot, lang) {
				OV.BallotParser.setLocale(ballot, lang);
			},

			/**
             Encrypts the vote and sends it to the voting platform. If
             successful, the promise will resolve to the choiceCodes
             to show to the user.
             @memberof! OvApi
             @param {string[]} voteOptions The vote options: BigIntegers as strings
             @param {Object} correctness correctness id map as found in ballot
             @returns {promise|string[]} The choice codes
			 */
			sendVote: function (
				voteOptions,
				correctness
			) {
				return workers.main.invoke(
					'sendVote',
					voteOptions,
					correctness
				);
			},

			/**
             Sends the 'confirm vote' message to the voting platform. If
             successful, the promise will resolve to the voteCastCode
             to show to the user.
             @memberof! OvApi
             @param {string} castingKey The 'vote casting key'
             @returns {promise|Object} An object containing voteCastCode
			 */
			confirmVote: function (castingKey) {
				return workers.main.invoke('confirmVote', castingKey);
			},

			/**
             Requests the 'choice codes' from the voting platform.
             This call is only necessary if the vote cycle was
             interrupted.
             @memberof! OvApi
             @returns {promise|string[]} The 'choice codes'
			 */
			requestChoiceCodes: function () {
				return workers.main.invoke('requestChoiceCodes', '');
			},

			/**
             Requests the 'vote cast codes' from the voting platform.
             This call is only necessary if the vote cycle was
             interrupted.
             @memberof! OvApi
             @returns {promise|string} The 'vote cast code'
			 */
			requestVoteCastCode: function () {
				return workers.main.invoke('requestVoteCastCode', '');
			},

			/**
             Retrieves the authentication token for the current
             voter.
             @memberof! OvApi
             @returns {promise|Object} The authentication token
			 */
			getAuthentication: function () {
				return workers.main.invoke('getAuthentication', '');
			}
		};
	};
}
