/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function (
	$scope,
	$rootScope,
	$state,
	$modal,
	$window,
	sessionService,
	gettext,
	gettextCatalog,
	$controller,
	$http,
	config,
	detailedProgressService,
) {
	'ngInject';

	// base identification controller:

	$scope.config = config;

	$controller('identificationBase', {
		$scope: $scope,
		$rootScope: $rootScope,
		$state: $state,
		$modal: $modal,
		sessionService: sessionService,
		gettextCatalog: gettextCatalog,
	});

	detailedProgressService.init([
		{
			running: gettext('Authentication in progress'),
			success: gettext('Authentication done'),
		},
		{
			running: gettext('Retrieving Ballot Information in progress'),
			success: gettext('Retrieving Ballot Information done'),
		},
	]);

	// DOB/YOB credential identification

	$scope.startVoting = function () {
		sessionService.setState(''); // restart state: quit, leave, cancel
		$scope.errors = {
			hasError: false,
		};
		$scope.data.processing = true;

		// key present?

		const inputSVK = document.getElementById('fc_start_voting_code');
		const inputDOB = document.getElementById('fc_dob');
		const inputMOB = document.getElementById('fc_mob');
		const inputYOB = document.getElementById('fc_yob');
		let inputToBeFocused;

		if ($scope.authForm.$invalid) {
			$scope.data.processing = false;

			if ($scope.authForm.svk.$invalid) {
				inputToBeFocused = inputSVK;
			} else {
				if ($scope.authForm.dob.$error.day) {
					inputToBeFocused = inputDOB;
				} else if ($scope.authForm.dob.$error.month) {
					inputToBeFocused = inputMOB;
				} else if (
					$scope.authForm.year.$invalid ||
					$scope.authForm.dob.$error.year
				) {
					inputToBeFocused = inputYOB;
				}
			}

			if (inputToBeFocused){
				inputToBeFocused.focus();
			}

			$scope.authForm.$setPristine();
			$scope.authForm.$setUntouched();

			return;
		}

		// retrieve SVK
		$rootScope.safeApply(detailedProgressService.startProgressOnNextStep);

		ovApi
			.authenticate(
				$scope.data.svk.toLowerCase(),
				$scope.data.dob,
				$rootScope.settings.tenantId,
				sessionService.getElectionEventId(),
			)
			.then(
				function (startVotingKey) {
					$scope.doStartVoting(startVotingKey);
				},
				function (error) {
					$rootScope.safeApply(function () {
						detailedProgressService.stopProgress();

						$scope.errors.validationHasError = true;
						$scope.errors.hasError = true;
						$scope.data.processing = false;

						if (error && error.numberOfRemainingAttempts) {
							$scope.retries = error.numberOfRemainingAttempts;
							$scope.errors.validationErrorMsg = 'ERROR_RETRY';

							return;
						}

						switch (error) {
							case 401:
								$scope.retries = error.data.numberOfRemainingAttempts;
								$scope.errors.validationErrorMsg = 'ERROR_RETRY';
								break;
							case 403:
								$scope.errors.validationErrorMsg = 'ERROR_NORETRIES';
								break;
							case 404:
								$scope.errors.validationHasError = false;
								$scope.errors.badKey = true;
								break;
							default:
								if (error === 0) {
									$scope.errors.validationErrorMsg = 'CONNECTION_ERROR';
								}
								break;
						}
						document.getElementById('fc_start_voting_code').focus();
					});
				},
			);
	};
};
