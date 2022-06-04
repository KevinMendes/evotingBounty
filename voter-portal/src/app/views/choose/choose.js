/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const _ = require('lodash');

module.exports = angular
	.module('app.choose', [
		'ui.router',
		'app.ui.modal',
		'app.services',
		'gettext',
	])

	.controller('choose', function (
		$scope,
		$state,
		$modal,
		sessionService,
		gettext,
	) {
		'ngInject';

		sessionService.setState('voting');

		$scope.errors = {};
		$scope.elections = sessionService.getElections();
		let errorWatcherInitialized = false;

		$scope.review = function () {
			$scope.$broadcast('reviewClicked');

			$scope.errors = {
				hasError: false,
			};

			// validate choices
			_.each($scope.elections, function (contest) {
				contest.validate(contest);

				const isContestFormInvalid =
					$scope.contestsForm[`electionForm_${contest.id}`].$invalid;

				contest.error = contest.error || isContestFormInvalid;

				const electionFormErrors =
					$scope.contestsForm[`electionForm_${contest.id}`].$error;

				if (electionFormErrors && electionFormErrors.writeIn) {
					contest.errors.push({
						id: 'write-in',
						message: gettext(
							'Some texts contain an invalid set of characters or an invalid format. Please review the highlighted options below and use valid characters and a valid format. See the errors displayed above each input for more details',
						),
						type: 'warning',
						isHidden: true,
					});

					$scope.contestsForm[`electionForm_${contest.id}`].$$element[0]
						.getElementsByClassName('ng-invalid')[0]
						.getElementsByTagName('input')[0]
						.focus();
				}

				$scope.errors.hasError = $scope.errors.hasError || contest.error;
			});

			if (!$scope.errors.hasError) {
				return $state.go('review');
			}

			if (errorWatcherInitialized) {
				return;
			}

			// When form is submitted, watch for write-in errors
			// in order to clear the write-in error when contests
			// become valid from a write -in perspective
			$scope.$watch('contestsForm.$error.writeIn', () => {
				_.each($scope.elections, function (contest) {
					const isContestWriteInInvalid =
						$scope.contestsForm[`electionForm_${contest.id}`].$error.writeIn;

					if (!isContestWriteInInvalid) {
						_.remove(contest.errors, err => err.id === 'write-in');
					}
				});
			});

			errorWatcherInitialized = true;
		};
	}).name;
