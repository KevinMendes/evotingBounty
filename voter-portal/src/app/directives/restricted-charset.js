/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function () {
	'ngInject';

	return {
		require: 'ngModel',
		link: function ($scope, $element, attrs, modelCtrl) {
			modelCtrl.$parsers.push(function (inputValue) {
				if (!inputValue) {
					return '';
				}
				const transformedInput = inputValue.replace(/[^0-9]/g, '');

				if (transformedInput !== inputValue) {
					modelCtrl.$setViewValue(transformedInput);
					modelCtrl.$render();
				}

				return transformedInput;
			});
		},
	};
};
