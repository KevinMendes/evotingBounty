/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function ($templateCache, $timeout, $compile) {
	'ngInject';

	return {
		restrict: 'A',
		link: function (scope, element, attrs) {
			if (attrs.dropdown === '') {
				element[0].setAttribute('data-dropdown', 'top-left');
			}

			const dropdownBackdropPath = 'layout/dropdown-backdrop.html';
			const dropdownBackdrop = $compile(
				$templateCache.get(dropdownBackdropPath),
			)(scope);
			const trigger = angular.element(
				element[0].getElementsByClassName('dropdown-trigger'),
			);
			const menu = angular.element(
				element[0].getElementsByClassName('dropdown-menu'),
			);

			function initMenu(target) {
				if (!target.hasClass('hide')) {
					target.addClass('hide');
					target.removeClass('is-open');
				}
			}

			function toggleMenu(target) {
				if (target.hasClass('hide')) {
					// dropdown open
					target.removeClass('hide');
					angular.element(element).prepend(dropdownBackdrop);
					$timeout(function () {
						target.toggleClass('is-open');
					}, 100);
					dropdownBackdrop.on('click', function () {
						toggleMenu(menu);
					});
					trigger[0].setAttribute('aria-expanded', 'true');
				} else {
					// dropdown hide
					target.toggleClass('is-open');
					$timeout(function () {
						angular.element(target[0]).addClass('hide');
						angular
							.element(document.getElementsByClassName('dropdown-backdrop'))
							.remove();
					}, 100);
					trigger[0].setAttribute('aria-expanded', 'false');
				}
			}

			// dropdown init
			initMenu(menu);

			trigger.on('click', function () {
				toggleMenu(menu);
			});
			menu.on('click', function () {
				toggleMenu(menu);
			});
		},
	};
};
