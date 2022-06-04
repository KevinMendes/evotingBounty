/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function (config) {
	'ngInject';

	return {
		restrict: 'A',
		require: 'ngModel',
		templateUrl: function () {
			return (
				'views/identification/auth-challenge-' +
				config.identification +
				'.tpl.html'
			);
		},
		link: function (scope, iElement, iAttrs, ngModelCtrl) {
			/**
			 * Zero padding function.
			 *
			 * @param {any} num The number which has to be padded by zeros
			 * @param {any} size Length to accomplish by padding zeros
			 * @returns pad(11, 3) : '011'
			 */
			function pad(num, size) {
				let s = num + '';

				while (s.length < size) {
					s = '0' + s;
				}

				return s;
			}

			/**
			 * Executed whenever the bound ngModel expression changes programmatically.
			 * Used to format / convert the $modelValue for display in the control.
			 */
			ngModelCtrl.$formatters.push(function (modelValue) {
				if (!modelValue) {
					return {
						day: '',
						month: '',
						year: '',
					};
				}

				// If the challenge is "dob", the view model has to be an object with day, month and year
				if (config.identification === 'dob') {
					return {
						day: modelValue.substring(0, 2),
						month: modelValue.substring(2, 2),
						year: modelValue.substring(4, 4),
					};
				}

				// If the auth challenge is "yob", the view model has to be an object only with the year
				return {
					year: modelValue.toString(),
				};
			});

			/**
			 * Called whenever the control updates the ngModelController
			 * with a new $viewValue from the DOM, usually via user input
			 */
			ngModelCtrl.$parsers.push(function (viewValue) {
				let model = '';

				ngModelCtrl.$setValidity('filled', true);
				ngModelCtrl.$setValidity('format', true);
				ngModelCtrl.$setValidity('day', true);
				ngModelCtrl.$setValidity('month', true);
				ngModelCtrl.$setValidity('year', true);

				viewValue.day = viewValue.day.toString().replace(/[^0-9]/g, '');
				viewValue.month = viewValue.month.toString().replace(/[^0-9]/g, '');
				viewValue.year = viewValue.year.toString().replace(/[^0-9]/g, '');

				ngModelCtrl.$setViewValue(viewValue);
				ngModelCtrl.$render();

				let hasErrors = false;

				if (scope.config.identification === 'dob') {
					if (
						viewValue.day &&
						viewValue.day > 0 &&
						viewValue.day <=
						new Date(viewValue.year, viewValue.month, 0).getDate()
					) {
						model += pad(viewValue.day, 2);
					} else {
						ngModelCtrl.$setValidity('day', false);
						hasErrors = true;
					}

					if (viewValue.month && viewValue.month > 0 && viewValue.month <= 12) {
						model += pad(viewValue.month, 2);
					} else {
						ngModelCtrl.$setValidity('month', false);
						hasErrors = true;
					}
				}

				if (
					viewValue.year &&
					viewValue.year.length === 4 &&
					parseInt(viewValue.year) <= new Date().getFullYear()
				) {
					model += pad(viewValue.year, 4);
				} else {
					ngModelCtrl.$setValidity('year', false);
					hasErrors = true;
				}

				if (hasErrors) {
					ngModelCtrl.$setValidity('format', false);

					return null;
				}

				return model;
			});

			scope.$watch('day + month + year', function () {
				// Update the view value if the scope of the directive is changed
				ngModelCtrl.$setViewValue({
					day: scope.day || '',
					month: scope.month || '',
					year: scope.year || '',
				});
			});

			/**
			 * Called when the view needs to be updated (the value referenced
			 * by ng-model is changed programmatically and both the $modelValue
			 * and the $viewValue are different from last time.)
			 */
			ngModelCtrl.$render = function () {
				if (!ngModelCtrl.$viewValue) {
					ngModelCtrl.$viewValue = {
						day: '',
						month: '',
						year: '',
					};
				}

				scope.day = ngModelCtrl.$viewValue.day.toString();
				scope.month = ngModelCtrl.$viewValue.month.toString();
				scope.year = ngModelCtrl.$viewValue.year.toString();
			};
		},
	};
};
