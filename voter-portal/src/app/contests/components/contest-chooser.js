/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function (ContestService, contestTypes) {
	'ngInject';

	return {
		restrict: 'EA',
		scope: {
			contest: '=',
		},
		templateUrl: 'contests/components/contest-chooser.tpl.html',
		link: function (scope) {
			scope.contest.expanded = true;
			scope.contest.isType = ContestService.isType;
			scope.contest.canCumulate = ContestService.canCumulate;
			scope.contestTypes = contestTypes;

			scope.getSelectedCandidatesCount = () => {
				if (!scope.contest.selectedCandidates) {
					return 0;
				}

				return scope.contest.selectedCandidates.filter(c => c && !c.isBlank)
					.length;
			};
		},
	};
};
