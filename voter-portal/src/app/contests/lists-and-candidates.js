/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function (
	ListsAndCandidatesService,
	ContestService,
	$modal,
	gettext,
) {
	'ngInject';

	return {
		restrict: 'E',
		scope: {
			contest: '=ngModel',
		},
		templateUrl: 'contests/lists-and-candidates.tpl.html',
		link: function (scope) {
			scope.contest.validate = ListsAndCandidatesService.validate;
			ListsAndCandidatesService.initialize(scope.contest);
			scope.listSlots = Array.from(
				Array(scope.contest.listQuestion.maxChoices),
			);
			scope.candidateSlots = Array.from(
				Array(scope.contest.candidatesQuestion.maxChoices),
			);

			scope.onListSelected = selectedList => {
				if (ContestService.isCompletelyBlank(scope.contest)) {
					ListsAndCandidatesService.addSelectedListToContest(selectedList);

					return;
				}

				const modalInstance = $modal.open({
					templateUrl: 'views/modals/confirmModal.tpl.html',
					controller: 'confirmModal',
					resolve: {
						labels: () => {
							return {
								title: gettext('Confirm action'),
								content: gettext(
									'If you change the list, all selected candidates will be cleared',
								),
								okButton: gettext('CHANGE LIST'),
								cancelButton: gettext('Cancel'),
							};
						},
					},
				});

				modalInstance.result.then(
					function () {
						ListsAndCandidatesService.addSelectedListToContest(selectedList);
					},
					function () {
						// Intentionally unhandled error
					},
				);
			};

			scope.onListCleared = () => {
				const modalInstance = $modal.open({
					templateUrl: 'views/modals/confirmModal.tpl.html',
					controller: 'confirmModal',
					resolve: {
						labels: () => {
							return {
								title: gettext('Confirm action'),
								content: gettext(
									'If you clear the list, all selected candidates will be cleared',
								),
								okButton: gettext('CLEAR LIST'),
								cancelButton: gettext('Cancel'),
							};
						},
					},
				});

				modalInstance.result.then(function () {
					ListsAndCandidatesService.clearList(scope.contest);
				}, angular.noop);
			};

			scope.onCandidateSelected = selectedCandidate => {
				const {candidate, position} = selectedCandidate;

				ContestService.selectCandidate(candidate, position);
			};

			scope.onCandidateCleared = (candidate, position) => {
				ContestService.clearCandidate(candidate, position);
			};
		},
	};
};
