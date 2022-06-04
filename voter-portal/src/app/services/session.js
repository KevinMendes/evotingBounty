/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function ($rootScope, $q) {
	'ngInject';

	this.userJWTToken = null;
	this.authenticated = false;
	this.status = ''; // partial|voted

	this.choiceCodes = [];
	this.resumeSentButNotCast = false;
	this.validationHasError = false;
	this.validationErrorMsg = '';

	const authenticate = function (userJWTToken) {
		this.authenticated = true;
		this.userJWTToken = userJWTToken;
		this.choiceCodes = [];
		this.resumeSentButNotCast = false;
	};

	const invalidate = function () {
		this.userJWTToken = null;
		this.authenticated = false;
		this.choiceCodes = [];
		this.resumeSentButNotCast = false;
		this.verificationKey = null;
		this.proofValues = null;
		this.elections = null;
		this.ballotName = '';
		this.status = null;
		this.proofsPromise = null;
	};

	const setBallotName = function (ballotName) {
		this.ballotName = ballotName;
	};

	const getBallotName = function () {
		return this.ballotName;
	};

	const setElections = function (elections) {
		this.elections = elections;
	};

	const getElections = function () {
		return this.elections;
	};

	const setStatus = function (status) {
		this.status = status;
	};

	const getStatus = function () {
		return this.status;
	};

	const getAuthenticationToken = function () {
		return this.userJWTToken;
	};

	const setState = function (state) {
		$rootScope.voteState = state;
	};

	const setChoiceCodes = function (choiceCodes) {
		this.choiceCodes = choiceCodes;
	};

	const getChoiceCodes = function () {
		return this.choiceCodes;
	};

	const setResumeSentButNotCast = function (resume) {
		this.resumeSentButNotCast = resume;
	};

	const isResumeSentButNotCast = function () {
		return this.resumeSentButNotCast;
	};

	const setElectionEventId = function (eeid) {
		this.eeid = eeid;
	};

	const getElectionEventId = function () {
		return this.eeid;
	};

	const setProofsPromise = function (promise) {
		this.proofsPromise = promise;
	};

	const getProofsPromise = function () {
		return this.proofsPromise ? this.proofsPromise : $q.resolve(null);
	};

	return {
		authenticate: authenticate,
		invalidate: invalidate,
		setBallotName: setBallotName,
		getBallotName: getBallotName,
		getElections: getElections,
		setElections: setElections,
		getStatus: getStatus,
		setStatus: setStatus,
		getAuthenticationToken: getAuthenticationToken,
		setState: setState,
		setChoiceCodes: setChoiceCodes,
		getChoiceCodes: getChoiceCodes,
		setResumeSentButNotCast: setResumeSentButNotCast,
		isResumeSentButNotCast: isResumeSentButNotCast,
		getElectionEventId: getElectionEventId,
		setElectionEventId: setElectionEventId,
		setProofsPromise: setProofsPromise,
		getProofsPromise: getProofsPromise,
	};
};
