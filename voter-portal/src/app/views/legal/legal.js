/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = angular
	.module('app.legal', ['ui.router'])

	.controller('legal', function (
		$scope,
		$rootScope,
		$stateParams,
		sessionService,
	) {
		'ngInject';

		sessionService.setState('');
		sessionService.setElectionEventId($stateParams.eeid);

		$rootScope.requestedLang = $stateParams.lang ? $stateParams.lang : null;

		$scope.NO_EEID = false;
		if (!sessionService.getElectionEventId()) {
			$scope.NO_EEID = true;
		}

		$scope.errors = {};
		$scope.data = {};
	}).name;
