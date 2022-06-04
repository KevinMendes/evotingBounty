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
	gettextCatalog,
	gettext,
	detailedProgressService,
) {
	'ngInject';

	$scope.data = {
		svk: '',
		startVotingKey: '',
		processing: false,
	};
	$scope.errors = {};

	// if the error comes from another view that has redirected to the initial svk page
	if (sessionService.validationHasError) {
		$scope.errors.validationHasError = true;
		$scope.errors.validationErrorMsg = sessionService.validationErrorMsg;
		sessionService.validationHasError = false;
		sessionService.validationErrorMsg = '';
	}

	// initialize the OV api
	$scope.initOV = function () {
		// OV configuration

		const config = {
			lang: $rootScope.lang,
			tenantId: $rootScope.settings.tenantId,
			electionEventId: sessionService.getElectionEventId(),
			debug: $rootScope.settings.debug,
			platformRootCA: platformRootCA,
		};

		// terminate any previous OV instances

		if (typeof ovApi !== 'undefined' && ovApi) {
			ovApi.terminate();
		}

		const ovOk = function () {
			$rootScope.safeApply(function () {
				$scope.ovReady = true;
			});
		};

		const ovError = function () {
			console.error('Could not create workers');
			$rootScope.safeApply(function () {
				$scope.ovError = true;
				$scope.errors.validationHasError = true;
				$scope.errors.validationErrorMsg = 'CONNECTION_ERROR';
			});
		};

		// create our fresh OvApi instance

		try {
			window.ovApi = new OvApi(config);
		} catch (e) {
			ovError();
		}

		// wait for them to warm up

		ovApi
			.init()
			.then(ovOk)
			.fail(ovError);
	};

	$scope.clearSVK = function () {
		$scope.data.startVotingKey = '';
		$scope.data.svk = '';

		const input = document.getElementById('fc_start_voting_code');

		input.value = '';
		input.focus();

		$scope.errors.hasError = false;
		$scope.errors.badKey = false;
	};

	$scope.handleBallot = function (at, ballot) {
		sessionService.authenticate(at);

		// check ballot status and resume vote process accordingly
		switch (ballot.status) {
			case 'SENT_BUT_NOT_CAST':
				ovApi.requestChoiceCodes().then(
					function (choiceCodes) {
						sessionService.setChoiceCodes(choiceCodes);
						sessionService.setResumeSentButNotCast(true);

						$rootScope.safeApply(detailedProgressService.stopProgress);

						$state.go('validate');
					},
					function (error) {
						$rootScope.safeApply(function () {
							$scope.data.processing = false;
							$rootScope.safeApply(detailedProgressService.stopProgress);

							if (error && error.validationError) {
								$scope.errors.validationHasError = true;
								$scope.errors.validationErrorMsg =
									error.validationError.validationErrorType;
								$scope.errors.validationErrorArgs =
									error.validationError.errorArgs;
							} else {
								$scope.errors.hasError = true;
							}
						});
					},
				);
				break;

			case 'CAST':
				ovApi.requestVoteCastCode().then(
					function (data) {
						sessionService.voteCastCode = data.voteCastCode;
						sessionService.setStatus('voted'); // already voted
						$rootScope.safeApply(detailedProgressService.stopProgress);

						$state.go('confirmation');
					},
					function (error) {
						$rootScope.safeApply(function () {
							$scope.data.processing = false;
							$scope.errors.hasError = true;
							if (error && error.validationError) {
								$scope.errors.validationHasError = true;
								$scope.errors.validationErrorMsg =
									error.validationError.validationErrorType;
								$scope.errors.validationErrorArgs =
									error.validationError.errorArgs;
							}
						});
					},
				);
				break;

			case 'NOT_SENT':
				$rootScope.safeApply(detailedProgressService.stopProgress);
				// ok, go to choose view
				$state.go('choose');
				break;

			case 'BLOCKED':
			case 'WRONG_BALLOT_CASTING_KEY':
				$scope.data.processing = false;
				$rootScope.safeApply(function () {
					$scope.errors.hasError = true;
					$scope.errors.validationHasError = true;
					$scope.errors.validationErrorMsg = 'INACTIVE_KEY';
					detailedProgressService.stopProgress();
				});

				break;

			default:
				$scope.data.processing = false;
				console.log('unknown status:', ballot.status);
				$rootScope.safeApply(function () {
					$scope.errors.hasError = true;
					detailedProgressService.stopProgress();
				});
				break;
		}
	};

	$scope.doStartVoting = function (startVotingKey) {
		$rootScope.safeApply(detailedProgressService.startProgressOnNextStep);

		$scope.data.processing = true;
		ovApi.requestBallot(startVotingKey).then(
			function (ballotResponse) {
				const ballot = ovApi.parseBallot(ballotResponse);

				// PO lang code format uses '_' e.g. 'en_US'
				// but Back-end contest come with '-' e.g. 'en-US'
				// so we need to reformat when asking for it to the ovApi
				ovApi.translateBallot(
					ballot,
					gettextCatalog.getCurrentLanguage().replace('_', '-'),
				);
				$rootScope.ballot = ballot;

				sessionService.setElections(ballot.contests);
				sessionService.setStatus(ballot.status);
				sessionService.setBallotName(ballot.title);

				let validationError = null;

				try {
					validationError = ballotResponse.validationError.validationErrorType;
				} catch (ignore) {
					angular.noop();
				}

				if (validationError && validationError !== 'SUCCESS') {
					$rootScope.safeApply(function () {
						detailedProgressService.stopProgress();

						$scope.errors.validationHasError = true;
						$scope.data.processing = false;
						$scope.errors.validationErrorMsg = validationError;

						document.getElementById('fc_start_voting_code').focus();
					});

					return;
				}

				ovApi.getAuthentication().then(
					function (at) {
						$scope.handleBallot(at, ballot);
					},
					function (error) {
						console.error('* Error handling ballot:', error);
						$scope.errors.hasError = true;
					},
				);
			},
			function (error) {
				$rootScope.safeApply(function () {
					detailedProgressService.stopProgress();

					$scope.data.processing = false;
					if (error.validationError) {
						$scope.errors.validationHasError = true;
						$scope.errors.validationErrorMsg =
							error.validationError.validationErrorType;
						$scope.errors.validationErrorArgs = error.validationError.errorArgs;
					} else if (error === 0) {
						$scope.errors.validationHasError = true;
						$scope.errors.validationErrorMsg = 'CONNECTION_ERROR';
					} else if (error.toString().indexOf('TOKEN_ERROR') < 0) {
						$scope.errors.hasError = true;
						$scope.errors.badKey = true;
					} else {
						$scope.errors.hasError = true;
					}
					document.getElementById('fc_start_voting_code').focus();
				});
			},
		);
	};

	$scope.resetVoteState = function () {
		$rootScope.voteState = '';
		$scope.errors = {};
	};

	// check agreement

	if (!$rootScope.accepted || !$rootScope.settings) {
		$state.go('legal-terms', {
			eeid: sessionService.getElectionEventId(),
		});
	}

	// initialize OV

	$scope.initOV();
	$scope.ovReady = false;
	$scope.eeid = sessionService.getElectionEventId();
	sessionService.invalidate();
};
