/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function () {
	'ngInject';

	return {
		restrict: 'EA',
		scope: {
			list: '=',
			onSelected: '=',
		},
		templateUrl: 'contests/components/list-selector.tpl.html',
		link: function (scope) {
			scope.selectParty = function () {
				if (typeof scope.onSelected !== 'function') {
					return angular.noop();
				}

				scope.onSelected(scope.list);
			};
		},
	};
};
