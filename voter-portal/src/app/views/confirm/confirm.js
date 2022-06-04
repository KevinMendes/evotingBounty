/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = angular
	.module('app.confirm', ['ui.router', 'app.ui.modal', 'app.services'])

	.controller('confirm', function (
		$scope,
		$state,
		$modal,
		sessionService,
		gettextCatalog,
	) {
		'ngInject';

		sessionService.setState('cast');

		$scope.data = {
			voteCastCode: sessionService.voteCastCode
		};
		$scope.voteCastReturnCodeFormatted = [
			$scope.data.voteCastCode.toString().slice(0, 4),
			' ',
			$scope.data.voteCastCode.toString().slice(4),
		].join('');
		$scope.errors = {};
		$scope.ballotName = sessionService.getBallotName();

		//Scaped single quote its not working with html filter for IE
		$scope.getClearCacheText = function () {
			return gettextCatalog.getString('For additional security, we recommend you to clear your browser cache to remove any data related to yo' +
				'ur vote. You can find an instruction <a href="https://www.evoting.ch#rules">here</a>');
		};
		$scope.isVoteNew = function () {
			return sessionService.getStatus() !== 'voted';
		};
	}).name;
