/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function ($modalStack, $timeout) {
	'ngInject';

	return {
		restrict: 'EA',
		scope: {
			index: '@',
			animate: '=',
		},
		replace: true,
		transclude: true,
		templateUrl: function (tElement, tAttrs) {
			return tAttrs.templateUrl || 'views/modals/window.html';
		},
		link: function (scope, element, attrs) {
			element.addClass(attrs.windowClass || '');
			scope.size = attrs.size;

			$timeout(function () {
				// trigger CSS transitions
				scope.animate = true;

				/**
				 * Auto-focusing of a freshly-opened modal element causes any child elements
				 * with the autofocus attribute to lose focus. This is an issue on touch
				 * based devices which will show and then hide the onscreen keyboard.
				 * Attempts to refocus the autofocus element via JavaScript will not reopen
				 * the onscreen keyboard. Fixed by updated the focusing logic to only autofocus
				 * the modal element if the modal does not contain an autofocus element.
				 */
				if (!element[0].querySelectorAll('[autofocus]').length) {
					element[0].focus();
				}
			});

			scope.close = function (evt) {
				const modal = $modalStack.getTop();

				if (
					modal &&
					modal.value.backdrop &&
					modal.value.backdrop !== 'static' &&
					evt.target === evt.currentTarget
				) {
					evt.preventDefault();
					evt.stopPropagation();
					$modalStack.dismiss(modal.key, 'backdrop click');
				}
			};
		},
	};
};
