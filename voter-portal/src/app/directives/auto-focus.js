/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function ($timeout) {
	'ngInject';

	return {
		restrict: 'AC',
		link: function (_scope, _element) {
			$timeout(function () {
				_element[0].focus();
			}, 100);
		},
	};
};
