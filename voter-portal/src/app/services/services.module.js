/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = angular
	.module('app.services', [])

	.service('i18n', require('./i18n'))
	.factory('searchService', require('./search'))
	.factory('sessionService', require('./session')).name;
