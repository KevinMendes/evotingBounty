/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = angular
	.module('app.ui.modal', [require('./ui-bootstrap-transition.module')])

	/**
	 * A helper, internal data structure that acts as a map but also allows getting / removing
	 * elements in the LIFO order
	 */
	.factory('$$stackedMap', require('./stackedMap.service'))

	/**
	 * A helper directive for the $modal service. It creates a backdrop element.
	 */
	.directive('modalBackdrop', require('./modal-backdrop'))

	.directive('modalWindow', require('./modal-window'))

	.directive('modalTransclude', require('./modal-transclude'))

	.factory('$modalStack', require('./modal-stack.service'))

	.provider('$modal', require('./modal.provider'))

	.controller('confirmModal', require('./confirm-modal.ctrl'))
	.controller('defaultModal', require('./default-modal.ctrl')).name;
