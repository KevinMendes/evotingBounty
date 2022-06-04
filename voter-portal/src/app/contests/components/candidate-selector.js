/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function (ContestService, CandidateService, contestTypes) {
	'ngInject';

	return {
		restrict: 'EA',
		scope: {
			candidate: '=',
			contest: '=',
			position: '=',
			onSelected: '=',
		},
		templateUrl: 'contests/components/candidate-selector.tpl.html',
		link: function (scope) {
			scope.contestTypes = contestTypes;
			scope.isCandidateSelected = CandidateService.isCandidateSelected;
			scope.isAliasSelected = CandidateService.isAliasSelected;
			scope.getCandidateCumul = CandidateService.getCandidateCumul;

			scope.candidateMaxAllowedCumul = () => {
				return CandidateService.candidateMaxAllowedCumul(scope.candidate);
			};

			scope.cumulAllowed = function () {
				return scope.candidateMaxAllowedCumul() > 1;
			};

			scope.isSelectedOnCurrentPosition = candidate => {
				const positions = ContestService.getSelectedCandidatePositions(
					candidate,
				);

				return positions.indexOf(scope.position) > -1;
			};

			scope.selectCandidate = function () {
				if (typeof scope.onSelected !== 'function') {
					return angular.noop();
				}

				const {candidate, position} = scope;

				if (candidate.isWriteIn) {
					candidate.writeInValidated = false;
				}

				scope.onSelected({
					candidate,
					position,
				});
			};
		},
	};
};
