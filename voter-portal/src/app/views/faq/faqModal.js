/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = angular
	.module('app.ui.faqModal', [])

	.controller('faqModal', function (
		$scope,
		$modalInstance,
		$timeout,
		topic,
		config,
	) {
		'ngInject';

		$scope.topic = topic;
		$scope.config = config;

		let focusTo = topic;

		if (!focusTo) {
			focusTo = 'btn_modal_close_1';
		}

		$timeout(function () {
			if (focusTo !== 'btn_modal_close_1') {
				document.getElementById(focusTo).click();
			}
			document.getElementById(focusTo).focus();
			document.getElementById(focusTo).scrollIntoView();
		}, 100);

		$scope.ok = function () {
			$modalInstance.close();
		};
		$scope.cancel = function () {
			$modalInstance.dismiss();
		};
	}).name;
