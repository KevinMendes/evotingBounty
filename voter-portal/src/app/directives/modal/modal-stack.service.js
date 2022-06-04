/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function (
	$transition,
	$timeout,
	$document,
	$compile,
	$rootScope,
	$$stackedMap,
) {
	'ngInject';

	const OPENED_MODAL_CLASS = 'modal-open';
	const MUTED_MODAL_CLASS = 'modal-muted';

	let backdropDomEl;
	let backdropScope;
	const openedWindows = $$stackedMap.createNew();
	const tababbleSelector = 'a[href], area[href], input:not([disabled]), button:not([disabled]),select:not([disabled]), textarea:not([disabled]), ' +
		'iframe, object, embed, *[tabindex], *[contenteditable]';
	const $modalStack = {};

	function backdropIndex() {
		let topBackdropIndex = -1;
		const opened = openedWindows.keys();

		for (let i = 0; i < opened.length; i++) {
			if (openedWindows.get(opened[i]).value.backdrop) {
				topBackdropIndex = i;
			}
		}

		return topBackdropIndex;
	}

	$rootScope.$watch(backdropIndex, function (newBackdropIndex) {
		if (backdropScope) {
			backdropScope.index = newBackdropIndex;
		}
	});

	function removeModalWindow(modalInstance) {
		const body = $document.find('body').eq(0);
		const modalWindow = openedWindows.get(modalInstance).value;

		// clean up the stack
		openedWindows.remove(modalInstance);

		// remove window DOM element
		removeAfterAnimate(
			modalWindow.modalDomEl,
			modalWindow.modalScope,
			300,
			function () {
				modalWindow.modalScope.$destroy();
				body.toggleClass(OPENED_MODAL_CLASS, openedWindows.length() > 0);
				checkRemoveBackdrop();
			},
		);
		modalWindow.trapFocusDomEl.remove();
	}

	function checkRemoveBackdrop() {
		// remove backdrop if no longer needed
		if (backdropDomEl && backdropIndex() === -1) {
			let backdropScopeRef = backdropScope;

			removeAfterAnimate(backdropDomEl, backdropScope, 150, function () {
				backdropScopeRef.$destroy();
				backdropScopeRef = null;
			});
			backdropDomEl = null;
			backdropScope = null;
			document.removeEventListener('focus', focusModal, true);
		}
	}

	function removeAfterAnimate(domEl, scope, emulateTime, done) {
		function afterAnimating() {
			if (afterAnimating.done) {
				return;
			}
			afterAnimating.done = true;

			domEl.remove();
			if (done) {
				done();
			}
		}

		// Closing animation
		scope.animate = false;

		const transitionEndEventName = $transition.transitionEndEventName;

		if (transitionEndEventName) {
			// transition out
			const timeout = $timeout(afterAnimating, emulateTime);

			domEl.bind(transitionEndEventName, function () {
				$timeout.cancel(timeout);
				afterAnimating();
				scope.$apply();
			});
		} else {
			// Ensure this call is async
			$timeout(afterAnimating);
		}
	}

	function firstFocusable(domEl) {
		const list = domEl.querySelectorAll(tababbleSelector);

		return list[0];
	}

	function lastFocusable(domEl) {
		const list = domEl.querySelectorAll(tababbleSelector);

		return list[list.length - 1];
	}

	$document.bind('keydown', function (evt) {
		let modal;

		if (evt.which === 27) {
			modal = openedWindows.top();
			if (modal && modal.value.keyboard) {
				evt.preventDefault();
				$rootScope.$apply(function () {
					$modalStack.dismiss(modal.key, 'escape key press');
				});
			}
		}
	});

	function focusModal(evt) {
		let modalDomEl;
		let focusEl;
		let inside = true;
		const modal = openedWindows.top();

		if (modal) {
			modalDomEl = modal.value.modalDomEl;
			inside = modalDomEl[0].contains(evt.target);
		}
		if (!inside) {
			const trapFocusDomEl = modal.value.trapFocusDomEl;
			// explicitOriginalTarget for FF support
			const relatedTarget = evt.relatedTarget || evt.explicitOriginalTarget;
			const fromAddressBar = relatedTarget === null;

			if (trapFocusDomEl[0] === evt.target || fromAddressBar) {
				focusEl = firstFocusable(modalDomEl[0]);
			} else {
				focusEl = lastFocusable(modalDomEl[0]);
			}
			if (focusEl) {
				focusEl.focus();
			} else {
				modalDomEl[0].focus();
			}
		}
	}

	$modalStack.open = function (modalInstance, modal) {
		const modalOpener = $document[0].activeElement;

		openedWindows.add(modalInstance, {
			deferred: modal.deferred,
			modalScope: modal.scope,
			backdrop: modal.backdrop,
			keyboard: modal.keyboard,
		});

		const body = $document.find('body').eq(0);
		const main = angular.element(
			$document[0].getElementById('modal-container'),
		);
		const currBackdropIndex = backdropIndex();

		// aria hide what is not modal
		const modalMuted = angular.element(
			$document[0].getElementsByClassName(MUTED_MODAL_CLASS),
		);

		modalMuted.attr('aria-hidden', 'true'); // NOSONAR Rule javascript:S1192 - False positive
		body.toggleClass(OPENED_MODAL_CLASS, true);

		if (currBackdropIndex >= 0 && !backdropDomEl) {
			backdropScope = $rootScope.$new(true);
			backdropScope.index = currBackdropIndex;
			const angularBackgroundDomEl = angular.element(
				'<div data-modal-backdrop></div>',
			);

			angularBackgroundDomEl.attr('backdrop-class', modal.backdropClass);
			backdropDomEl = $compile(angularBackgroundDomEl)(backdropScope);
			main.append(backdropDomEl);
			document.addEventListener('focus', focusModal, true);
		}

		const angularDomEl = angular.element('<div data-modal-window></div>');

		angularDomEl
			.attr({
				'template-url': modal.windowTemplateUrl,
				'window-class': modal.windowClass,
				'data-size': modal.size,
				'data-index': openedWindows.length() - 1,
				'data-animate': 'animate',
			})
			.html(modal.content);

		const modalDomEl = $compile(angularDomEl)(modal.scope);

		openedWindows.top().value.modalDomEl = modalDomEl;
		openedWindows.top().value.modalOpener = modalOpener;
		main.append(modalDomEl);

		// &nbsp; for FF and Safari
		const trapFocusDomEl = angular.element(
			'<a class="sr-only sr-only-focusable" href="" name="sr-only-focusable">&nbsp;</a>',
		);

		openedWindows.top().value.trapFocusDomEl = trapFocusDomEl;
		main.append(trapFocusDomEl);

		main.addClass(OPENED_MODAL_CLASS);
	};

	$modalStack.close = function (modalInstance, result) {
		const modalWindow = openedWindows.get(modalInstance);

		if (modalWindow) {
			modalWindow.value.deferred.resolve(result);
			removeModalWindow(modalInstance);
			modalWindow.value.modalOpener.focus();
			// aria show what is not modal
			const modalMuted = angular.element(
				$document[0].getElementsByClassName(MUTED_MODAL_CLASS),
			);

			modalMuted.attr('aria-hidden', 'false'); // NOSONAR Rule javascript:S1192 - False positive
		}
	};

	$modalStack.dismiss = function (modalInstance, reason) {
		const modalWindow = openedWindows.get(modalInstance);

		if (modalWindow) {
			modalWindow.value.deferred.reject(reason);
			removeModalWindow(modalInstance);
			modalWindow.value.modalOpener.focus();
			// aria show what is not modal
			const modalMuted = angular.element(
				$document[0].getElementsByClassName(MUTED_MODAL_CLASS),
			);

			modalMuted.attr('aria-hidden', 'false'); // NOSONAR Rule javascript:S1192 - False positive
		}
	};

	$modalStack.dismissAll = function (reason) {
		let topModal = this.getTop();

		while (topModal) {
			this.dismiss(topModal.key, reason);
			topModal = this.getTop();
		}
	};

	$modalStack.getTop = function () {
		return openedWindows.top();
	};

	return $modalStack;
};
