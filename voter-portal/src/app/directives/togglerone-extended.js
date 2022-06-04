/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function () {
	'ngInject';

	return {
		restrict: 'A',
		link: function (scope, element, attrs) {
			const setAriaAttributes = function (target) {
				if (element.hasClass('is-open')) {
					element[0].setAttribute('aria-selected', 'true');
					target[0].removeAttribute('aria-hidden');
					target[0].setAttribute('aria-expanded', 'true');
				} else {
					element[0].setAttribute('aria-selected', 'false');
					target[0].setAttribute('aria-hidden', 'true');
					target[0].setAttribute('aria-expanded', 'false');
				}
			};

			scope.$on('resetToggles', function () {
				const target = angular.element(
					document.getElementById(attrs.toggleroneExtended),
				);
				const iconTarget = angular.element(
					document.getElementById(attrs.toggleIcon),
				);

				target.removeClass(attrs.toggleroneClass);
				iconTarget.removeClass(attrs.animateIcon);

				setAriaAttributes(target);
			});

			const toggleItems = function () {
				const target = angular.element(
					document.getElementById(attrs.toggleroneExtended),
				);

				target.toggleClass(attrs.toggleroneClass);
				element.toggleClass('is-open');

				setAriaAttributes(target);

				// icon
				const iconTarget = angular.element(
					document.getElementById(attrs.toggleIcon),
				);

				iconTarget.toggleClass(attrs.animateIcon);
			};

			element.on('click', toggleItems);
			element.on('keydown keypress', function (event) {
				if (event.which === 13 || event.which === 32) {
					toggleItems();
					event.preventDefault();
				}
			});
		},
	};
};
