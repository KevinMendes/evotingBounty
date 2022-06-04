/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = (
	$timeout,
	ContestService,
	ListService,
	CandidateService,
	$modal,
	gettext,
	contestTypes,
) => {
	'ngInject';

	return {
		restrict: 'EA',
		require: 'ngModel',
		scope: {
			contest: '=',
			candidate: '=ngModel',
			position: '=',
			onSelected: '=',
			onCleared: '=',
		},
		templateUrl: 'contests/components/candidate-pre-selector.tpl.html',
		controller: function ($scope) {
			'ngInject';

			$scope.getBlankCandidate = ContestService._getAvailableBlankCandidate;

			$scope.contestTypes = contestTypes;
			$scope.hasAllSeatsOccupied = ContestService.hasAllSeatsOccupied;

			$scope.$on('reviewClicked', function () {
				if ($scope.candidate) {
					$scope.candidate.writeInValidated = true;
				}
			});

			// Initialize the wirteInCandidate variable only if the model passed to the
			// directive represents a write-in candidate. If blank, then there's the risk
			// of displaying the "blank candidate" name in the write-in input

			$scope.viewModel = {
				writeInCandidate:
					$scope.candidate && $scope.candidate.isWriteIn
						? $scope.candidate.name
						: '',
			};

			$scope.$watch('viewModel.writeInCandidate', function (newName, oldName) {
				// Do nothing if newName is empty and candidate is null
				// clearCandidate function triggered this either by clearing
				// the list or direct clear of the candidate slot
				if (oldName === newName || (!$scope.candidate && !newName)) {
					return;
				}


                // Update the write-in's name
				if ($scope.candidate.isWriteIn) {
					$scope.candidate.name = newName;
				}
			});

			/**
			 * Methods
			 */
			$scope.openCandidateSelectionModal = function () {
				const {contest, position} = $scope;

				$scope.data = {
					searchedText: '',
					searchCandidateText: '',
					selectedList: {},
					allLists: ListService.getAllLists(contest),
					allCandidates: CandidateService.getAllCandidates(contest),
					candidates: CandidateService.getAllCandidates(contest),
					contest,
					position,
					allListsText: gettext('All Lists'),
				};
				$modal
					.open({
						templateUrl: 'views/modals/choose-candidate.tpl.html',
						controller: 'defaultModal',
						size: 'lg',
						scope: $scope,
					})
					.result.then(function (selectedCandidate) {
					if (typeof $scope.onSelected !== 'function') {
						angular.noop();
					} else {
						$scope.onSelected(selectedCandidate);

						if (
							selectedCandidate.candidate.isWriteIn &&
							!selectedCandidate.candidate.name
						) {
							selectedCandidate.candidate.name = '';
						}
					}

					$scope.refocusBtn();
				});
			};

			$scope.hasDetails = function (candidate) {
				if (!candidate) {
					return false;
				}

				try {
					return (
						(
							(candidate.description
								? candidate.description.toString().trim()
								: '') +
							(candidate.age ? candidate.age.toString().trim() : '') +
							(candidate.profession
								? candidate.profession.toString().trim()
								: '')
						).length > 0
					);
				} catch (ex) {
					return false;
				}
			};

			$scope.getCandidateCumul = CandidateService.getCandidateCumul;

			$scope.candidateMaxAllowedCumul = () => {
				return CandidateService.candidateMaxAllowedCumul($scope.candidate);
			};

			$scope.cumulate = () => {
				$scope.onSelected({
					candidate: $scope.candidate,
					position: ContestService.findEmptyPosition($scope.contest),
				});
			};
		},
		link: function (scope, element, attrs, ngModelCtrl) {
			/**
			 * Directive validations
			 */
			if (!attrs.id) {
				console.error(
					`candidate-pre-selector directive needs a unique id to be specified as an attribute.
          Make sure this id is unique and is not relying on the candidate model attached to the directive
          since is volatile thus resulting in a fluctuating id value.`,
				);
			}

			/**
			 * Methods
			 */
			scope.clearCandidate = function (refocusChoseBtn) {
				scope.viewModel.writeInCandidate = '';

				if (typeof scope.onCleared === 'function') {
					scope.onCleared(scope.candidate, scope.position);
				}

				if (refocusChoseBtn) {
					$timeout(() => {
						const target = document
							.getElementById(attrs.id)
							.getElementsByClassName('btn-select-candidate-goto')[0];

						target.setAttribute('tabindex', -1);
						target.focus();
					}, 10);
				}
			};

			scope.refocusBtn = function () {
				$timeout(() => {
					document
						.getElementById(attrs.id)
						.getElementsByClassName('btn-select-candidate')[0]
						.focus();
				});
			};

			ngModelCtrl.$formatters.push(function (modelValue) {
				return modelValue;
			});

			/**
			 * Model validations
			 */
			scope.$watch('candidate.name', () => {
				ngModelCtrl.$setValidity('writeIn', true);
				ngModelCtrl.$setValidity('name', true);
				ngModelCtrl.$setValidity('alphabet', true);

				if (!scope.candidate || !scope.candidate.isWriteIn) {
					scope.viewModel.writeInCandidate = '';

					return;
				}

				const isWriteInValid = CandidateService.isWriteInValid(scope.candidate);

				if (isWriteInValid !== true) {
					ngModelCtrl.$setValidity('writeIn', false);
					ngModelCtrl.$setValidity(isWriteInValid, false);
				} else {
					scope.candidate.name = scope.candidate.name
						? scope.candidate.name.trim()
						: '';
				}
			});
		},
	};
};
