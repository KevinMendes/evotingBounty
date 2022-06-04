/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const _ = require('lodash');
const BCK_LEN = 9;

module.exports = angular
	.module('app.validate', [
		'ui.router',
		'app.ui.modal',
		'app.services',
		'app.contests',
	])

	.controller('validate', function (
		$scope,
		$rootScope,
		$state,
		$modal,
		sessionService,
		ContestService,
		ListService,
		contestTypes,
	) {
		'ngInject';

		sessionService.setState('sent');

		$scope.errors = {};
		$scope.data = {
			ballotCastingKey: '',
			processing: false,
		};
		$scope.elections = _.each(sessionService.getElections(), contest => {
			contest.isType = ContestService.isType;

			return contest;
		});

		$scope.contestTypes = contestTypes;

		$scope.resumeSentButNotCast = sessionService.isResumeSentButNotCast();
		$scope.choiceCodes = sessionService.getChoiceCodes();

		const getSeatsForContest = function (contest) {
			if (contest.isType(contestTypes.LISTS_AND_CANDIDATES)) {
				const {
					listQuestion: {maxChoices: listsMaxChoices},
					candidatesQuestion: {maxChoices: candidatesMaxChoices},
				} = contest;

				return listsMaxChoices + candidatesMaxChoices;
			} else if (contest.isType(contestTypes.OPTIONS)) {
				return contest.questions.length;
			} else if (contest.isType(contestTypes.CANDIDATES_ONLY)) {
				const {
					candidatesQuestion: {maxChoices: candidatesMaxChoices},
				} = contest;

				return candidatesMaxChoices;
			}

			throw new Error('Undefined contest type');
		};

		const getSubArrayChoicesCodes = function (array, init, length) {
			const subarray = [];

			for (let i = init; i < init + length; i++) {
				subarray.push(array[i]);
			}

			return subarray;
		};

		$scope.getChoiceCodesForContest = function (contest, cardinal) {
			let choicesCodesAlreadyAssigned = 0;

			for (let i = 0; i < cardinal; i++) {
				choicesCodesAlreadyAssigned += getSeatsForContest($scope.elections[i]);
			}

			if (contest.isType(contestTypes.LISTS_AND_CANDIDATES)) {
				const seats = getSeatsForContest(contest);

				return getSubArrayChoicesCodes(
					$scope.choiceCodes,
					choicesCodesAlreadyAssigned,
					seats,
				);
			} else if (contest.isType(contestTypes.OPTIONS)) {
				return getSubArrayChoicesCodes(
					$scope.choiceCodes,
					choicesCodesAlreadyAssigned,
					contest.questions.length,
				);
			} else if (contest.isType(contestTypes.CANDIDATES_ONLY)) {
				const seats = getSeatsForContest(contest);

				return getSubArrayChoicesCodes(
					$scope.choiceCodes,
					choicesCodesAlreadyAssigned,
					seats,
				);
			}
		};

		$scope.getChoiceCode = function (question) {
			const choiceCode = _.filter(sessionService.getChoiceCodes(), {
				id: question.id,
			});
			const choiceCodes = sessionService.getChoiceCodes();
			const result = [];

			if (choiceCode.length > 0) {
				_.each(choiceCode, function (cc) {
					result.push(cc.choiceCode);
				});
			} else {
				result.push(choiceCodes[question.ordinal - 1]);
			}

			return result.join(' ');
		};

		$scope.inputAreDigits = function (value) {
			$rootScope.safeApply(function () {
				$scope.errors.ballotCastingKeyFormat = false;

				if (!/^[0-9]*$/.test(value)) {
					$scope.errors.ballotCastingKeyFormat = true;
					$scope.errors.ballotCastingKey = false;
				}
			});
		};

		$scope.validateBCK = function (value) {
			$scope.inputAreDigits(value);
		};

		$scope.$watch('data.ballotCastingKey', function (value) {
			$scope.inputAreDigits(value);
		});

		$scope.confirmVote = function () {
			$scope.errors = {};

			// basic input validation

			if (
				!$scope.data.ballotCastingKey ||
				$scope.data.ballotCastingKey.trim().length === 0 ||
				$scope.data.ballotCastingKey.trim().length < BCK_LEN
			) {
				$scope.errors.ballotCastingKey = true;
				$scope.errors.hasError = true;
				document.getElementById('fc_ballot_validation_code').focus();

				return;
			}

			$scope.validateBCK($scope.data.ballotCastingKey);
			if (
				$scope.errors.ballotCastingKeyFormat
			) {
				$scope.errors.hasError = true;
				document.getElementById('fc_ballot_validation_code').focus();

				return;
			}

			// request vote confirmation

			$scope.data.processing = true;
			ovApi.confirmVote($scope.data.ballotCastingKey).then(
				function (data) {
					sessionService.voteCastCode = data.voteCastCode;

					sessionService.setState('cast');

					$state.go('confirmation');
				},
				function (error) {
					$rootScope.safeApply(function () {
						$scope.data.processing = false;

						if ([401, 412, 422].indexOf(error.httpStatus) > -1) {
							sessionService.validationHasError = true;
							sessionService.validationErrorMsg = 'AUTH_TOKEN_EXPIRED';
							$state.go('identification');

							return;
						}

						if (error.validationError) {
							$scope.errors.validationHasError = true;
							$scope.errors.validationErrorMsg =
								error.validationError.validationErrorType;
							$scope.errors.validationErrorArgs =
								error.validationError.errorArgs;
							switch (error.validationError.validationErrorType) {
								case 'WRONG_BALLOT_CASTING_KEY':
									$scope.errors.castFailed = true;
									break;
								case 'BCK_ATTEMPTS_EXCEEDED':
									$scope.errors.castAttemptsFailed = true;
									break;
								case 'AUTH_TOKEN_EXPIRED':
									sessionService.validationHasError = true;
									sessionService.validationErrorMsg =
										$scope.errors.validationErrorMsg;
									$state.go('identification');
									break;
								default:
									$scope.errors.voteFailed = true;
									break;
							}
						} else {
							$scope.errors.voteFailed = true;
							$scope.data.processing = false;
							if (error === 0) {
								$scope.errors.validationHasError = true;
								$scope.errors.validationErrorMsg = 'CONNECTION_ERROR';
							}
						}
						document.getElementById('fc_ballot_validation_code').focus();
					});
				},
			);
		};

		$scope.agreement = function () {
			$modal.open({
				templateUrl: 'views/modals/legalTerms.tpl.html',
				controller: 'defaultModal',
			});
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

		$scope.clearBCK = function () {
			$scope.data.ballotCastingKey = '';
			$scope.data.bck = '';

			const input = document.getElementById('fc_ballot_validation_code');

			input.value = '';
			input.focus();

			$scope.errors.hasError = false;
		};
	}).name;
