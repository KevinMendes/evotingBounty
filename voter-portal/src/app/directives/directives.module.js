/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = angular
	.module('app.directives', [])

	.directive({
		pageInfo: require('./page-info/page-info'),
		setPageInfo: require('./page-info/set-page-info'),

		autoFocus: require('./auto-focus'),
		breadcrumbs: require('./breadcrumbs'),
		dropdown: require('./dropdown'),
		inputGuided: require('./input-guided'),
		restrictedCharset: require('./restricted-charset'),
		toastAlert: require('./toast-alert'),
		toggleroneExtended: require('./togglerone-extended'),
		togglerone: require('./togglerone'),
		helpButton: require('./help-button'),
		alertMessage: require('./alert-message'),
		detailedProgressModal: require('./detailed-progress-modal.directive'),
	})

	.factory(
		'detailedProgressService',
		require('./detailed-progress.service'),
	).name;
