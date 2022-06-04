/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function ($anchorScroll, $timeout) {
	'ngInject';

	return {
		restrict: 'EA',
		link: function (scope) {
			$timeout(function () {
				$anchorScroll();
			}, 100);
		},
	};
};
