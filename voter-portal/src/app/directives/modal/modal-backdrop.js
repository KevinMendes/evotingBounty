/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function ($timeout) {
	'ngInject';

	return {
		restrict: 'EA',
		replace: true,
		templateUrl: 'views/modals/backdrop.html',
		link: function (scope, element, attrs) {
			scope.backdropClass = attrs.backdropClass || '';

			scope.animate = false;

			// trigger CSS transitions
			$timeout(function () {
				scope.animate = true;
			});
		},
	};
};
