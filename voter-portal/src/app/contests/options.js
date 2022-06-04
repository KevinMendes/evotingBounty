/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function (OptionsService) {
	'ngInject';

	return {
		restrict: 'E',
		scope: {
			contest: '=ngModel',
		},
		templateUrl: 'contests/options.tpl.html',
		link: function ($scope) {
			$scope.contest.validate = OptionsService.validate;
			OptionsService.initialize($scope.contest);
		},
	};
};
