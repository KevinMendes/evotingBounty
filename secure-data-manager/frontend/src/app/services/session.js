/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
	.module('app.sessionService', [
		// 'conf.globals'
	])

	.factory('sessionService', function ($timeout) {
		'use strict';

		// key expiration config
		const keyExpiration = {
			ab: {
				ms: 60 * 60 * 1000,
				timer: null,
			},
			eb: {
				ms: 60 * 60 * 1000,
				timer: null,
			},
		};

		const setKeyExpiration = function (type, value) {
			const minutes = Number.parseInt(value, 10);
			if (!isNaN(minutes)) {
				keyExpiration[type].ms = minutes * 60 * 1000;
				console.log(
					'Setting (' + type + ') expiration to' + minutes + ' minutes',
				);
			} else {
				console.log(
					'** ERROR **  Wrong argument for expiration (' + type + '):' + value,
				);
			}
		};

		const expireAfter = function (type, board) {
			// reset any previous expiration

			if (keyExpiration[type].timer) {
				$timeout.cancel(keyExpiration[type].timer);
				keyExpiration[type].timer = null;
			}

			// enable expiration for this key

			if (board && board.privateKey) {
				keyExpiration[type].timer = $timeout(function () {
					if (board) {
						board.privateKey = null;
					}
					keyExpiration[type].timer = null;
				}, keyExpiration[type].ms);
			}
		};

		this.userJWTToken = null;
		this.authenticated = false;
		this.status = ''; // partial|voted
		this.membersPoll = {};

		const setHostMode = function (hostMode) {
			this.hostMode = hostMode;
		}

		const getHostMode = function () {
			return this.hostMode;
		}

		const setElectionEvents = function (electionEvents) {
			this.electionEvents = electionEvents;
		};

		const getElectionEvents = function () {
			return this.electionEvents;
		};

		const setAdminBoards = function (adminBoards) {
			this.adminBoards = adminBoards;
		};

		const getAdminBoards = function () {
			return this.adminBoards;
		};

		const setElectoralAuthorities = function (electoralAuthorities) {
			this.electoralAuthorities = electoralAuthorities;
		};

		const getElectoralAuthorities = function () {
			return this.electoralAuthorities;
		};

		const setSelectedElectionEvent = function (selectedElectionEvent) {
			this.selectedElectionEvent = selectedElectionEvent;
		};

		const getSelectedElectionEvent = function () {
			return this.selectedElectionEvent;
		};

		const clearSelectedElectionEvent = function () {
			this.selectedElectionEvent = undefined;
		};

		const setBallots = function (ballots) {
			this.ballots = ballots;
		};

		const getBallots = function () {
			return this.ballots;
		};
		const setVotingCardSets = function (votingCardSets) {
			this.votingCardSets = votingCardSets;
		};

		const getVotingCardSets = function () {
			return this.votingCardSets;
		};

		const setSelectedVotingCardSet = function (selectedVotingCardSet) {
			this.selectedVotingCardSet = selectedVotingCardSet;
		};

		const getSelectedVotingCardSet = function () {
			return this.selectedVotingCardSet;
		};

		const setSelectedElectoralAuthority = function (electoralAuthority) {
			this.selectedElectoralAuthority = electoralAuthority;
		};

		const getSelectedElectoralAuthority = function () {
			return this.selectedElectoralAuthority;
		};

		const setBallotBoxes = function (ballotBoxes) {
			this.ballotBoxes = ballotBoxes;
		};

		const getBallotBoxes = function () {
			return this.ballotBoxes;
		};

		const setSelectedAdminBoard = function (selectedAdminBoard) {
			this.selectedAdminBoard = selectedAdminBoard;
		};

		const getSelectedAdminBoard = function () {
			return this.selectedAdminBoard;
		};

		const setNumberOfSuccessfullyWrittenSmartCards = function (
			successfullyWrittenSmartCards,
		) {
			this.successfullyWrittenSmartCards = successfullyWrittenSmartCards;
		};

		const getNumberOfSuccessfullyWrittenSmartCards = function () {
			return this.successfullyWrittenSmartCards;
		};

		const setMembersPoll = function (poll) {
			this.membersPoll = poll;
		};

		const getMembersPoll = function () {
			return this.membersPoll;
		};

		const hasValidMembersPoll = function () {
			return Object.keys(this.membersPoll).length > 0;
		};

		let synchronizing = 0;

		const startSync = function () {
			synchronizing++;
		};

		const stopSync = function () {
			synchronizing--;
		};

		const isSync = function () {
			return synchronizing > 0;
		};

		const isGeneratePreVotingOutputsEnabled = function () {
			return this.generatePreVotingOutputsEnabled;
		};
		const setGeneratePreVotingOutputsEnabled = function (bool) {
			this.generatePreVotingOutputsEnabled = bool;
		};

		const isGeneratePostVotingOutputsEnabled = function () {
			return this.generatePostVotingOutputsEnabled;
		};
		const setGeneratePostVotingOutputsEnabled = function (bool) {
			this.generatePostVotingOutputsEnabled = bool;
		};

		const isVcPrecomputationEnabled = function () {
			return this.vcPrecomputationEnabled;
		};
		const setVcPrecomputationEnabled = function (bool) {
			this.vcPrecomputationEnabled = bool;
		};

		const isVcComputationEnabled = function () {
			return this.vcComputationEnabled;
		};
		const setVcComputationEnabled = function (bool) {
			this.vcComputationEnabled = bool;
		};

		const isVcDownloadEnabled = function () {
			return this.vcDownloadEnabled;
		};
		const setVcDownloadEnabled = function (bool) {
			this.vcDownloadEnabled = bool;
		};

		const isImportExportEnabled = function () {
			return this.importExportEnabled;
		};
		const setImportExportEnabled = function (bool) {
			this.importExportEnabled = bool;
		};

		const doesActivatedABBelongToSelectedEE = function () {
			const electionEvent = this.getSelectedElectionEvent();
			const adminBoard = this.getSelectedAdminBoard();

			if (electionEvent && adminBoard) {
				const eEventAdminBoard = electionEvent.administrationAuthority.id;
				const activatedAdminBoard = adminBoard.id;
				return eEventAdminBoard === activatedAdminBoard;
			}
		};

		return {
			setHostMode: setHostMode,
			getHostMode: getHostMode,
			getElectionEvents: getElectionEvents,
			setElectionEvents: setElectionEvents,
			setAdminBoards: setAdminBoards,
			getAdminBoards: getAdminBoards,
			setElectoralAuthorities: setElectoralAuthorities,
			getElectoralAuthorities: getElectoralAuthorities,
			setSelectedAdminBoard: setSelectedAdminBoard,
			getSelectedAdminBoard: getSelectedAdminBoard,
			setSelectedElectoralAuthority: setSelectedElectoralAuthority,
			getSelectedElectoralAuthority: getSelectedElectoralAuthority,
			getSelectedElectionEvent: getSelectedElectionEvent,
			setSelectedElectionEvent: setSelectedElectionEvent,
			clearSelectedElectionEvent: clearSelectedElectionEvent,
			getBallots: getBallots,
			setBallots: setBallots,
			getVotingCardSets: getVotingCardSets,
			setVotingCardSets: setVotingCardSets,
			setSelectedVotingCardSet: setSelectedVotingCardSet,
			getSelectedVotingCardSet: getSelectedVotingCardSet,
			setBallotBoxes: setBallotBoxes,
			getBallotBoxes: getBallotBoxes,
			setNumberOfSuccessfullyWrittenSmartCards: setNumberOfSuccessfullyWrittenSmartCards,
			getNumberOfSuccessfullyWrittenSmartCards: getNumberOfSuccessfullyWrittenSmartCards,
			setMembersPoll: setMembersPoll,
			getMembersPoll: getMembersPoll,
			hasValidMembersPoll: hasValidMembersPoll,
			startSync: startSync,
			stopSync: stopSync,
			isSync: isSync,
			expireAfter: expireAfter,
			setKeyExpiration: setKeyExpiration,
			isGeneratePreVotingOutputsEnabled: isGeneratePreVotingOutputsEnabled,
			setGeneratePreVotingOutputsEnabled: setGeneratePreVotingOutputsEnabled,
			isGeneratePostVotingOutputsEnabled: isGeneratePostVotingOutputsEnabled,
			setGeneratePostVotingOutputsEnabled: setGeneratePostVotingOutputsEnabled,
			isVcPrecomputationEnabled: isVcPrecomputationEnabled,
			setVcPrecomputationEnabled: setVcPrecomputationEnabled,
			isVcComputationEnabled: isVcComputationEnabled,
			setVcComputationEnabled: setVcComputationEnabled,
			isVcDownloadEnabled: isVcDownloadEnabled,
			setVcDownloadEnabled: setVcDownloadEnabled,
			isImportExportEnabled: isImportExportEnabled,
			setImportExportEnabled: setImportExportEnabled,
			doesActivatedABBelongToSelectedEE: doesActivatedABBelongToSelectedEE
		};
	});
