/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function () {
	'ngInject';

	return {
		restrict: 'A',
		link: function (scope, element, attrs) {
			const togglerOne = function () {
				const target = angular.element(
					document.getElementById(attrs.togglerone),
				);

				target.toggleClass(attrs.toggleroneClass);
			};

			element.on('click', function () {
				togglerOne();
			});

			element.bind('keydown keypress', function (event) {
				if (event.which === 13 || event.which === 32) {
					const target = angular.element(
						document.getElementById(attrs.togglerone),
					);

					target.toggleClass(attrs.toggleroneClass);
					event.preventDefault();
				}
			});
		},
	};
};
