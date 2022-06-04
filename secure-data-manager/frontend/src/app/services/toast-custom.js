/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('toastCustom', [
        // 'conf.globals'
    ])

    .factory('toastCustom', function ($mdToast, gettextCatalog) {
        'use strict';

        // customization for showing the simple toast in the top center & adding an OK for closing it
		const topCenter = function (text, type) {
			let toastClass = 'toast-cutom md-top';
			switch (type) {
				case 'success':
					const closeToast = function (event) {
						$mdToast.hide();
						document.removeEventListener('click', closeToast, true);
					};
					document.addEventListener('click', closeToast, true);
					toastClass += ' toast-success';
					break;
				case 'error':
					toastClass += ' toast-error';
					break;
			}
			return $mdToast
				.simple()
				.content(text)
				.toastClass(toastClass)
				.action(gettextCatalog.getString('Close'))
				.highlightAction(true)
				.highlightClass('toast-btn-close')
				.hideDelay(false)
				.position('top start');
		};

		return {
            topCenter: topCenter,
        };
    });
