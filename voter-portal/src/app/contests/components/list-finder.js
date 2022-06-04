/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function (searchService) {
	'ngInject';

	return {
		restrict: 'EA',
		scope: {
			data: '=data',
			onSelected: '=',
		},
		templateUrl: 'contests/components/list-finder.tpl.html',
		link: function (scope, element) {
			scope.searchParty = function () {
				scope.data.searchedText = scope.data.searchPartyText;
				scope.data.lists = searchService.search(
					scope.data.searchPartyText,
					'name',
					scope.data.allLists,
				);

				scope.$broadcast('resetToggles');
			};

			scope.clearParty = function () {
				scope.data.searchPartyText = '';

				const input = element[0].querySelector('#fc_search');

				scope.searchParty();
				input.focus();
			};
		},
	};
};
