/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function () {
	return {
		link: function ($scope, $element, $attrs, controller, $transclude) {
			$transclude($scope.$parent, function (clone) {
				$element.empty();
				$element.append(clone);
			});
		},
	};
};
