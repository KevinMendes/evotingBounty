/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('entitiesCounterService', [
        // 'conf.globals'
    ])

    .factory('entitiesCounterService', function (
        $http,
        endpoints,
        sessionService,
    ) {
        'use strict';

		const model = {};
		const ELECTION_EVENT_ID = '{electionEventId}';

		// init
        model.allowElectionEventNav = false;

		const setElectionEventNav = function (value) {
			model.allowElectionEventNav = value;
		};

		const getTheMainItemsCount = function () {
			$http({
				method: 'GET',
				url: endpoints.host() + endpoints.electionEvents,
			}).then(
				function successCallback(response) {
					model.electionEvents = response.data.result.length;
				},
				function errorCallback(response) {
					model.electionEvents = '?';
				},
			);

			$http({
				method: 'GET',
				url: endpoints.host() + endpoints.administrationBoards,
			}).then(
				function successCallback(response) {
					model.adminBoards = response.data.result.length;
				},
				function errorCallback(data) {
					model.adminBoards = '?';
				},
			);
		};

		const getElectionEventInfo = function (electionEvent) {
			model.selectedElectionEvent = electionEvent.defaultTitle;

			$http({
				method: 'GET',
				url:
					endpoints.host() +
					endpoints.ballots.replace(ELECTION_EVENT_ID, electionEvent.id),
			}).then(
				function successCallback(response) {
					model.ballots = response.data.result.length;
				},
				function errorCallback(response) {
					model.ballots = '?';
				},
			);

			$http({
				method: 'GET',
				url:
					endpoints.host() +
					endpoints.votingCardSets.replace(
						ELECTION_EVENT_ID,
						electionEvent.id,
					),
			}).then(
				function successCallback(response) {
					model.votingCards = response.data.result.length;
				},
				function errorCallback(response) {
					model.votingCards = '?';
				},
			);

			$http({
				method: 'GET',
				url:
					endpoints.host() +
					endpoints.electoralAuthorities.replace(
						ELECTION_EVENT_ID,
						electionEvent.id,
					),
			}).then(
				function successCallback(response) {
					model.electoralAuthorities = response.data.result.length;
				},
				function errorCallback(response) {
					model.electoralAuthorities = '?';
				},
			);

			$http({
				method: 'GET',
				url:
					endpoints.host() +
					endpoints.ballotboxes.replace(ELECTION_EVENT_ID, electionEvent.id),
			}).then(
				function successCallback(response) {
					model.ballotBoxes = response.data.result.length;
				},
				function errorCallback(response) {
					model.ballotBoxes = '?';
				},
			);
		};

		const resetElectionEventInfo = function () {
			model.selectedElectionEvent = undefined;
			model.ballots = '-';
			model.votingCards = '-';
			model.electoralAuthorities = '-';
			model.ballotBoxes = '-';
		};

		return {
            model,
            setElectionEventNav,
            getTheMainItemsCount,
            getElectionEventInfo,
            resetElectionEventInfo,
        };
    });
