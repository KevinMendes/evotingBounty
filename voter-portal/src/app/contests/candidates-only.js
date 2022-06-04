/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function (CandidatesOnlyService, ContestService) {
	'ngInject';

	return {
		restrict: 'E',
		scope: {
			contest: '=ngModel',
		},
		templateUrl: 'contests/candidates-only.tpl.html',
		link: function (scope) {
			scope.contest.validate = CandidatesOnlyService.validate;
			CandidatesOnlyService.initialize(scope.contest);
			scope.candidateSlots = Array.from(
				Array(scope.contest.candidatesQuestion.maxChoices),
			);

			scope.onListSelected = selectedList => {
				CandidatesOnlyService.addSelectedListToContest(selectedList);
			};

			scope.onListCleared = () => {
				CandidatesOnlyService.clearList(scope.contest);
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
