/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = (
	ContestService,
	CandidateService,
	searchService,
	contestTypes,
) => {
	'ngInject';

	return {
		restrict: 'EA',
		scope: {
			data: '=',
			onSelected: '=',
		},
		templateUrl: 'contests/components/candidate-finder.tpl.html',
		link: function (scope, element) {
			scope.contestTypes = contestTypes;

			scope.searchCandidate = function () {
				scope.data.searchedText = scope.data.searchCandidateText;
				scope.data.candidates = searchService.search(
					scope.data.searchCandidateText,
					'name',
					scope.data.allCandidates,
				);

				scope.$broadcast('resetToggles');
			};

			scope.filterByListById = function (id) {
				const obj = scope.data.allLists.filter(function (o) {
					return o.id === id;
				})[0];

				scope.data.selectedList = obj;
				if (scope.data.selectedList) {
					scope.data.allCandidates = scope.data.selectedList.candidates;
					scope.data.candidates = scope.data.selectedList.candidates;
				} else {
					scope.data.allCandidates = CandidateService.getAllCandidates(
						scope.data.contest,
					);
					scope.data.candidates = CandidateService.getAllCandidates(
						scope.data.contest,
					);
				}

				if (scope.data.searchCandidateText) {
					scope.searchCandidate();
				}
			};

			scope.showOnlySelected = false;

			scope.writeInCandidate = ContestService._getAvailableWriteInCandidate(
				scope.data.contest,
				scope.data.position,
			);

			scope.clearCandidate = function () {
				scope.data.searchCandidateText = '';

				const input = element[0].querySelector('#fc_search');

				scope.searchCandidate();
				input.focus();
			};

			scope.showOnlySelectedFilter = function () {
				return item => {
					return scope.showOnlySelected ? item.chosen > 0 : true;
				};
			};
		},
	};
};
