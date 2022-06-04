/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = angular
	.module('app.review', [
		'ui.router',
		'app.ui.modal',
		'app.services',
		'app.contests',
	])

	.controller(
		'review',
		/* @ngInject*/ function (
			$scope,
			$rootScope,
			$state,
			$modal,
			sessionService,
			gettext,
			contestTypes,
			ContestService,
		) {
			'ngInject';

			const _ = require('lodash');

			sessionService.setState('voting');
			$scope.errors = {};
			$scope.elections = sessionService.getElections();
			$scope.contestTypes = contestTypes;

			for (const contest of $scope.elections) {
				contest.isBlank = ContestService.isCompletelyBlank(contest);
			}

			// update progress information
			// called with no arguments: reset progress trackers
			// step: string, step name step1|step2
			// action: string, running|success

			const setProgress = function (step, action) {
				// reset

				if (!step) {
					$scope.data = {
						processing: false,
						progress: {
							text: gettext('Sealing Ballot ... please wait'),
							impatientText: gettext(
								'Sealing Ballot ... looks this is taking some time. Please be patient.',
							),
							patience: 2, // seconds
							startTime: Date.now(),
							finalStep: 'step2',

							step1: {
								text: '',
								i18n: {
									running: gettext('Sealing your Ballot'),
									success: gettext('Ballot sealed'),
								},
								running: false,
								success: false,
							},

							step2: {
								text: '',
								i18n: {
									running: gettext('Generating choice codes'),
									success: gettext('Choice codes generated'),
								},
								running: false,
								success: false,
							},
						},
					};

					return;
				}

				// update global progress status

				if (action === 'running') {
					$scope.data.processing = true;
				}
				if (action === 'success' && step === $scope.data.progress.finalStep) {
					$scope.data.processing = false;
				}

				// update step progress status

				if (action === 'running') {
					$scope.data.progress[step].running = true;
					$scope.data.progress[step].text =
						$scope.data.progress[step].i18n[action];
				}
				if (action === 'success') {
					$scope.data.progress[step].running = false;
					$scope.data.progress[step].success = true;
					$scope.data.progress[step].text =
						$scope.data.progress[step].i18n[action];
				}

				// patience check

				const elapsed = (Date.now() - $scope.data.progress.startTime) / 1000;

				if (elapsed > $scope.data.progress.patience) {
					$scope.data.progress.text = $scope.data.progress.impatientText;
				}
			};

			$scope.getOption = function (question) {
				const selectedOption = _.find(question.options, {
					id: question.chosen,
				});

				if (selectedOption) {
					return selectedOption.text;
				}

				if (question.blankOption) {
					return question.blankOption.text;
				}

				return '';
			};

			$scope.isBlankImplicitAnswer = function (question) {
				const selectedOption = _.find(question.options, {
					id: question.chosen,
				});

				return !selectedOption && question.blankOption;
			};

			const sendVote = function () {
				setProgress(); // reset progress
				setProgress('step1', 'running'); // task 1
				$scope.errors = {};

				// assemble vote options
				let allVoteOptions = [];
				let allWriteIns = [];

				_.each($scope.elections, function (election) {
					election.validate(election);

					const representations = ContestService.extractVotedOptionsRepresentations(
						election,
					);

					// This is null only if the contest type is not a known one
					if (!representations) {
						$scope.errors.hasError = true;

						return;
					}

					const {voteOptions, writeIns} = representations;

					allVoteOptions = _.concat(allVoteOptions, voteOptions);
					allWriteIns = _.concat(allWriteIns, writeIns); // Prepared but not used in sendVote method
				});

				// encrypt & send

				const primes = ContestService.getPrimes(allVoteOptions);

				$scope.data.processing = true;

				const correctness = $rootScope.ballot.correctnessIds;

				ovApi
					.sendVote(
						primes,
						correctness
					)
					.then(
						function (choiceCodes) {
							setProgress('step2', 'success');
							sessionService.setChoiceCodes(choiceCodes);
							sessionService.setState('sent');
							$state.go('validate');
						},
						function (error) {
							if ([401, 412, 422].indexOf(error.httpStatus) > -1) {
								sessionService.validationHasError = true;
								sessionService.validationErrorMsg =
									'AUTH_TOKEN_EXPIRED';
								$state.go('identification');

								return;
							}

							$scope.data.processing = false;
							$rootScope.safeApply(function () {
								if (error.validationError) {
									$scope.errors.validationHasError = true;
									$scope.errors.validationErrorMsg =
										error.validationError.validationErrorType;
									$scope.errors.validationErrorArgs =
										error.validationError.errorArgs;

									if (
										$scope.errors.validationErrorMsg ===
										'AUTH_TOKEN_EXPIRED'
									) {
										sessionService.validationHasError = true;
										sessionService.validationErrorMsg =
											$scope.errors.validationErrorMsg;
										sessionService.setState('');
										$state.go('identification');

										return;
									}

									if (
										$scope.errors.validationErrorArgs[0] === 'BLOCKED'
									) {
										$scope.errors.validationHasError = true;
										$scope.errors.validationErrorMsg = 'BLOCKED';
									}
								} else if (error === 0) {
									$scope.errors.validationHasError = true;
									$scope.errors.validationErrorMsg = 'CONNECTION_ERROR';
								} else {
									$scope.errors.voteFailed = true;
								}
							});
						},
						function () {
							$rootScope.safeApply(function () {
								setProgress('step1', 'success');
								setProgress('step2', 'running');
							});
						},
					);
			};

			$scope.sendVote = function () {
				const modalInstance = $modal.open({
					templateUrl: 'views/modals/sendVoteConfirm.tpl.html',
					controller: 'defaultModal',
				});

				modalInstance.result.then(
					function () {
						sendVote();
					},
					function () {
						// Intentionally unhandled error
					},
				);
			};


		},
	).name;
